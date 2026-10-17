package dev.linkops.server.dto;

/** 抓取后的页面摘要,作为研判输入。正文已截断,避免超出上下文和浪费 token。 */
public record PageSnapshot(String url, String finalUrl, int httpStatus, String title, String description,
                           String forms, String text, int originalTextLength) {}
