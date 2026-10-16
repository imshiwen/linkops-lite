package dev.linkops.server.dto;

/** 外链库统计。 */
public record SourceStats(long total, long hosts, long extraEntriesOnSameHost) {}
