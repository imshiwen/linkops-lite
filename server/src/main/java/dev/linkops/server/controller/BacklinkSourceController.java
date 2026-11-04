package dev.linkops.server.controller;

import dev.linkops.server.dto.Channel;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /** 粘贴 Markdown 表格或每行一个 URL;channel 指定这批入口的渠道,不传为 unknown(检查时自动识别)。 */
    @PostMapping(value = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ImportResult importList(@RequestBody String text, @RequestParam(required = false) String channel) {
        return service.importText(text, Channel.parse(channel));
    }

    @GetMapping
    public List<BacklinkSource> list(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String channel,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "50") int limit) {
        return service.list(status, channel, keyword, limit);
    }

    /** 手动修正渠道,请求体为渠道名,如 blog_comment。 */
    @PatchMapping(value = "/{id}/channel", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void updateChannel(@PathVariable long id, @RequestBody String channel) {
        service.updateChannel(id, Channel.parse(channel));
    }

    @GetMapping("/channel/stats")
    public List<Map<String, Object>> channelStats() {
        return service.channelCounts();
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
