package dev.linkops.server.dto;

/**
 * 单个入口的可访问性检查结果。
 * status: ok | dead | tls_invalid | blocked | unreachable | error
 * channel: 按页面结构识别的渠道(blog_comment / submit_form / other / unknown),请求失败时为 null。
 */
public record LinkCheckResult(Long id, String status, Integer httpStatus, String finalUrl, String error, String channel) {}
