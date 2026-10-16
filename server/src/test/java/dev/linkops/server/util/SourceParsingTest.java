package dev.linkops.server.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class SourceParsingTest {

    @Test
    void normalizesForDedup() {
        var a = UrlNormalizer.normalize("https://WWW.Example.com/submit/?utm_source=x&b=2#top ");
        var b = UrlNormalizer.normalize("https://example.com/submit?b=2");
        assertEquals(b.url(), a.url());
        assertEquals("example.com", a.host());
    }

    @Test
    void parsesMarkdownTableRow() {
        String md = """
                | 编号 | 网站与入口 | 网站简介 | 批次或日期 | 备注 |
                |---:|---|---|---|---|
                | 1 | [https://aivalley.ai/submit-tool/ (暂时发不了)](<https://aivalley.ai/submit-tool/>) | AI Valley（aivalley.ai）：是 AI 工具目录。 | — | 未收录 入口关闭 |
                """;
        List<SourceListParser.Row> rows = SourceListParser.parse(md);
        assertEquals(1, rows.size());
        var r = rows.get(0);
        assertEquals("https://aivalley.ai/submit-tool/", r.url());
        assertEquals("AI Valley", r.name());
        assertNull(r.batch());
        assertEquals("暂时发不了; 未收录 入口关闭", r.note());
    }

    @Test
    void skipsHeaderAndNonNumberedTableRows() {
        String md = """
                | 网站 | 原历史备注中的数字 | 来源 |
                |---|---:|---|
                | producthunt.com | 4.84M | [Similarweb](https://www.similarweb.com/website/producthunt.com/) |
                """;
        assertTrue(SourceListParser.parse(md).isEmpty());
    }

    @Test
    void parsesPlainUrlLines() {
        var rows = SourceListParser.parse("https://a.com/x\nnot a url\nhttps://b.com\n来源:[GitHub](https://c.com/readme)\n");
        assertEquals(List.of("https://a.com/x", "https://b.com"), rows.stream().map(SourceListParser.Row::url).toList());
    }
}
