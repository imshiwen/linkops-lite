package dev.linkops.server.component;

import dev.linkops.server.dto.KeyPage;
import dev.linkops.server.dto.SiteForm;
import dev.linkops.server.dto.SubmitKit;
import java.util.List;
import org.springframework.stereotype.Component;

/** 把站点配置渲染成给模型看的文本。不包含联系邮箱等隐私字段。 */
@Component
public class SiteProfileRenderer {

    public String render(SiteForm s) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(s.name()).append("\n\n");
        line(sb, "域名", s.domain());
        line(sb, "首页", s.homepageUrl());
        line(sb, "介绍", s.intro());
        SubmitKit k = s.submitKit();
        if (k != null) {
            line(sb, "一句话", k.tagline());
            line(sb, "短描述", k.shortDescription());
            line(sb, "定价", k.pricing());
            if (k.categories() != null && !k.categories().isEmpty()) line(sb, "分类", String.join(", ", k.categories()));
        }
        list(sb, "事实清单(只能引用这些事实)", s.facts());
        List<KeyPage> pages = s.keyPages();
        if (pages != null && !pages.isEmpty()) {
            sb.append("\n## 重要页面\n");
            for (KeyPage p : pages) sb.append("- ").append(p.url()).append(p.summary() == null ? "" : ":" + p.summary()).append('\n');
        }
        return sb.toString();
    }

    private static void line(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) sb.append("- ").append(label).append(":").append(value.strip()).append('\n');
    }

    private static void list(StringBuilder sb, String title, List<String> items) {
        if (items == null || items.isEmpty()) return;
        sb.append("\n## ").append(title).append('\n');
        for (String i : items) sb.append("- ").append(i).append('\n');
    }
}
