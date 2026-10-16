package dev.linkops.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 解析外链列表:支持 Markdown 表格(编号|网站与入口|简介|批次|备注)和每行一个 URL。 */
public final class SourceListParser {

    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]*)]\\(<?(https?://[^)>\\s]+)>?\\)");
    private static final Pattern BARE_URL = Pattern.compile("https?://[^\\s)<>|]+");
    private static final Pattern URL_ONLY_LINE = Pattern.compile("^<?(https?://\\S+?)>?$");

    private SourceListParser() {}

    public record Row(String url, String name, String description, String batch, String note) {}

    public static List<Row> parse(String text) {
        List<Row> rows = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String t = line.strip();
            if (t.startsWith("|")) {
                parseTableRow(t).ifPresent(rows::add);
            } else {
                // 纯 URL 列表:整行只能是一个 URL,避免把说明文字里的链接当成外链入口
                Matcher m = URL_ONLY_LINE.matcher(t);
                if (m.matches()) rows.add(new Row(m.group(1), null, null, null, null));
            }
        }
        return rows;
    }

    private static Optional<Row> parseTableRow(String line) {
        String[] cells = line.substring(1, line.endsWith("|") ? line.length() - 1 : line.length()).split("\\|", -1);
        // 只接受首列为编号的数据行,跳过表头、分隔行和说明性表格
        if (cells.length < 2 || !cells[0].strip().matches("\\d+")) return Optional.empty();
        Matcher m = MD_LINK.matcher(cells[1]);
        String url;
        String linkNote = null;
        if (m.find()) {
            url = m.group(2);
            // 链接文字里除 URL 外的说明,如"(暂时发不了)",并入备注
            String extra = m.group(1).replace(url, "").replaceAll("^\\s*\\(|\\)\\s*$", "").strip();
            if (!extra.isEmpty()) linkNote = extra;
        } else {
            Matcher b = BARE_URL.matcher(cells[1]);
            if (!b.find()) return Optional.empty();
            url = b.group();
        }
        String description = cell(cells, 2);
        String name = description == null ? null : description.split("[（：(]", 2)[0].strip();
        return Optional.of(new Row(url, name, description, cell(cells, 3), join(linkNote, cell(cells, 4))));
    }

    private static String cell(String[] cells, int i) {
        if (i >= cells.length) return null;
        String v = cells[i].strip();
        return v.isEmpty() || v.equals("—") || v.equals("-") ? null : v;
    }

    private static String join(String a, String b) {
        if (a == null) return b;
        if (b == null) return a;
        return a + "; " + b;
    }
}
