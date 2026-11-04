-- P1:外链渠道类型;站点独立配置
alter table backlink_source add column channel text not null default 'unknown';  -- blog_comment | submit_form | other | unknown
create index backlink_source_channel_idx on backlink_source (channel);

alter table site drop column positioning;
alter table site add column homepage_url  text;
alter table site add column contact_name  text;
alter table site add column contact_email text;
alter table site add column comment_name  text;            -- 评论 Name 字段,为空时用 contact_name
alter table site add column intro         text;
alter table site add column submit_kit    jsonb not null default '{}';
alter table site add column key_pages     jsonb not null default '[]';
alter table site add column updated_at    timestamptz not null default now();
