package dev.linkops.server.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

/** 提交表单用的资料包。 */
public record SubmitKit(
        @Size(max = 60) String tagline,
        @Size(max = 160) String shortDescription,
        @Size(max = 500) String longDescription,
        List<String> categories,
        List<String> tags,
        String pricing,
        String logoUrl,
        String screenshotUrl) {

    public static SubmitKit empty() {
        return new SubmitKit(null, null, null, List.of(), List.of(), null, null, null);
    }
}
