package dev.linkops.server.judge;

import java.util.List;

/**
 * 研判结果:模型按这个结构输出 JSON,Spring AI 自动反序列化。
 *
 * TODO(你来设计):想清楚研判要给出哪些信息,后续"审核列表、排序、起草、评估"都依赖这些字段。
 * 可以考虑:是否适合、0-100 分、渠道类型(目录/博客评论/社区…)、能否留链接、理由、风险、信心。
 * 每个字段写一行注释说明含义和取值范围;字段名会出现在 JSON schema 里,模型会参考它。
 */
public record JudgmentResult(
        /** 是否适合推广目标站 */
        boolean suitable,
        /** 判断理由,每条一句话 */
        List<String> reasons) {}
