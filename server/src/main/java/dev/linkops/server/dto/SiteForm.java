package dev.linkops.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** 新建 / 编辑站点的请求体,也是返回给页面的结构(带 id)。 */
public record SiteForm(
        Long id,
        @NotBlank String name,
        @NotBlank String domain,
        @NotBlank String homepageUrl,
        @NotBlank String contactName,
        @NotBlank @Email String contactEmail,
        String commentName,
        @NotBlank String intro,
        @Valid SubmitKit submitKit,
        List<@Valid KeyPage> keyPages,
        List<String> facts) {}
