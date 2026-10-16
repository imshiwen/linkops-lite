-- 可访问性检查:记录跳转后的最终地址和失败原因
alter table backlink_source add column final_url text;
alter table backlink_source add column check_error text;
create index backlink_source_screen_status_idx on backlink_source (screen_status);
