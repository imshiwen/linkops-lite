# AI 工程师最短路线(基于 roadmap.sh/ai-engineer)

来源:roadmap.sh 开源仓库 `roadmaps/ai-engineer/content`,共约 190 个节点(2026-10-16 读取)。
近一半节点是"同类选一个"的替代品(各家模型、各家向量库、各家观测平台),所以压缩成 10 组。

深度:**做** = 在 linkops-lite 里亲手实现;**懂** = 能讲清原理和取舍;**知道** = 知道是什么、什么时候用。

| # | 能力组 | roadmap 节点 | 深度 | 在 linkops-lite 哪里练 | Day |
|---|---|---|---|---|---|
| 1 | LLM 基础 | how-llms-work, tokens, context-window, temperature, top-p, top-k, sampling-parameters, type-of-models, closed-vs-open-source-models, choosing-the-right-model | 懂 | 选模型;同一研判任务改 temperature 对比输出稳定性 | 2 |
| 2 | 调用与输出 | using-sdks-directly / openai-compatible-apis, streaming-responses, structured-output, constraining-outputs-and-inputs, prompt-caching | 做 | 先裸 HTTP 调一次,再上 Spring AI;研判结果用 JSON 结构化输出并校验重试 | 2 |
| 3 | Prompt 与上下文 | prompt-engineering, system-prompting, zero-shot, few-shot, cot, role--behavior, robust-prompt-engineering, context-engineering, context-compaction, context-failure-modes | 做 | 研判 prompt 迭代:zero-shot → few-shot(用你标注的例子)→ 加推理步骤 | 3 |
| 4 | Embedding 与向量库 | embeddings, embedding-models, chunking, vector-database, semantic-search, performing-similarity-search, indexing-embeddings | 做 | 站点资料切块入 pgvector;候选页面与站点主题的相似度 | 4 |
| 5 | RAG | rag, retrieval-process, generation, rag--dynamic-filters, rag-vs-fine-tuning | 做 | 起草时检索站点资料,附出处;资料不足时拒答 | 5 |
| 6 | 工具与 MCP | function-calling, tools--function-calling, mcp, mcp-server, mcp-client, transport-layer, building-an-mcp-server | 做 | 工具:抓页面、查去重、写台账;把候选库做成 MCP Server,在 Claude Code 里直接查 | 6 |
| 7 | Agent | ai-agents, react, manual-implementation, memory-systems, state--historical-context, multi-agents | 做(多 Agent 只懂) | 批量研判流程:抓取 → 判断 → 不确定时补抓 → 记录 | 7 |
| 8 | 评估 | llm-evaluations, evaluation-metrics, deterministic-evals, model-based-evals, human-evals, regression-testing, ragas, deepeval | 做 | 你标注 30 个站,算研判准确率;草稿事实有据率;改 prompt 前后回归 | 8 |
| 9 | 可观测与成本 | llm-observability, tracing--logging, costlatency-monitoring, production-monitoring, langfuse / langsmith / helicone | 做(平台知道一个) | 每次调用记录 token、耗时、成本、prompt 版本 | 9 |
| 10 | 安全 | prompt-injection-attacks, security-and-privacy-concerns, context-security, adding-end-user-ids-in-prompts, conducting-adversarial-testing, content-moderation-apis | 做 | 抓来的网页是不可信输入:造几个藏指令的页面做对抗测试 | 9 |

## 学习方式:A(在 linkops-lite 里练)

**硬规则:十天内只做路线上的事。** 每天的代码必须落在当天的路线节点上;浏览器扩展(自动填表)、页面美化等非 AI 工作排到十天之后,想法记到 `ideas.md`。

| Day | 学什么(路线) | 在 linkops-lite 里做什么 | 为什么有用 |
|---|---|---|---|
| 1 | (前置,不属于路线) | 建库、导入、去重、可访问性检查 | 后续所有能力的数据底座 |
| 2 | 第 1、2 组 | 模型读一个外链页面,输出"适不适合"的 JSON | 研判雏形 |
| 3 | 第 3 组 | 用你标注的例子迭代研判 prompt | 判得更准 |
| 4 | 第 4 组 | 可用站简介与自有站做向量相似度 | 不调大模型先粗筛,省钱 |
| 5 | 第 5 组 | 起草评论时检索站点资料,附出处 | 草稿不编造 |
| 6 | 第 6 组 | 模型调用"抓页面""查去重";候选库做成 MCP Server | 在 Claude Code 里直接问候选 |
| 7 | 第 7 组 | 批量研判流程 | 选一批站一键研判 |
| 8 | 第 8 组 | 30 个标注站算准确率,改动前后回归 | 知道它靠不靠谱 |
| 9 | 第 9、10 组 | 成本记录;恶意网页对抗测试 | 放心批量跑 |
| 10 | 复盘 | 架构图、README、面试故事 | 能讲出来 |

## 只需"知道"、不进十天计划

- 多模态:image/video/audio understanding、speech-to-text、text-to-speech、whisper、dall-e、image-generation
- 训练侧:fine-tuning、training、inference-engineering、hugging-face 系列、sentence-transformers
- 自部署:self-hosted-models、ollama、lm-studio(能讲"什么时候值得自部署")
- 框架:langchain、llama-index、haystack、ragflow、各家 agent SDK(Java 侧用 Spring AI,能对比即可)
- 开发工具:claude-code、cursor、codex、devin、windsurf(日常已在用)
- 各家模型和向量库清单:选一个用,其余知道名字

## 和 FDE备战 D16–D19 的关系

D16–D19 是**面试答题卡**(怎么说),不是路线。路线以本文件为准;每天学完后,可以用对应的 D 课检查自己能不能说出来:
第 1–2 组 → D16,第 4–5 组 → D17,第 6–7 组 → D18,第 8 组和上下文 → D19。
