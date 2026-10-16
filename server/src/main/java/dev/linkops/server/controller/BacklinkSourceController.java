package dev.linkops.server.controller;

import dev.linkops.server.dto.ImportResult;
import dev.linkops.server.dto.LinkCheckSummary;
import dev.linkops.server.dto.SourceStats;
import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.service.BacklinkSourceService;
import dev.linkops.server.service.LinkCheckService;
import java.util.List;
import java.util.Map;
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
    private final LinkCheckService linkCheckService;

    public BacklinkSourceController(BacklinkSourceService service, LinkCheckService linkCheckService) {
        this.service = service;
        this.linkCheckService = linkCheckService;
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

    /** 可访问性检查。默认只查还没检查过的(status=new);传 status= 空字符串则全部重查。 */
    @PostMapping("/check")
    public LinkCheckSummary check(@RequestParam(defaultValue = "new") String status,
                                  @RequestParam(defaultValue = "1000") int limit) {
        return linkCheckService.check(status, limit);
    }

    @GetMapping("/check/stats")
    public List<Map<String, Object>> checkStats() {
        return linkCheckService.statusCounts();
    }
}
