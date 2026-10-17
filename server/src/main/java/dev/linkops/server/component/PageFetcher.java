package dev.linkops.server.component;

import dev.linkops.server.dto.PageSnapshot;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/** 抓取外链入口页面,提取标题、描述、表单结构和正文。 */
@Component
public class PageFetcher {

    /** 正文最多保留的字符数。太长浪费 token,太短信息不够,Day 2 实验可以调。 */
    static final int MAX_TEXT_CHARS = 4000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; linkops-lite/0.1; +https://github.com/imshiwen/linkops-lite)";

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public PageSnapshot fetch(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url.strip()))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,*/*")
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            Document doc = Jsoup.parse(resp.body(), resp.uri().toString());
            doc.select("script, style, noscript, svg, iframe").remove();
            String text = doc.body() == null ? "" : doc.body().text();
            return new PageSnapshot(url, resp.uri().toString(), resp.statusCode(),
                    doc.title(),
                    doc.select("meta[name=description]").attr("content"),
                    describeForms(doc),
                    text.length() > MAX_TEXT_CHARS ? text.substring(0, MAX_TEXT_CHARS) : text,
                    text.length());
        } catch (Exception e) {
            throw new IllegalStateException("fetch failed: " + url + " -> " + e.getMessage(), e);
        }
    }

    /** 把页面里的表单压缩成一行行描述,让模型知道有没有可提交的入口。 */
    static String describeForms(Document doc) {
        List<String> lines = new ArrayList<>();
        for (Element form : doc.select("form")) {
            List<String> fields = new ArrayList<>();
            for (Element input : form.select("input, textarea, select")) {
                String type = input.tagName().equals("input") ? input.attr("type") : input.tagName();
                if (type.equals("hidden") || type.equals("submit")) continue;
                String name = !input.attr("name").isBlank() ? input.attr("name") : input.attr("id");
                fields.add(type.isBlank() ? name : type + ":" + name);
            }
            if (fields.isEmpty()) continue;
            String id = form.id().isBlank() ? "" : "#" + form.id();
            lines.add("form" + id + " [" + String.join(", ", fields) + "]");
            if (lines.size() >= 5) break;
        }
        return lines.isEmpty() ? "未发现表单" : String.join("\n", lines);
    }
}
