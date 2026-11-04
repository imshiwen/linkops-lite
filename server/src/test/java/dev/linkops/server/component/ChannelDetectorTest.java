package dev.linkops.server.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.linkops.server.dto.Channel;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class ChannelDetectorTest {

    private final ChannelDetector detector = new ChannelDetector();

    @Test
    void wordpressCommentForm() {
        var doc = Jsoup.parse("""
                <div id="respond"><form id="commentform" action="https://x.com/wp-comments-post.php">
                <textarea name="comment"></textarea><input name="author"><input name="email"><input name="url">
                </form></div>""");
        assertEquals(Channel.BLOG_COMMENT, detector.detect(doc));
    }

    @Test
    void submitFormWithUrlField() {
        var doc = Jsoup.parse("<form><input name='tool_name'><input type='url' name='website'></form>");
        assertEquals(Channel.SUBMIT_FORM, detector.detect(doc));
    }

    @Test
    void otherFormAndNoForm() {
        assertEquals(Channel.OTHER, detector.detect(Jsoup.parse("<form><input name='q'></form>")));
        assertEquals(Channel.UNKNOWN, detector.detect(Jsoup.parse("<p>no form</p>")));
    }
}
