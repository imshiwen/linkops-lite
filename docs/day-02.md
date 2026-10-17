# Day 2 · 模型调用与结构化输出(路线第 1、2 组)

> 目标:给定一个外链入口和 cursivefontgenerator.online,模型输出"适不适合"的结构化结果;
> 用实验说明思考开关、temperature、结构化输出方式对结果的影响。

## 1. 学习(约 45 分钟)

| 概念 | 学完要能回答 |
|---|---|
| Token、上下文窗口 | 为什么只回复"你好"也要 92 个 token?研判一次约 1250 输入 token,其中大头是什么? |
| temperature / top_p | 研判任务该调高还是调低?为什么两者只调一个?思考模式下 temperature 有什么限制? |
| 思考模式 | 开思考换来了什么、付出了什么?哪些任务值得开? |
| 结构化输出 | 提示词约束 vs 接口层 json_schema 的区别;`validateSchema()` 解决什么,不解决什么? |
| 调用失败 | 超时、限流、格式错误、业务不合理,分别在哪一层处理? |

参考:roadmap 节点 tokens、context-window、temperature、top-p、structured-output、streaming-responses。
自测:FDE备战 D16 第 1、2、3、9 题。

## 2. 你要写的 4 处(代码里搜 `TODO(你来`)

1. **`prompts/judge-system.st`**:评估目标、判断标准、打分规则、信息不足时不要猜。
2. **`prompts/judge-user.st`**:用户消息模板,`{site}` `{url}` `{title}` `{description}` `{forms}` `{text}` 是可用变量。
3. **`dto/JudgmentResult.java`**:研判结果字段。后续审核、排序、起草、评估都依赖它,想清楚再定。
4. **`service/JudgmentValidator.java` + `service/JudgeService.java` 里的重试**:业务规则校验;不通过时把错误反馈给模型重试,有上限。

改完后单独测一个入口:

```bash
curl -s -X POST localhost:8090/api/judge/preview -H 'Content-Type: application/json' \
  -d '{"sourceId": 9, "thinking": false}'
```

验收标准:
- 字段齐全、取值合理,`validationErrors` 为空。
- 对明显不相关的 AI 工具目录给出"不适合"并说清理由;对通用免费工具目录能给出更高的分。
- 页面信息不足时,结果里能体现"不确定",而不是编造。

## 3. 实验(我写好了脚本,你跑并写结论)

先挑 5 个入口:2 个 AI 工具目录、1 个通用软件目录、1 个免费工具目录、1 个你认为适合的。然后:

```bash
python3 eval/day02_experiment.py --ids 4,9,40,<id>,<id> --runs 3
```

脚本会对比 5 组配置:基线、temperature=0、temperature=1.2、开启思考、原生 json_schema。

| 配置 | 结论一致率 | 平均耗时 | 平均输出 token | 你的结论 |
|---|---|---|---|---|
| 基线 | | | | |
| temperature=0 | | | | |
| temperature=1.2 | | | | |
| 开启思考 | | | | |
| 原生 json_schema | | | | |

**最后定下:研判默认用哪组参数,理由是什么。** 这就是你的第一个面试故事。

## 4. 已知事实(我验证过的)

- qwen3.8-flash **默认开启思考**:只回复"你好"时 26 个输出 token 里有 22 个是思考 token。脚手架默认传 `enable_thinking=false`。
- 用占位 prompt 研判 SaaSHub:输入 1256 token,输出 203 token,耗时 6.3 秒,结论"不适合"(B2B 软件目录,与花体字工具受众不符)。
- 百炼文档:思考模式下 temperature 低于 0.6 会被改成 0.6;`json_object` 要求提示词里出现 JSON 字样。

## 5. 复盘(你来写三行)

- 做了什么:
- 踩了什么坑:
- 做了什么取舍:
