package dev.linkops.server.judge;

import java.util.List;

/** 研判预览结果:结论 + 用于实验对比的调用数据。 */
public record JudgeResponse(
        long sourceId,
        String url,
        JudgmentResult result,
        List<String> validationErrors,
        int attempts,
        String rawOutput,
        Integer promptTokens,
        Integer completionTokens,
        Object nativeUsage,
        long elapsedMs,
        String model,
        boolean thinking,
        Double temperature,
        boolean nativeSchema) {}
