# linkops-lite

轻量外链工作台:外链列表入库初筛 → 为自有站研判候选 → 起草 → 浏览器扩展填写 → 人工提交。
同时作为 AI 工程能力的学习项目,学习记录见 `docs/`。

## 本地运行(IDEA)

1. Project SDK 选 Java 21。
2. 复制 `server/.env.example` 为 `server/.env.local`,填 Neon `dev` 分支连接串。
3. 运行 `server` 模块的 `ServerApplication`,端口 8090。工作目录为项目根或 `server/` 均可。

命令行:`cd server && ./mvnw spring-boot:run`(需 `JAVA_HOME` 指向 Java 21)。

## 结构

- `server/`:Spring Boot 4 + MyBatis + Flyway + Postgres(Neon,pgvector)
  - `controller` / `service` / `mapper` / `entity` / `dto` / `util` / `config`
  - SQL:`resources/mapper/*.xml`;迁移:`resources/db/migration`
- `extension/`:Chrome 扩展(待开发)
- `eval/`:评估用例与脚本
- `docs/`:设计与每日学习记录
