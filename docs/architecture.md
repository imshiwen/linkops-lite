# linkops-lite 项目结构说明

> 给自己看的项目地图:每个目录放什么、一次请求怎么走、加功能改哪里。
> 随项目推进更新,以代码为准。

## 1. 技术栈一览

| 层 | 选型 | 作用 |
|---|---|---|
| 语言 / 运行时 | Java 21 | record、虚拟线程、文本块 `"""` 都用到了 |
| Web 框架 | Spring Boot 4.1 + Spring MVC | 提供 REST 接口,端口 8090 |
| 持久层 | MyBatis 4.1(XML Mapper) | SQL 写在 `resources/mapper/*.xml` |
| 数据库 | Postgres(Neon 托管)+ pgvector | 业务数据 + 向量检索(Day 4 起) |
| 表结构管理 | Flyway | `resources/db/migration/V*.sql`,启动时自动执行未执行过的版本 |
| AI | Spring AI 2.0.1(OpenAI 兼容接口) | 对话:qwen3.8-flash;Embedding:qwen3.7-text-embedding,均为百炼 |
| 网页解析 | jsoup | 抓取外链页面后提取标题、表单、正文 |
| 构建 | Maven | `server/pom.xml` |

## 2. 目录结构

```
linkops-lite/
├── README.md               启动方式
├── docs/                   设计、路线、每日学习记录(你现在读的这份也在这)
├── eval/                   实验和评估脚本,结果在 eval/results/
├── extension/              Chrome 扩展(十天后再做)
└── server/                 Spring Boot 后端
    ├── pom.xml             依赖和版本
    ├── .env.example        环境变量样例(.env.local 存真实值,不进 Git)
    └── src/
        ├── main/java/dev/linkops/server/
        │   ├── ServerApplication.java   启动入口
        │   ├── config/        配置类:数据源(Neon 连接串转换)
        │   ├── controller/    接口层:接收 HTTP 请求、参数校验、返回 JSON
        │   ├── service/       业务层:导入去重、可访问性检查、研判(抓页面、调模型、结果校验)
        │   ├── mapper/        数据访问层接口(MyBatis),SQL 在 resources/mapper
        │   ├── entity/        与表一一对应的实体类
        │   ├── dto/           接口出入参和模型结构化输出(record),不直接暴露实体
        │   └── util/          无状态工具:URL 规范化、列表解析
        ├── main/resources/
        │   ├── application.properties   应用配置(引用 .env.local 里的变量)
        │   ├── db/migration/            Flyway 迁移脚本
        │   ├── mapper/                  MyBatis XML
        │   ├── prompts/                 提示词模板(.st,{变量} 占位)
        │   └── sites/                   自有站资料(Day 5 前先用文件,之后进库)
        └── test/java/                   单元测试
```

**分层规则(MVC + 三层):** `controller → service → mapper → 数据库`,只能上层调下层,不跨层、不反向。
所有功能都按层存放,不建按功能聚合的包。以研判为例:

| 层 | 研判相关的类 |
|---|---|
| controller | `JudgeController` |
| service | `JudgeService`(流程编排)、`PageFetcher`(抓页面)、`JudgmentValidator`(结果校验) |
| dto | `JudgeRequest`、`JudgeResponse`、`JudgmentResult`(模型输出结构)、`PageSnapshot`(页面摘要) |
| mapper | 复用 `BacklinkSourceMapper.findById` |

调用 AI 的 `ChatClient` 属于外部依赖,和数据库一样只在 service 层使用。

## 3. 一次请求怎么走

### 例 1:导入外链列表 `POST /api/sources/import`

```
curl --data-binary @list.md
  → BacklinkSourceController.importList()        接收文本
  → BacklinkSourceService.importText()           业务编排(@Transactional)
      → SourceListParser.parse()                 解析 Markdown 表格 / URL 行
      → UrlNormalizer.normalize()                规范化,列表内去重
      → BacklinkSourceMapper.insertIgnoreBatch() 每 200 条一批
          → BacklinkSourceMapper.xml             insert ... on conflict do nothing
  ← ImportResult(dto)                            转成 JSON 返回
```

### 例 2:研判预览 `POST /api/judge/preview`

```
{"sourceId": 9, "thinking": false}
  → JudgeController.preview()
  → JudgeService.preview()
      → BacklinkSourceMapper.findById()          查外链入口
      → PageFetcher.fetch()                      抓页面、提取表单和正文(截断)
      → 读取 sites/cursivefontgenerator.md       自有站资料
      → ChatClient.prompt()                      Spring AI:system + user 模板 + 参数
          .options(...)                          思考开关、temperature
          .call().responseEntity(JudgmentResult) 结构化输出,自动反序列化
              → OpenAI Java SDK → HTTP POST {AI_CHAT_BASE_URL}/chat/completions
      → JudgmentValidator.validate()             业务规则校验
  ← JudgeResponse                                结果 + token + 耗时,供实验对比
```

## 4. 配置是怎么生效的

1. `server/.env.local`(不进 Git)存放真实值:数据库连接串、模型地址和 key。
2. `application.properties` 第一行 `spring.config.import` 把它当作配置文件导入。
3. 其余配置用 `${变量名:默认值}` 引用,例如 `spring.ai.openai.chat.model=${AI_CHAT_MODEL:qwen3.8-flash}`。
4. Spring Boot 自动配置根据这些属性创建 Bean:`DataSource`(我们自定义,见 `DataSourceConfig`)、`ChatClient.Builder`、`EmbeddingModel` 等。
5. 代码里通过构造器注入拿到这些 Bean,不用 `new`。

## 5. 数据库与 Flyway

- 表结构只通过新增迁移文件修改:要改表就新建 `V3__xxx.sql`,**不要改已经执行过的 V1、V2**(Flyway 会校验文件哈希,改了会启动失败)。
- 当前表:`backlink_source`(外链入口)、`site`、`candidate`、`judgment`、`draft`、`submission`,含义见 `design.md`。
- 开发库是 Neon 的 `dev` 分支;对远程库的写入操作(迁移、导入、删除)都要先确认。

## 6. 加一个功能,通常改哪些地方

以"给外链入口加备注"为例:

1. 表结构:`db/migration/V3__source_remark.sql`
2. 实体:`entity/BacklinkSource.java` 加字段
3. SQL:`mapper/BacklinkSourceMapper.xml` 加 `<update>`,接口 `BacklinkSourceMapper.java` 加方法
4. 业务:`service/BacklinkSourceService.java` 加方法
5. 接口:`controller/BacklinkSourceController.java` 加 `@PatchMapping`,出入参放 `dto/`
6. 测试:纯逻辑写单元测试;涉及数据库的先手动用 curl 验证

## 7. 常用命令

```bash
cd server
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # IDEA 里设好 SDK 就不需要
./mvnw test                                         # 单元测试(不连数据库、不调模型)
./mvnw spring-boot:run                              # 启动,会执行未执行的迁移
curl localhost:8090/actuator/health                 # 健康检查
```

## 8. 和 Spring AI 相关的几个版本差异(2.0 vs 网上常见的 1.x 教程)

- 底层改用 OpenAI 官方 Java SDK;`base-url` 要写到 `/v1`,没有 `completions-path` 配置。
- 模型名配置是 `spring.ai.openai.chat.model`,旧的 `chat.options.model` 已废弃。
- `ChatClient.prompt().options(...)` 传的是 Builder,不是 `build()` 之后的对象。
- 结构化输出可选 `useProviderStructuredOutput()`(接口层 json_schema)和 `validateSchema()`(校验失败自动重试)。
- starter 默认会创建语音、图片、审核模型,用不到要关掉(`spring.ai.model.xxx=none`),否则启动时要求额外的 key。
