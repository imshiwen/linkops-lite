package dev.linkops.server.dto;

import jakarta.validation.constraints.NotBlank;

/** 站点重要页面:博客评论按文章内容选最相关的一页填进 Website 字段。 */
public record KeyPage(@NotBlank String url, String summary) {}
