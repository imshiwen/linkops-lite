package dev.linkops.server.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 研判结果:模型按这个结构输出 JSON,Spring AI 自动反序列化。
 * 字段与 judge-system.st 的判断维度一一对应;@JsonPropertyDescription 会进入 JSON schema,模型据此理解字段含义。
 */
public record JudgmentResult(
        @JsonPropertyDescription("总结论:SUITABLE=适合,UNSUITABLE=不适合,UNCERTAIN=信息不足无法判断(不要猜测)")
        Verdict verdict,

        @JsonPropertyDescription("档位,按打分规则:HIGH=高,MEDIUM=中,LOW=低;UNRATED 仅在 verdict 为 UNCERTAIN 时使用")
        Level level,

        @JsonPropertyDescription("渠道类型")
        ChannelType channelType,

        @JsonPropertyDescription("页面是否有可填写网址的字段")
        Answer hasUrlField,

        @JsonPropertyDescription("留链接是否需要登录或注册")
        Answer requiresLogin,

        @JsonPropertyDescription("留链接是否需要付费")
        Answer requiresPayment,

        @JsonPropertyDescription("质量与风险问题,如垃圾站、链接农场、入口失效;没有则为空数组")
        List<String> risks,

        @JsonPropertyDescription("判断理由,每条一句话,必须对应页面中的具体信息(标题、表单字段、正文)")
        List<String> reasons) {

    public enum Verdict { SUITABLE, UNSUITABLE, UNCERTAIN }

    public enum Level { HIGH, MEDIUM, LOW, UNRATED }

    public enum ChannelType { DIRECTORY, BLOG_COMMENT, COMMUNITY, RESOURCE_PAGE, OTHER, UNKNOWN }

    /** 三态:页面上看不出来时用 UNKNOWN,不要猜。 */
    public enum Answer { YES, NO, UNKNOWN }
}
