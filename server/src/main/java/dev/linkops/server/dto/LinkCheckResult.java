package dev.linkops.server.dto;

/**
 * 单个入口的可访问性检查结果。
 * status: ok | dead | tls_invalid | blocked | unreachable | error
 */
public record LinkCheckResult(Long id, String status, Integer httpStatus, String finalUrl, String error) {}
