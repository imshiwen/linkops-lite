package dev.linkops.server.service;

import dev.linkops.server.dto.KeyPage;
import dev.linkops.server.dto.SiteForm;
import dev.linkops.server.dto.SubmitKit;
import dev.linkops.server.entity.Site;
import dev.linkops.server.exception.NotFoundException;
import dev.linkops.server.mapper.SiteMapper;
import dev.linkops.server.util.DomainNormalizer;
import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** 自有站配置:增改查,jsonb 字段与 DTO 互转。 */
@Service
public class SiteService {

    private final SiteMapper mapper;
    private final JsonMapper json;

    public SiteService(SiteMapper mapper, JsonMapper json) {
        this.mapper = mapper;
        this.json = json;
    }

    public List<SiteForm> list() {
        return mapper.list().stream().map(this::toForm).toList();
    }

    public SiteForm get(long id) {
        Site s = mapper.findById(id);
        if (s == null) throw new NotFoundException("站点不存在:" + id);
        return toForm(s);
    }

    public SiteForm create(SiteForm form) {
        Site s = toEntity(form);
        mapper.insert(s);
        return get(s.getId());
    }

    public SiteForm update(long id, SiteForm form) {
        Site s = toEntity(form);
        s.setId(id);
        if (mapper.update(s) == 0) throw new NotFoundException("站点不存在:" + id);
        return get(id);
    }

    private Site toEntity(SiteForm f) {
        Site s = new Site();
        s.setName(f.name().strip());
        s.setDomain(DomainNormalizer.normalize(f.domain()));
        s.setHomepageUrl(f.homepageUrl().strip());
        s.setContactName(f.contactName().strip());
        s.setContactEmail(f.contactEmail().strip());
        s.setCommentName(blankToNull(f.commentName()));
        s.setIntro(f.intro().strip());
        s.setSubmitKit(json.writeValueAsString(f.submitKit() == null ? SubmitKit.empty() : f.submitKit()));
        s.setKeyPages(json.writeValueAsString(f.keyPages() == null ? List.of() : f.keyPages()));
        s.setFacts(json.writeValueAsString(f.facts() == null ? List.of()
                : f.facts().stream().map(String::strip).filter(x -> !x.isEmpty()).toList()));
        return s;
    }

    private SiteForm toForm(Site s) {
        return new SiteForm(s.getId(), s.getName(), s.getDomain(), s.getHomepageUrl(), s.getContactName(),
                s.getContactEmail(), s.getCommentName(), s.getIntro(),
                json.readValue(s.getSubmitKit(), SubmitKit.class),
                json.readValue(s.getKeyPages(), new TypeReference<List<KeyPage>>() {}),
                json.readValue(s.getFacts(), new TypeReference<List<String>>() {}));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
