# linkops-lite 设计(v0.1)

目标:单用户、本机优先的外链工作台。先保证能用,再借它搭建完整的 AI 工程能力。

## 数据模型(4+2 张表)

- `backlink_source`:全局外链列表。URL 规范化后按 host 去重,一次入库,供所有自有站复用。
- `site`:我的站点资料(名称、域名、定位、功能、事实清单)。RAG 语料来源。
- `candidate`:`source × site` 的候选关系,每个自有站独立状态:
  `new → judged → approved|rejected → drafted → filled → submitted|abandoned`
- `judgment`:某个候选针对某个站的研判结果(评分、理由、表单类型、nofollow 等),保留模型版本和 prompt 版本,便于评估。
- `draft`:评论草稿 + 引用的资料出处。
- `submission`:人工提交结果。"已填写 ≠ 已提交 ≠ 已公开"。

## 两阶段筛选

1. **入库初筛(便宜,不用 LLM):** URL 规范化、去重、可访问性检查、粗分类。
2. **选用时研判(LLM):** 用户为某个自有站选定一批候选后,才对这批做抓取+适配研判。

## 执行边界

批量研判、起草、填表自动;提交由用户在目标页点击。不绕过验证码、不换身份重试。
第三方页面内容一律视为不可信输入。草稿事实必须出自站点资料。

## 开发规则

- 开发库用 Neon `dev` 分支,不在生产分支上测试。
- 连接串只放 `server/.env.local`,不进 Git;提交 `.env.example`。
- 十天内功能清单冻结;新想法记到 `docs/ideas.md`。
