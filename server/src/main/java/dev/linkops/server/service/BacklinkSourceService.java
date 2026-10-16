package dev.linkops.server.service;

import dev.linkops.server.dto.ImportResult;
import dev.linkops.server.dto.SourceStats;
import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.mapper.BacklinkSourceMapper;
import dev.linkops.server.util.SourceListParser;
import dev.linkops.server.util.UrlNormalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 外链列表:入库初筛(解析、规范化、去重),不调用模型。 */
@Service
public class BacklinkSourceService {

    /** 每批插入条数:远程库按批发送,减少网络往返。 */
    private static final int BATCH_SIZE = 200;

    private final BacklinkSourceMapper mapper;

    public BacklinkSourceService(BacklinkSourceMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public ImportResult importText(String text) {
        List<SourceListParser.Row> rows = SourceListParser.parse(text);
        Set<String> seen = new HashSet<>();
        List<String> invalid = new ArrayList<>();
        List<BacklinkSource> toInsert = new ArrayList<>();
        int dupInList = 0;

        for (SourceListParser.Row row : rows) {
            UrlNormalizer.Normalized n;
            try {
                n = UrlNormalizer.normalize(row.url());
            } catch (RuntimeException e) {
                invalid.add(row.url());
                continue;
            }
            if (!seen.add(n.url())) {
                dupInList++;
                continue;
            }
            toInsert.add(toEntity(row, n));
        }

        int inserted = 0;
        for (int i = 0; i < toInsert.size(); i += BATCH_SIZE) {
            inserted += mapper.insertIgnoreBatch(toInsert.subList(i, Math.min(i + BATCH_SIZE, toInsert.size())));
        }
        return new ImportResult(rows.size(), inserted, dupInList, toInsert.size() - inserted, invalid);
    }

    public List<BacklinkSource> list(int limit) {
        return mapper.list(Math.min(Math.max(limit, 1), 500));
    }

    public SourceStats stats() {
        return mapper.stats();
    }

    private static BacklinkSource toEntity(SourceListParser.Row row, UrlNormalizer.Normalized n) {
        BacklinkSource s = new BacklinkSource();
        s.setUrl(row.url().strip());
        s.setNormalizedUrl(n.url());
        s.setHost(n.host());
        s.setName(row.name());
        s.setDescription(row.description());
        s.setBatch(row.batch());
        s.setNote(row.note());
        return s;
    }
}
