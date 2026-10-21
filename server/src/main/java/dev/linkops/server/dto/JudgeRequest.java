package dev.linkops.server.dto;

/**
 * 一次研判预览的参数。
 * thinking:是否开启模型思考,默认关闭;temperature:为空时用 app.judge.temperature(默认 0);
 * nativeSchema:true 用接口层 json_schema 约束,false 把格式要求写进提示词(Spring AI 默认);
 * promptVersion:提示词版本,对应 resources/prompts/judge/<版本>/,为空时用 app.judge.prompt-version。
 */
public record JudgeRequest(long sourceId, String site, Boolean thinking, Double temperature, Boolean nativeSchema,
                           String promptVersion) {

    public String siteOrDefault() {
        return site == null || site.isBlank() ? "cursivefontgenerator" : site;
    }
}
