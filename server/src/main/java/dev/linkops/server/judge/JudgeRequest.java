package dev.linkops.server.judge;

/**
 * 一次研判预览的参数。
 * thinking:是否开启模型思考;temperature:为空时用模型默认值;
 * nativeSchema:true 用接口层 json_schema 约束,false 把格式要求写进提示词(Spring AI 默认)。
 */
public record JudgeRequest(long sourceId, String site, Boolean thinking, Double temperature, Boolean nativeSchema) {

    public String siteOrDefault() {
        return site == null || site.isBlank() ? "cursivefontgenerator" : site;
    }
}
