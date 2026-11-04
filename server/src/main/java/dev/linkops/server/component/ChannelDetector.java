package dev.linkops.server.component;

import dev.linkops.server.dto.Channel;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * 按页面结构粗判渠道,不调用模型。只用于 channel 还是 unknown 的入口,判错可手动改。
 * 博客评论:WordPress 等常见评论表单;提交表单:表单里有网址类字段。
 */
@Component
public class ChannelDetector {

    public Channel detect(Document doc) {
        if (!doc.select("form#commentform, form[action*=wp-comments-post], textarea[name=comment], #respond form").isEmpty()) {
            return Channel.BLOG_COMMENT;
        }
        for (Element form : doc.select("form")) {
            if (!form.select("input[type=url], input[name~=(?i)(url|website|site|link|domain)]").isEmpty()) {
                return Channel.SUBMIT_FORM;
            }
        }
        return doc.select("form").isEmpty() ? Channel.UNKNOWN : Channel.OTHER;
    }
}
