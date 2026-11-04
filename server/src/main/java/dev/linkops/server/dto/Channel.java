package dev.linkops.server.dto;

import java.util.Locale;

/** 外链渠道类型,库里存小写字符串。 */
public enum Channel {
    BLOG_COMMENT, SUBMIT_FORM, OTHER, UNKNOWN;

    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Channel parse(String s) {
        if (s == null || s.isBlank()) return UNKNOWN;
        try {
            return valueOf(s.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知渠道:" + s + ",可选 blog_comment / submit_form / other / unknown");
        }
    }
}
