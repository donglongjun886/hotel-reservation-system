# AGENTS.md

## 项目背景

本项目是一个酒店管理系统，核心功能为：预订、订单查询、办理入住（含退房）。用途是面试作业，要求：**代码可运行、可演示**，并附完整过程文档（需求方案、技术方案、测试用例等）。

## 工作流程

项目按阶段推进：

**需求方案 → 技术方案 → 编码测试 → 总结**

- 每个阶段只做该阶段的事，不提前进入下一阶段的工作（如需求阶段不写代码、不做技术选型）
- 每个阶段完成后，必须停下来等用户确认，确认后才进入下一阶段

## 文档约定

过程文档统一放在 `docs/` 目录下，命名规则：

| 文件 | 内容 |
| --- | --- |
| `docs/01-需求文档.md` | 需求方案（PRD） |
| `docs/02-技术方案.md` | 技术方案 |
| `docs/03-测试用例.md` | 测试用例 |
| `docs/04-实现思路和亮点.md` | 实现思路与亮点总结 |

补充文档（如原型需求）按序编号续排（如 `docs/05-xxx.md`）。

## 范围约束

只做 **预订 → 订单查询 → 入住 → 退房** 的业务闭环，不要过度设计。明确不做：在线支付、订单改期、一单多间/多房型、浮动房价、会员/优惠券/发票/通知等。

## 操作边界

文件操作仅限项目根目录（AGENTS.md 所在目录）及其子目录；确需访问项目外内容，先征得用户同意。

## 提交规范

每个阶段完成后提示用户提交 git，提交信息格式为：`阶段: 简述`（如 `需求方案: 输出PRD与前端原型需求`）。

## 禁止事项

- 未经确认不引入新依赖
- 不删除或覆盖已有文档
- 不主动实现范围外功能

## 编码规范

规则以 `docs/02-技术方案.md` 为准，此处只列容易违反的硬规则：

- **依赖白名单**：只允许使用技术方案 §1.3 列出的依赖；新增依赖（含 Pinia、Flyway 等）必须先经用户确认
- **分层纪律**：业务规则只写 service 层，controller 只做参数校验与组装响应；mapper 不跨模块使用；跨模块调用只允许 `order → inventory`、`order → room`、`room → inventory`（UC-09 房间维护需同步未来库存行）
- **金额口径**：后端内部 BigDecimal（单位元）、DB 存 DECIMAL(10,2)、前后端接口一律传"分"单位的整数；元↔分换算只在 DTO 序列化层，禁止在前端或业务代码里换算；前端换算只准出现在 `src/utils/money.js`（formatYuan 渲染 / yuanToFen 提交 / fenToYuan 回填）
- **状态流转**：一律用带源状态条件的 UPDATE（如 `WHERE status='CONFIRMED'`），禁止"先查后改"
- **统一响应**：接口返回 `Result{code, message, data}`；业务错误抛 `BizException`，message 使用原型文档中的提示文案
- **鉴权**：登录态走 `X-Token` 请求头；住客数据一律按 token 中的 user_id 过滤，禁止信任前端传的 userId
- **DB 脚本幂等**：`schema.sql` 用 `CREATE TABLE IF NOT EXISTS`，`data.sql` 用 `INSERT IGNORE`
- **前端路由**：用 hash 模式（静态托管下刷新不 404）；接口调用统一走 `src/api/`，组件内不直接写 axios

通用风格：

- **单一职责**：方法和类保持单一职责，避免过度抽象（与技术方案 §3.4"不做假 seam"一致）
- **命名**：清晰、优先使用业务领域名称；术语沿用 PRD（房型、订单状态、入住窗口等），文档与代码用词一致
- **可读性**：不为减少代码行数牺牲业务逻辑可读性
- **局部修改**：修改代码时优先局部修改，不做无关重构；延续现有代码风格，不引入新写法/新框架

## 技术栈、目录结构、启动和测试命令

**技术栈**：后端 Spring Boot 3 + Java 17 + MyBatis-Plus；前端 Vue 3 + Vite + Element Plus；数据库 MySQL 8（docker-compose）。

**目录结构**：

```
hotel-server/        # 后端 Maven 工程，按业务模块分包：auth / room / inventory / order
                     # 模块内四层：controller / service / mapper / entity
hotel-web/           # 前端 Vite 工程，页面在 src/views/guest 与 src/views/admin
docker-compose.yml   # MySQL 8
start.sh             # 一键构建并启动
```

**启动**：

- 一键启动（完整构建）：`./start.sh`（前端构建 → 产物拷入后端 static → Maven 打包 → docker compose up → 启动 jar）
- 后端 dev：`cd hotel-server && mvn spring-boot:run`
- 前端 dev：`cd hotel-web && npm run dev`（5173 端口，/api 代理到 8080）
- 仅启动数据库：`docker compose up -d`

**测试**：`cd hotel-server && mvn test`（需 MySQL 已启动）。

## 其他约定

- 文档、注释、回复默认使用中文
- skill 和 agent 配置分别放在 `ai-process/skills/`、`ai-process/agents/`
