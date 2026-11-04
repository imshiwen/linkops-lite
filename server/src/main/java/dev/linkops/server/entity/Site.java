package dev.linkops.server.entity;

import java.time.OffsetDateTime;

/** 自有站配置,对应表 site。jsonb 列在实体里以 JSON 字符串保存,由 service 转换。 */
public class Site {
    private Long id;
    private String name;
    private String domain;
    private String homepageUrl;
    private String contactName;
    private String contactEmail;
    private String commentName;
    private String intro;
    private String submitKit;
    private String keyPages;
    private String facts;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getHomepageUrl() { return homepageUrl; }
    public void setHomepageUrl(String homepageUrl) { this.homepageUrl = homepageUrl; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getCommentName() { return commentName; }
    public void setCommentName(String commentName) { this.commentName = commentName; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getSubmitKit() { return submitKit; }
    public void setSubmitKit(String submitKit) { this.submitKit = submitKit; }
    public String getKeyPages() { return keyPages; }
    public void setKeyPages(String keyPages) { this.keyPages = keyPages; }
    public String getFacts() { return facts; }
    public void setFacts(String facts) { this.facts = facts; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
