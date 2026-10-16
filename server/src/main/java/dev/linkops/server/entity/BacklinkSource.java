package dev.linkops.server.entity;

import java.time.OffsetDateTime;

/** 全局外链入口,对应表 backlink_source。 */
public class BacklinkSource {
    private Long id;
    private String url;
    private String normalizedUrl;
    private String host;
    private String name;
    private String description;
    private String batch;
    private String note;
    private String screenStatus;
    private Integer httpStatus;
    private String finalUrl;
    private String checkError;
    private OffsetDateTime checkedAt;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getNormalizedUrl() { return normalizedUrl; }
    public void setNormalizedUrl(String normalizedUrl) { this.normalizedUrl = normalizedUrl; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getScreenStatus() { return screenStatus; }
    public void setScreenStatus(String screenStatus) { this.screenStatus = screenStatus; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getFinalUrl() { return finalUrl; }
    public void setFinalUrl(String finalUrl) { this.finalUrl = finalUrl; }
    public String getCheckError() { return checkError; }
    public void setCheckError(String checkError) { this.checkError = checkError; }
    public OffsetDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(OffsetDateTime checkedAt) { this.checkedAt = checkedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
