package dev.linkops.server.util;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** 规范化 URL 用于去重:小写主机、去 www、去默认端口、去锚点和跟踪参数、去末尾斜杠。 */
public final class UrlNormalizer {

    private UrlNormalizer() {}

    public record Normalized(String url, String host) {}

    public static Normalized normalize(String raw) {
        URI u = URI.create(raw.trim());
        String scheme = u.getScheme() == null ? "https" : u.getScheme().toLowerCase(Locale.ROOT);
        String host = u.getHost().toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        int port = u.getPort();
        boolean defaultPort = port == -1 || (port == 80 && scheme.equals("http")) || (port == 443 && scheme.equals("https"));

        String path = u.getRawPath() == null ? "" : u.getRawPath();
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        String query = u.getRawQuery() == null ? null : Arrays.stream(u.getRawQuery().split("&"))
                .filter(p -> !p.isBlank())
                .filter(p -> !p.toLowerCase(Locale.ROOT).matches("^(utm_[^=]*|ref|fbclid|gclid)(=.*)?$"))
                .sorted()
                .collect(Collectors.joining("&"));

        String url = scheme + "://" + host + (defaultPort ? "" : ":" + port) + path
                + (query == null || query.isEmpty() ? "" : "?" + query);
        return new Normalized(url, host);
    }
}
