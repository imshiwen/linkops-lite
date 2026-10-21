# Day 3 · Prompt 与上下文工程(路线第 3 组)

> 目标:把"宽进"原则写进研判 prompt v2,标注集扩到 25 条,用回归证明 v2 比 v1 好,且没有把原来对的改错。

## 1. 学习(约 60 分钟)

| 概念 | 学完要能回答 | 在本项目里对应什么 |
|---|---|---|
| Prompt 的结构 | 一个好的 system prompt 由哪几部分组成?角色、任务、判断标准、输出要求、边界各管什么? | `prompts/judge/v1/system.st` 的五段 |
| zero-shot / few-shot | 什么时候给例子?例子怎么选(覆盖边界情况、正反都有)?为什么例子不能用测试集里的站? | v2 要加 2–3 个例子 |
| 推理引导(CoT) | 关了思考,还能让模型"先想再答"吗?字段顺序(先写理由再下结论)有影响吗? | `JudgmentResult` 字段顺序 |
| 上下文工程 | 上下文里放了什么、放多少?正文截断 4000 字符合理吗?信息放在开头和中间,模型注意力一样吗? | `PageFetcher.MAX_TEXT_CHARS`、user 模板 |
| 指令冲突 | prompt、字段说明、校验规则说法不一致时,模型听谁的?怎么发现? | 见第 2 节 |
| Prompt 版本与回归 | 改了 prompt 怎么证明变好了?为什么必须跑全量标注而不只看改的那条? | `prompts/judge/<版本>/`、`eval/regress.py` |

参考:roadmap 节点 prompt-engineering、system-prompting、few-shot、cot、context-engineering、context-failure-modes。
自测:FDE备战 D19 中关于上下文的题。

## 2. 先做一个决定:档位(level)是什么意思?

**我核对时发现三处说法不一致:**

| 位置 | 现在的说法 |
|---|---|
| `design.md` 宽进原则 | 通用目录即使受众不完全重合也判 SUITABLE,**用 HIGH/MEDIUM/LOW 区分优先级** |
| `v1/system.st` 打分规则 | LOW = 没有相关度 |
| `JudgmentValidator` | SUITABLE 不能是 LOW |

按宽进原则,"适合但优先级低"应该存在;按 v1 和校验器,它不存在。三者要统一,建议二选一:

- **方案 A(推荐):** level 只表示"适合的站里先发哪个"。SUITABLE 可以是 HIGH/MEDIUM/LOW;UNSUITABLE 不再分档,统一填 LOW 或新增 `NONE`。校验器相应修改。
- **方案 B:** 保持 v1 含义(LOW = 不相关),宽进只靠 verdict,不用档位排优先级。

决定后写进 `design.md`,v2 prompt 和校验器按它改。

## 3. 你要做的

### 3.1 标注 20 条(约 30 分钟)

打开 `eval/to-label.csv`,在 `expected` 列填 `SUITABLE` / `UNSUITABLE` / `UNCERTAIN`,`note` 写一句理由。
这 20 条按类型抽样:AI 工具目录、通用软件目录、产品发布、综合链接目录、社区、设计创意。
标完执行 `python3 eval/regress.py merge`,并入 `labels.csv`。

**标注标准**(用宽进原则,标不准的写进 note,别硬猜):
- UNSUITABLE:明确只收某类产品(如只收 AI 工具)、只收付费、没有地方留链接、明显垃圾站。
- SUITABLE:能留链接,且访客里有人可能用得上花体字工具。
- UNCERTAIN:页面打不开内容、需要登录才能看到提交入口等。

### 3.2 跑 v1 基线(约 10 分钟)

```bash
python3 ~/WebApp/linkops-lite/eval/regress.py run --versions v1
```

看结果文件里的**混淆矩阵**和**判错的理由**,找出 v1 错在哪一类。

### 3.3 写 prompt v2(核心,你来写)

在 `server/src/main/resources/prompts/judge/v2/system.st`(和需要的话 `user.st`)里写。至少包括:
1. 宽进原则。
2. 第 2 节决定后的档位含义。
3. 页面没有明确说明时,三个 YES/NO 字段填 UNKNOWN(Day 2 遗留问题)。
4. 2–3 个 few-shot 例子:正反都有,**不用标注集里的站**。

如果第 2 节选了方案 A,同时改 `JudgmentValidator` 和它的测试(这部分也算核心代码)。

### 3.4 回归对比

```bash
python3 ~/WebApp/linkops-lite/eval/regress.py run --versions v1,v2
```

**通过标准:** v2 准确率高于 v1,且 v1 判对的条目 v2 没有判错。通过后把 `application.properties` 的 `app.judge.prompt-version` 改成 `v2`。
不通过就看"判错的理由"继续改,每改一次都跑全量,不只看改的那条。

## 4. 已准备好的(我写的)

- 提示词按版本存放:`prompts/judge/v1/`(原来的)、`prompts/judge/v2/`(待你写);请求可带 `promptVersion` 指定版本,默认取 `app.judge.prompt-version`。
- `eval/to-label.csv`:20 个待标注入口。
- `eval/regress.py`:`merge` 合并标注;`run` 按版本跑全量标注,输出准确率、稳定性、重试次数、token、混淆矩阵、逐条对比和判错理由。参数固定为关闭思考、temperature=0,只变 prompt。

**注意:** IDEA 里运行的服务要重启才能用上版本管理(端口 8090 上现在是旧代码,`promptVersion` 返回为空)。

## 5. 实验结论(做完后填)

| 版本 | 准确率 | 稳定性 | 重试>1 次 | 平均输入 token |
|---|---|---|---|---|
| v1 | | | | |
| v2 | | | | |

v2 改了什么、修好了哪类错、有没有新错:

## 6. 面试怎么讲(做完后写)

问题:**"你怎么改 prompt?怎么知道改好了?"**

## 7. 复盘

- 做了什么:
- 踩了什么坑:
- 做了什么取舍:
