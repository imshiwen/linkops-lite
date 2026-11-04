package dev.linkops.server.util;

import java.net.URI;
import java.util.Locale;

/** 把用户输入的域名或网址统一成小写、去 www 的主机名,如 "https://www.Example.com/x" → "example.com"。 */
public final class DomainNormalizer {

    private DomainNormalizer() {}

    public static String normalize(String input) {
        String s = input.strip();
        String host = s.contains("://") ? URI.create(s).getHost() : s.split("[/?#]", 2)[0];
        if (host == null || host.isBlank()) throw new IllegalArgumentException("域名无效:" + input);
        return host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
    }
}
