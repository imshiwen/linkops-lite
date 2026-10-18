package dev.linkops.server.service;

import dev.linkops.server.component.JudgmentValidator;
import dev.linkops.server.component.PageFetcher;
import dev.linkops.server.dto.JudgeRequest;
import dev.linkops.server.dto.JudgeResponse;
import dev.linkops.server.dto.JudgmentResult;
import dev.linkops.server.dto.PageSnapshot;
import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.mapper.BacklinkSourceMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/** 单个外链入口的研判预览:抓页面 → 拼提示词 → 调模型 → 结构化结果 → 业务校验。 */
@Service
public class JudgeService {

    private final ChatClient chatClient;
    private final PageFetcher fetcher;
    private final JudgmentValidator validator;
    private final BacklinkSourceMapper sourceMapper;
    private final Resource systemPrompt = new ClassPathResource("prompts/judge-system.st");
    private final Resource userPrompt = new ClassPathResource("prompts/judge-user.st");
    private final String model;
    /** 最多尝试次数(含第一次)。 */
    private final int maxAttempts;
    /** 请求未指定时使用的 temperature;Day 2 实验结论:研判用 0 最稳最准。 */
    private final double defaultTemperature;

    public JudgeService(ChatClient.Builder builder, PageFetcher fetcher, JudgmentValidator validator,
                        BacklinkSourceMapper sourceMapper,
                        @Value("${spring.ai.openai.chat.model:}") String model,
                        @Value("${app.judge.max-attempts:3}") int maxAttempts,
                        @Value("${app.judge.temperature:0}") double defaultTemperature) {
        this.chatClient = builder.build();
        this.fetcher = fetcher;
        this.validator = validator;
        this.sourceMapper = sourceMapper;
        this.model = model;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.defaultTemperature = defaultTemperature;
    }

    public JudgeResponse preview(JudgeRequest req) {
        BacklinkSource source = sourceMapper.findById(req.sourceId());
        if (source == null) throw new IllegalArgumentException("source not found: " + req.sourceId());
        PageSnapshot page = fetcher.fetch(source.getUrl());
        String site = loadSite(req.siteOrDefault());
        boolean thinking = Boolean.TRUE.equals(req.thinking());
        boolean nativeSchema = Boolean.TRUE.equals(req.nativeSchema());
        double temperature = req.temperature() != null ? req.temperature() : defaultTemperature;

        // 百炼兼容接口的 enable_thinking 不在 OpenAI 标准参数里,用 extraBody 放到请求体顶层
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .extraBody(Map.of("enable_thinking", thinking))
                .temperature(temperature);

        // 先渲染成文本消息:重试时要在同一段对话后面追加"上次输出 + 修正意见"
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(new PromptTemplate(systemPrompt).render()));
        messages.add(new UserMessage(new PromptTemplate(userPrompt).render(Map.of(
                "site", site,
                "url", page.finalUrl(),
                "title", page.title(),
                "description", page.description(),
                "forms", page.forms(),
                "text", page.text()))));

        long start = System.currentTimeMillis();
        JudgmentResult result = null;
        List<String> errors = List.of();
        String raw = null;
        int promptTokens = 0, completionTokens = 0;
        Object nativeUsage = null;
        int attempts = 0;

        // 重试只处理"格式解析失败"和"业务规则矛盾",网络超时、限流由 HTTP 客户端层处理
        while (attempts < maxAttempts) {
            attempts++;
            try {
                ResponseEntity<ChatResponse, JudgmentResult> entity = chatClient.prompt()
                        .options(options)
                        .messages(messages)
                        .call()
                        .responseEntity(JudgmentResult.class, spec -> {
                            if (nativeSchema) spec.useProviderStructuredOutput();
                            spec.validateSchema();
                        });
                ChatResponse resp = entity.response();
                raw = resp == null ? null : resp.getResult().getOutput().getText();
                Usage usage = resp == null ? null : resp.getMetadata().getUsage();
                if (usage != null) {
                    // 成本按所有尝试累计,否则重试会让实验里的 token 数偏低
                    promptTokens += nz(usage.getPromptTokens());
                    completionTokens += nz(usage.getCompletionTokens());
                    nativeUsage = usage.getNativeUsage();
                }
                result = entity.entity();
                errors = validator.validate(result);
            } catch (RuntimeException e) {
                // 输出不是合法 JSON 或不符合 schema:没有可用结果,同样带着原因重试
                result = null;
                errors = List.of("上次输出无法解析为符合要求的 JSON:" + firstLine(e.getMessage()));
            }
            if (errors.isEmpty() || attempts >= maxAttempts) break;

            if (raw != null) messages.add(new AssistantMessage(raw));
            messages.add(new UserMessage(feedback(errors)));
        }

        return new JudgeResponse(source.getId(), page.finalUrl(), result, errors, attempts, raw,
                promptTokens, completionTokens, nativeUsage,
                System.currentTimeMillis() - start,
                model, thinking, temperature, nativeSchema);
    }

    /** 修正意见:逐条列出违反的规则,要求输出完整结果而不是只改动的字段。 */
    private static String feedback(List<String> errors) {
        StringBuilder sb = new StringBuilder("你上一次的研判结果没有通过校验,问题如下:\n");
        for (String e : errors) sb.append("- ").append(e).append('\n');
        sb.append("请根据同样的页面信息修正这些问题,重新输出完整的研判结果。不要编造页面中没有的信息。");
        return sb.toString();
    }

    private static int nz(Integer n) {
        return n == null ? 0 : n;
    }

    private static String firstLine(String s) {
        if (s == null) return "未知错误";
        String line = s.lines().findFirst().orElse(s);
        return line.length() > 200 ? line.substring(0, 200) : line;
    }

    private static String loadSite(String name) {
        try {
            return new ClassPathResource("sites/" + name + ".md").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("site profile not found: " + name, e);
        }
    }
}
