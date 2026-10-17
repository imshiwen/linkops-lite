
## Day 4 注意(百炼 embedding 限制,来自官方文档 2026-10-17)

- 已改用 qwen3.7-text-embedding:单次请求最多 20 条,单条最多 128,000 token。Spring AI 默认按 token 数分批,可能一次超过 20 条,要自定义分批策略。
- 维度定为 1024。pgvector 的 `vector` 类型建 HNSW 索引上限 2000 维,选 2048/2560 就得改用 `halfvec`。
- 兼容模式不支持 `text_type`(query/document 区分),只有 DashScope 原生接口支持。先用兼容模式,评估检索效果后再决定是否切原生接口。
- 换 embedding 模型或维度,已有向量全部作废,需要重新生成。

## Day 2 注意(百炼 Chat 兼容接口,来自官方文档 2026-10-17)

- qwen3.8 是混合思考模型:`enable_thinking` 控制是否思考,HTTP 放在请求体顶层;文档没写 qwen3.8-flash 的默认值,Day 2 实测。
- 思考模式下 temperature 低于 0.6 会被改成 0.6;非思考模式默认 temperature 0.7、top_p 0.8。文档建议两者只调一个。
- `response_format` 支持 json_object 和 json_schema,但支持的模型不同,Day 2 实测;json_object 要求提示词里写明 JSON。
- `tool_choice=required` Qwen 暂不支持;`parallel_tool_calls` 默认 false。
- 流式要拿用量需 `stream_options.include_usage=true`,用量只在最后一个数据块。
