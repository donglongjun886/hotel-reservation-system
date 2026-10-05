# 酒店预订系统

酒店管理演示系统（面试作业），覆盖 **预订 → 订单查询 → 办理入住 → 退房** 的业务闭环，分住客端与前台管理端两个界面。技术栈：Spring Boot 3 + Vue 3 + MySQL 8。

## 提交物导览

| 提交物 | 位置 | 说明 |
| --- | --- | --- |
| 1. 完整的可运行代码 | `hotel-server/`、`hotel-web/` | 后端 Maven 工程（按 auth/room/inventory/order 分包）与前端 Vite 工程，`./start.sh` 一键启动 |
| 2. 与 AI 沟通的原始会话记录 | `ai-process/conversations/` | 13 份按时间导出的会话 Markdown |
| 3. 使用的 Skill | `ai-process/skills/` | 6 个自定义 Skill（implement、tdd、code-review、grilling 等） |
| 4. 使用的 Agent | `ai-process/conversations/` | 未单独定义自定义 Agent，使用 Kimi Code 内置子代理，调用过程体现在会话记录中 |
| 5. 过程文档 | `docs/01-需求文档.md`、`docs/02-技术方案.md`、`docs/03-测试用例.md`、`docs/05-前端原型需求.md` | 需求方案、技术方案、测试用例、前端原型需求 |
| 6. 产品效果 | `docs/screenshots/` | 13 张页面截图，覆盖住客端与管理端主链路 |
| 7. 实现思路和技术亮点总结 | `docs/04-实现思路和亮点.md` | 实现思路与亮点；编码过程进度见 `ai-process/编码任务清单.md` |

## 快速启动

**环境要求**：JDK 17+、Maven 3.6+、Node.js 18+ 与 npm、Docker（Docker Desktop 已启动）。

**一键启动**：

```bash
./start.sh
```

脚本依次完成：前置环境检测 → 前端构建 → 产物拷入后端 static → 后端打包 → 启动 MySQL 容器 → 启动应用。

**访问地址**：

- 住客端：http://localhost:8080 （首页房型列表，注册/登录后可预订）
- 前台管理端：http://localhost:8080/#/admin/login

**测试账号**：

- 前台管理员：`admin / admin123`
- 住客：无预置账号，在住客端自行注册

**预置数据**：数据库脚本（`hotel-server/src/main/resources/` 下 `schema.sql` / `data.sql`）随应用启动自动执行，幂等，重启不会重复初始化。预置 1 个管理员、4 个房型、10 个房间，开箱即可演示主链路。

**运行测试**：

```bash
docker compose up -d                      # 需先启动 MySQL
cd hotel-server && mvn test
```

集成测试覆盖接口鉴权、库存防超卖、订单全生命周期、并发场景（防超卖/并发入住/订单号唯一等）。

如需本地开发模式（前后端热更新）：

```bash
docker compose up -d                      # 仅启动 MySQL
cd hotel-server && mvn spring-boot:run    # 后端，8080 端口
cd hotel-web && npm run dev               # 前端，5173 端口，/api 代理到 8080
```

数据库连接可用环境变量覆盖：`MYSQL_HOST`（默认 localhost）、`MYSQL_PORT`（默认 3307）、`MYSQL_USER`、`MYSQL_PASSWORD`。

## 建议阅读顺序

1. 本 README → 2. `docs/01-需求文档.md`（做什么）→ 3. `docs/02-技术方案.md`（怎么做）→ 4. `docs/04-实现思路和亮点.md`（亮点总结）→ 5. `docs/03-测试用例.md`（如何验证）→ 6. `docs/screenshots/` 对照效果 → 7. 按需翻阅 `ai-process/` 下的会话记录与编码任务清单。
