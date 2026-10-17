package dev.linkops.server.judge;

import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.mapper.BacklinkSourceMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
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
    private final BacklinkSourceMapper sourceMapper;
    private final Resource systemPrompt = new ClassPathResource("prompts/judge-system.st");
    private final Resource userPrompt = new ClassPathResource("prompts/judge-user.st");
    private final String model;

    public JudgeService(ChatClient.Builder builder, PageFetcher fetcher, BacklinkSourceMapper sourceMapper,
                        @Value("${spring.ai.openai.chat.model:}") String model) {
        this.chatClient = builder.build();
        this.fetcher = fetcher;
        this.sourceMapper = sourceMapper;
        this.model = model;
    }

    public JudgeResponse preview(JudgeRequest req) {
        BacklinkSource source = sourceMapper.findById(req.sourceId());
        if (source == null) throw new IllegalArgumentException("source not found: " + req.sourceId());
        PageSnapshot page = fetcher.fetch(source.getUrl());
        String site = loadSite(req.siteOrDefault());
        boolean thinking = Boolean.TRUE.equals(req.thinking());
        boolean nativeSchema = Boolean.TRUE.equals(req.nativeSchema());

        // 百炼兼容接口的 enable_thinking 不在 OpenAI 标准参数里,用 extraBody 放到请求体顶层
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .extraBody(Map.of("enable_thinking", thinking));
        if (req.temperature() != null) options.temperature(req.temperature());

        long start = System.currentTimeMillis();
        ResponseEntity<ChatResponse, JudgmentResult> entity = chatClient.prompt()
                .options(options)
                .system(systemPrompt)
                .user(u -> u.text(userPrompt).params(Map.of(
                        "site", site,
                        "url", page.finalUrl(),
                        "title", page.title(),
                        "description", page.description(),
                        "forms", page.forms(),
                        "text", page.text())))
                .call()
                .responseEntity(JudgmentResult.class, spec -> {
                    if (nativeSchema) spec.useProviderStructuredOutput();
                    spec.validateSchema();
                });

        // TODO(你来写):业务校验不通过时怎么办?
        // 思路:把 validationErrors 作为反馈追加到对话里,让模型修正,最多重试 N 次;
        // 仍不通过就返回最后一次结果并标记失败,不要无限重试。重试次数记到 attempts。
        JudgmentResult result = entity.entity();
        List<String> errors = JudgmentValidator.validate(result);
        int attempts = 1;

        ChatResponse resp = entity.response();
        Usage usage = resp == null ? null : resp.getMetadata().getUsage();
        return new JudgeResponse(source.getId(), page.finalUrl(), result, errors, attempts,
                resp == null ? null : resp.getResult().getOutput().getText(),
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                usage == null ? null : usage.getNativeUsage(),
                System.currentTimeMillis() - start,
                model, thinking, req.temperature(), nativeSchema);
    }

    private static String loadSite(String name) {
        try {
            return new ClassPathResource("sites/" + name + ".md").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("site profile not found: " + name, e);
        }
    }
}
