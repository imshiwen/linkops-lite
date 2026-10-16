package dev.linkops.server.dto;

import java.util.Map;

/** 一次批量检查的汇总:本次检查数量、各状态数量、耗时。 */
public record LinkCheckSummary(int checked, Map<String, Long> byStatus, long elapsedMs) {}
