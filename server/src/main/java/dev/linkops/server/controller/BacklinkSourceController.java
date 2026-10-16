package dev.linkops.server.controller;

import dev.linkops.server.dto.ImportResult;
import dev.linkops.server.dto.SourceStats;
import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.service.BacklinkSourceService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sources")
public class BacklinkSourceController {

    private final BacklinkSourceService service;

    public BacklinkSourceController(BacklinkSourceService service) {
        this.service = service;
    }

    /** 粘贴 Markdown 表格或每行一个 URL。 */
    @PostMapping(value = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ImportResult importList(@RequestBody String text) {
        return service.importText(text);
    }

    @GetMapping
    public List<BacklinkSource> list(@RequestParam(defaultValue = "50") int limit) {
        return service.list(limit);
    }

    @GetMapping("/stats")
    public SourceStats stats() {
        return service.stats();
    }
}
