create extension if not exists vector;

-- 全局外链列表:同一入口 URL 只存一份,供所有自有站复用
create table backlink_source (
  id             bigserial primary key,
  url            text not null,
  normalized_url text not null unique,
  host           text not null,
  name           text,
  description    text,
  batch          text,
  note           text,
  screen_status  text not null default 'new',   -- new | ok | dead
  http_status    int,
  checked_at     timestamptz,
  created_at     timestamptz not null default now()
);
create index backlink_source_host_idx on backlink_source (host);

-- 我的自有站
create table site (
  id          bigserial primary key,
  name        text not null,
  domain      text not null unique,
  positioning text,
  facts       jsonb not null default '[]',
  created_at  timestamptz not null default now()
);

-- 外链来源 × 自有站 的候选关系,每个站独立推进
create table candidate (
  id         bigserial primary key,
  source_id  bigint not null references backlink_source(id),
  site_id    bigint not null references site(id),
  status     text not null default 'new',  -- new|judged|approved|rejected|drafted|filled|submitted|abandoned
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (source_id, site_id)
);

create table judgment (
  id             bigserial primary key,
  candidate_id   bigint not null references candidate(id),
  score          int,
  verdict        text,
  reasons        jsonb,
  form_type      text,
  nofollow       boolean,
  model          text,
  prompt_version text,
  created_at     timestamptz not null default now()
);

create table draft (
  id           bigserial primary key,
  candidate_id bigint not null references candidate(id),
  content      text not null,
  citations    jsonb not null default '[]',
  model        text,
  created_at   timestamptz not null default now()
);

-- 已填写 ≠ 已提交 ≠ 已公开
create table submission (
  id           bigserial primary key,
  candidate_id bigint not null references candidate(id),
  draft_id     bigint references draft(id),
  status       text not null,  -- filled | submitted | abandoned | unknown
  note         text,
  created_at   timestamptz not null default now()
);
