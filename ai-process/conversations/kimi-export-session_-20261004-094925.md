---
session_id: session_99363c90-f285-4a82-91b5-cd4c1c77974b
exported_at: 2026-10-04T09:49:25.268Z
work_dir: /Users/donglongjun/IdeaProjects/hotel-reservation-system
message_count: 182
token_count: 116070
---

# Kimi Session Export

## Overview

- **Topic**: 读取ai-process/编码任务清单.md，执行任务T6 完成后运行对应测试，告诉我结果。不要进入下一个任务。
- **Conversation**: 5 turns | 96 tool calls

---

## Turn 1

### User

读取ai-process/编码任务清单.md，执行任务T6 完成后运行对应测试，告诉我结果。不要进入下一个任务。

### Assistant

<details><summary>Thinking</summary>

Read the task list first.

</details>

#### Tool Call: Read (`ai-process/编码任务清单.md`)
<!-- call_id: tool_mFBDk2SnxHA7ctG6Uvw2Bx5Y -->
```json
{
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Read (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_mFBDk2SnxHA7ctG6Uvw2Bx5Y -->
1	# 编码任务清单（依据 docs/02-技术方案.md v1.1、docs/03-测试用例.md v1.0）
2	
3	> 进度快照：2026-10-04。**T1~T5 已完成并验收**（commit `af5b058`），下一个任务从 **T6** 开始。
4	> 原则：先骨架后业务；后端按依赖序 auth → room → inventory → order；前端放后端可用之后；并发与端到端测试收尾。每个任务达到验收条件再进入下一个。
5	
6	## 进度状态
7	
8	| 任务 | 状态 | 说明 |
9	| --- | --- | --- |
10	| T1 工程脚手架与启动链路 | ✅ 完成 | jar/前端构建通过；docker-compose、start.sh 就绪 |
11	| T2 数据库脚本 | ✅ 完成 | 7 张表 + 预置数据（admin/4 房型/10 房间），幂等，已随启动执行成功 |
12	| T3 后端公共层 | ✅ 完成 | Result/错误码/BizException/全局异常处理/校验工具/AuthInterceptor/WebConfig |
13	| T4 前端骨架 | ✅ 完成 | hash 路由+守卫、request.js、session.js、13 页面占位，构建通过 |
14	| T5 auth 模块 | ✅ 完成 | 注册/登录/登出/me/resolveUser，TC-A02~A10、H02 API 部分已实测通过 |
15	| T6~T12 | ⬜ 待做 | 见下方任务定义 |
16	
17	## 实施过程中的关键偏差（新对话必读）
18	
19	- **MySQL 容器端口映射为 `3307:3306`**（开发机 3306 被本机 MySQL 占用）；`application.yml` 默认连 3307，可用 `MYSQL_PORT` 环境变量覆盖。
20	- **已删除 `MybatisPlusConfig`**：MyBatis-Plus 3.5.9 的分页拦截器拆分到白名单外依赖，且业务不需要分页。
21	- 本机 Java 是 JDK 21，pom 目标版本 17，编译运行正常。
22	- 本地开发态：`docker compose up -d` 起库后，`cd hotel-server && mvn spring-boot:run`；前端 `cd hotel-web && npm run dev`。
23	- admin 预置账号：`admin / admin123`（BCrypt 密文在 data.sql）。
24	
25	---
26	
27	## 第一阶段：工程骨架（已完成 T1~T4）
28	
29	### T1 工程脚手架与启动链路 ✅
30	- 目标：`hotel-server`（Maven/Spring Boot 3/Java 17）、`hotel-web`（Vite/Vue 3/Element Plus）、`docker-compose.yml`、`start.sh`，依赖严格按技术方案 §1.3 白名单。
31	- 验收：`mvn package` 出可运行 jar；`npm run build` 通过、vite dev `/api` 代理 8080；`docker compose up -d` 拉起 MySQL；start.sh 逐项检测环境并友好报错。
32	
33	### T2 数据库脚本 ✅
34	- 目标：7 张表（user、auth_token、room_type、room、hotel_order 含生成列唯一索引、daily_inventory、order_seq）+ 预置数据；幂等（IF NOT EXISTS / INSERT IGNORE）。
35	- 对应测试：TC-F02（幂等）、TC-F01 数据部分。
36	- 验收：重启不报错不重复；手工插入同房间两笔 CHECKED_IN 订单第二笔被 DB 拒绝。
37	
38	### T3 后端公共层 ✅
39	- 目标：Result、错误码枚举（1001 已订满/1002 手机号已注册/1003 不满足入住条件等）、BizException + 全局异常处理器、正则工具、AuthInterceptor、WebConfig。
40	- 对应测试：TC-H01。
41	- 验收：`{code,message,data}` 结构；BizException → 非 0 code + message，HTTP 200；401/403 走拦截器。
42	
43	### T4 前端骨架 ✅
44	- 目标：request.js（X-Token 注入、非 0 code 统一 ElMessage）、session.js（reactive 单例 + localStorage）、hash 路由 + 登录/角色守卫、13 页面占位。
45	- 验收：构建通过；单 jar 托管下各 `/#/...` 路由刷新不 404；未登录访问受保护路由被重定向。
46	
47	---
48	
49	## 第二阶段：后端业务（按依赖序）
50	
51	### T5 auth 模块 ✅（已验收）
52	- 已实现：注册（手机号格式+唯一性、BCrypt）、登录（共用入口按 role）、登出、me、resolveUser；token UUID 落 auth_token 表（7 天过期）；401/403 拦截。
53	- 实测通过：TC-A02、A03、A04、A05、A07、A08、A09、A10、TC-H02。
54	
55	### T6 room 模块（住客侧只读 + 实体/mapper）
56	- **目标**：room_type/room 实体与 mapper；`GET /api/room-types`、`GET /api/room-types/{id}`（住客只读，不含剩余量）；`listAssignableRooms(typeId)` 内部方法（在住订单推导空闲，供 T9 用）。本任务不做 admin 维护接口（放 T9）。
57	- **涉及模块**：room。
58	- **对应测试用例**：TC-F01①、TC-A01①②。
59	- **验收条件**：未登录可调两个只读接口返回预置房型；金额以"分"整数输出（元↔分换算只在 DTO 序列化层）。
60	
61	### T7 inventory 模块（防超卖核心）
62	- **目标**：`InventoryService` 三方法——`queryAvailability`（逐日最小剩余量，无库存行按当前房间数计）、`tryOccupy`（按日期升序原子条件更新，INSERT 撞唯一索引重试一次，失败整单回滚由调用方事务保证）、`release`（逐日 −1 带 >0 保护）。并发集成测试对该接口多线程调用。
63	- **涉及模块**：inventory。
64	- **对应测试用例**：TC-G01 库存断言部分、TC-B01/B02 查询语义。
65	- **验收条件**：N（≥5）线程并发 tryOccupy 同区间，成功数恰为房间数、occupied 永不超 total；queryAvailability 无库存行日期边界正确。
66	
67	### T8 order 模块（住客侧）
68	- **目标**：`OrderNoGenerator`（每日序号表、同事务、允许跳号）；创建订单（BR-01 先校验后占用、金额下单时固化 BigDecimal、一个事务内逐日占用+插单）；我的订单/详情（按 token user_id 过滤，忽略前端 userId）；取消（带源状态+user_id 条件更新，成功后 release 库存）。
69	- **涉及模块**：order（依赖 inventory、room）。
70	- **对应测试用例**：TC-B03~B13、TC-C01~C05、TC-A06②、TC-H03、TC-G07；并发 TC-G02、G03。
71	- **验收条件**：非法请求拒绝且不占库存（TC-B13）；中途订满整单回滚无残留（TC-B12）；金额分单位且调价不回溯（TC-B09/B10）；20 线程订单号全局唯一（TC-G07）；越权查/取消被拒（TC-A06②/C05）。
72	
73	### T9 order 前台侧 + room 维护接口
74	- **目标**：admin 订单查询（订单号/guest_phone/空查全部）、详情、assignable-rooms；办理入住（BR-06 四项校验、房间行锁+锁内复查+状态条件更新）；办理退房（条件更新+逐日释放）；房型/房间维护（UC-09："新房间数 ≥ 有效订单数"校验、未来库存行 total 同步、改挂双方校验）。
75	- **涉及模块**：order、room、inventory。
76	- **对应测试用例**：TC-D01~D09、TC-E01~E05、TC-F03~F08；并发 TC-G04、G05、G06。
77	- **验收条件**：入住校验文案与原型一致；并发分房仅一笔成功（TC-G04）；提前退房剩余晚数可再售且金额不变（TC-E02/E03）；减房低于有效订单数被拒（TC-F07）；改挂双方库存同步（TC-F08）。
78	
79	---
80	
81	## 第三阶段：前端页面
82	
83	### T10 住客端页面（P-C1~C8）
84	- **目标**：房型列表（日期区间剩余量、不可订置灰）、房型详情、登录、注册、预订确认、预订结果（成功/失败态）、我的订单、订单详情（取消+二次确认）。接口全走 `src/api/`。
85	- **涉及模块**：hotel-web views/guest、api/。
86	- **对应测试用例**：TC-A01~A06、TC-B01~B11、TC-C01~C03 的 UI 部分。
87	- **验收条件**：游客浏览+预订引导登录（TC-A01）；订满跳失败页提示"该房型所选日期已订满"（TC-B11）；金额页面展示元；已入住订单无取消入口（TC-C03①）。
88	
89	### T11 前台管理端页面（P-A1~A5）
90	- **目标**：admin 登录、订单查询列表、订单详情（入住办理：身份证+房间下拉仅可分配房间；退房+二次确认；按状态显隐操作区）、房型维护、房间维护。
91	- **涉及模块**：hotel-web views/admin、api/admin.js。
92	- **对应测试用例**：TC-D01~D09、TC-E01~E05、TC-F03~F08 的 UI 部分。
93	- **验收条件**：非已确认订单无入住操作区（TC-D06①）；下拉不含在住房间（TC-D08）；表单校验文案与原型一致（TC-F05/F06）。
94	
95	---
96	
97	## 第四阶段：联调收尾
98	
99	### T12 端到端联调、并发用例与交付验证
100	- **目标**：start.sh 全链路实测；并发测试补齐（优先后端集成测试，断言 DB 终态）；README（前置条件、admin 明文密码、启动方式）；主链路 UI 冒烟。
101	- **涉及模块**：全工程。
102	- **对应测试用例**：TC-F01、TC-F02、TC-G01~G07 全量、TC-H01、TC-H05。
103	- **验收条件**：全新环境 `./start.sh` 一键起系统，TC-H05 全流程无阻断；TC-G01~G07 全部通过；docs/03 覆盖对照 AC-01~21 无遗漏。
104	
105	---
106	
107	## 依赖关系
108	
109	```
110	T1 → T2 → T3 → T4（骨架）
111	T3 → T5(auth) → T6(room 只读) → T7(inventory) → T8(order 住客侧) → T9(order 前台侧 + room 维护)
112	T4 + T5~T8 → T10（住客端页面）；T4 + T9 → T11（管理端页面）；T10 + T11 → T12（联调收尾）
113	```

</details>

<details><summary>Thinking</summary>

T6: room module — room_type/room entities and mappers; GET /api/room-types, GET /api/room-types/{id} (guest read-only, no availability); listAssignableRooms(typeId) internal method (derived from checked-in orders, for T9). Tests: TC-F01①, TC-A01①②. Acceptance: unauthenticated can call both endpoints; amounts in cents integer.

Let me explore the existing server structure. This is a moderate task — I can do it directly. Let me look at the project structure first.

</details>

#### Tool Call: Bash (`find hotel-server/src -type f -name "*.java" | sort`)
<!-- call_id: tool_Cu2olNchbpUmk3ybCfEuIB9l -->
```json
{
  "command": "find hotel-server/src -type f -name \"*.java\" | sort"
}
```

#### Tool Call: Read (`docs/02-技术方案.md`)
<!-- call_id: tool_3z9VkNAg4e3JZojkJTQvLLbk -->
```json
{
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Bash (`find hotel-server/src -type f -name "*.java" | sort`)</summary>

<!-- call_id: tool_Cu2olNchbpUmk3ybCfEuIB9l -->
hotel-server/src/main/java/com/example/hotel/auth/AuthController.java
hotel-server/src/main/java/com/example/hotel/auth/AuthService.java
hotel-server/src/main/java/com/example/hotel/auth/dto/LoginRequest.java
hotel-server/src/main/java/com/example/hotel/auth/dto/LoginResponse.java
hotel-server/src/main/java/com/example/hotel/auth/dto/RegisterRequest.java
hotel-server/src/main/java/com/example/hotel/auth/dto/UserInfo.java
hotel-server/src/main/java/com/example/hotel/auth/entity/AuthToken.java
hotel-server/src/main/java/com/example/hotel/auth/entity/User.java
hotel-server/src/main/java/com/example/hotel/auth/mapper/AuthTokenMapper.java
hotel-server/src/main/java/com/example/hotel/auth/mapper/UserMapper.java
hotel-server/src/main/java/com/example/hotel/common/api/ErrorCode.java
hotel-server/src/main/java/com/example/hotel/common/api/Result.java
hotel-server/src/main/java/com/example/hotel/common/context/LoginUser.java
hotel-server/src/main/java/com/example/hotel/common/context/UserContext.java
hotel-server/src/main/java/com/example/hotel/common/exception/BizException.java
hotel-server/src/main/java/com/example/hotel/common/exception/GlobalExceptionHandler.java
hotel-server/src/main/java/com/example/hotel/common/util/Validators.java
hotel-server/src/main/java/com/example/hotel/config/AuthInterceptor.java
hotel-server/src/main/java/com/example/hotel/config/WebConfig.java
hotel-server/src/main/java/com/example/hotel/HotelApplication.java


</details>

<details><summary>Tool Result: Read (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_3z9VkNAg4e3JZojkJTQvLLbk -->
1	# 酒店预订系统 技术方案
2	
3	| 项目 | 内容 |
4	| --- | --- |
5	| 文档版本 | v1.1 |
6	| 文档日期 | 2026-10-04 |
7	| 文档状态 | 已定稿 |
8	| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |
9	| 输入 | 已确认的 8 项技术决策 + 4 项补充确认（见 §1.2、§1.3） |
10	| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全） |
11	
12	---
13	
14	## 1. 技术选型
15	
16	### 1.1 已确认决策（不再变更）
17	
18	| # | 决策项 | 结论 |
19	| --- | --- | --- |
20	| 1 | 后端技术栈 | Spring Boot 3 + Java 17 |
21	| 2 | 前端技术栈 | Vue 3 + Vite + Element Plus |
22	| 3 | 数据库 | MySQL 8 + docker-compose 启动 |
23	| 4 | 数据访问层 | MyBatis-Plus |
24	| 5 | 防超卖 | 逐日库存表 + 原子条件更新（`UPDATE ... SET occupied = occupied + 1 WHERE occupied < total`） |
25	| 6 | 登录认证 | UUID token + 服务端存储（不用 JWT/Session） |
26	| 7 | 交付/一键运行 | 前端构建产物由后端托管，单 jar + start.sh；保留 dev 模式 |
27	| 8 | 订单号 | 数据库每日序号表（`HR + 下单日期 + 当日序号`） |
28	
29	无争议默认项：金额用 BigDecimal / DECIMAL；密码 BCrypt；身份证/手机号正则校验；订单状态用枚举 + 服务端校验。
30	
31	### 1.2 本阶段补充确认（需求未覆盖、决策未定的 4 个遗漏点）
32	
33	| # | 遗漏点 | 结论 |
34	| --- | --- | --- |
35	| 9 | 建表与预置数据初始化 | `schema.sql` + `data.sql`，Spring Boot 启动时自动执行，不引入 Flyway |
36	| 10 | BCrypt 依赖来源 | 引入 `spring-security-crypto`（仅加密小工具包，不引入整个 Spring Security） |
37	| 11 | 账号体系 | 单 `user` 表 + `role` 字段（住客自助注册、admin 预置，共用认证链路） |
38	| 12 | token 存储 | 存数据库表（重启不掉线，与"服务端存储"决策一致） |
39	
40	### 1.3 依赖清单（全量，此外不引入）
41	
42	**后端（Maven）**：`spring-boot-starter-web`、`spring-boot-starter-validation`、`mybatis-plus-spring-boot3-starter`、`mysql-connector-j`、`spring-security-crypto`（决策 10）、`spring-boot-starter-test`（测试）。
43	
44	**前端（npm）**：`vue`、`vue-router`、`element-plus`、`axios`、`vite`、`@vitejs/plugin-vue`。不引入 Pinia——登录态等少量全局状态用 Vue 原生 `reactive` 单例模块承载，对演示规模足够。
45	
46	---
47	
48	## 2. 总体架构
49	
50	```
51	┌─────────────────────────────────────────────────────┐
52	│  浏览器                                               │
53	│   住客端页面（P-C1~C8）   前台管理端页面（P-A1~A5）      │
54	└──────────────────────┬──────────────────────────────┘
55	                       │ HTTP / JSON（/api/**）
56	┌──────────────────────▼──────────────────────────────┐
57	│  Spring Boot 单应用（单 jar）                          │
58	│   ├─ 静态托管前端构建产物（决策 7）                     │
59	│   └─ 业务模块：auth / room / inventory / order        │
60	└──────────────────────┬──────────────────────────────┘
61	                       │ JDBC
62	┌──────────────────────▼──────────────────────────────┐
63	│  MySQL 8（docker-compose 容器）                        │
64	└─────────────────────────────────────────────────────┘
65	```
66	
67	- **一个工程、两端页面**：前端是单页应用，住客端与前台管理端是同一份构建产物里的两组路由（按路径前缀区分，见 §3.3），不拆成两个前端工程。
68	- **dev 模式**：`vite dev`（默认 5173 端口）通过 proxy 把 `/api` 转发到 `localhost:8080`，前后端分开热更新开发。
69	- **交付模式**：`vite build` 产物拷入后端 `static/` 目录，单 jar 启动后 `http://localhost:8080` 即可访问完整系统。**前端路由使用 hash 模式**（`/#/room/1`），静态托管下刷新任意路径都落在 index.html 上，无需后端 fallback 配置。
70	
71	### 2.1 一键启动流程（start.sh 负责完整构建并启动）
72	
73	1. `npm run build` 构建前端（`hotel-web/`）；
74	2. 前端产物复制到 `hotel-server/src/main/resources/static/`；
75	3. `mvn package` 打出单 jar；
76	4. `docker compose up -d` 启动 MySQL 容器；
77	5. 轮询等待 MySQL 就绪（`mysqladmin ping` 或等价探测）；
78	6. `java -jar hotel-server.jar` 启动应用；`schema.sql` + `data.sql` 完成建表与预置数据（房型 3~4 种、每房型若干房间、admin 账号，满足 AC-20），脚本幂等可重复启动（见 §4.2）。
79	
80	> 前置条件：演示机器需预装 JDK 17、Maven、Node.js、Docker，start.sh 逐项检测并给出友好报错。admin 预置账号的明文密码写入 README 与 `data.sql` 注释，供演示登录使用。
81	
82	---
83	
84	## 3. 代码结构
85	
86	### 3.1 仓库总体布局
87	
88	```
89	hotel-reservation-system/
90	├── hotel-server/            # 后端（Maven）
91	├── hotel-web/               # 前端（Vite）
92	├── docker-compose.yml       # MySQL 8
93	├── start.sh                 # 一键构建并启动（见 §2.1）
94	├── docs/
95	└── ai-process/
96	```
97	
98	### 3.2 后端目录结构与分层
99	
100	按**业务模块分包**（package by feature），每个模块内部自带 `controller / service / mapper / entity` 四层，避免按层分包导致的"改一个功能动四个包"：
101	
102	```
103	hotel-server/
104	├── pom.xml
105	├── src/main/java/com/example/hotel/
106	│   ├── HotelApplication.java
107	│   ├── common/                      # 跨模块公共层
108	│   │   ├── api/                     #   统一响应体 Result、错误码枚举
109	│   │   ├── exception/               #   业务异常 BizException + 全局异常处理器
110	│   │   └── util/                    #   手机号/身份证正则等校验工具
111	│   ├── config/                      # 全局配置
112	│   │   ├── AuthInterceptor.java     #   token 解析与角色鉴权
113	│   │   ├── WebConfig.java           #   拦截器注册、CORS（dev 模式）
114	│   │   └── MybatisPlusConfig.java
115	│   ├── auth/                        # 认证模块（§3.4）
116	│   │   ├── AuthController.java
117	│   │   ├── AuthService.java         #   模块接口
118	│   │   ├── entity/  mapper/  dto/
119	│   ├── room/                        # 房型与房间模块
120	│   │   ├── RoomTypeController.java  #   含住客侧只读查询
121	│   │   ├── RoomAdminController.java #   前台维护接口
122	│   │   ├── RoomService.java         #   模块接口
123	│   │   ├── entity/  mapper/  dto/
124	│   ├── inventory/                   # 库存模块（不暴露 controller）
125	│   │   ├── InventoryService.java    #   模块接口（深模块，见 §3.4）
126	│   │   ├── entity/  mapper/
127	│   └── order/                       # 订单模块
128	│       ├── OrderController.java     #   住客侧
129	│       ├── OrderAdminController.java#   前台侧（查询/入住/退房）
130	│       ├── OrderService.java        #   模块接口
131	│       ├── OrderNoGenerator.java    #   订单号生成（内部实现细节）
132	│       ├── entity/  mapper/  dto/
133	└── src/main/resources/
134	    ├── application.yml
135	    ├── schema.sql                   # 建表（决策 9）
136	    └── data.sql                     # 预置房型/房间/admin（决策 9）
137	```
138	
139	**分层职责与调用约束**：
140	
141	- `controller`：参数格式校验（`@Validated`）、鉴权注解/拦截器配合、调用模块接口、组装响应。不写业务逻辑。
142	- `service`：模块接口所在地，承载全部业务规则与事务边界（含业务规则校验，如预订日期规则 BR-01）。controller 只依赖本模块 service；跨模块调用只允许 `order → inventory`、`order → room`（见 §3.4 依赖图）。
143	- `mapper`：MyBatis-Plus 接口，仅被本模块 service 使用，不跨模块暴露。
144	- `entity / dto`：entity 对应表结构；dto 为接口出入参，controller 不直接向外暴露 entity。
145	
146	### 3.3 前端目录结构
147	
148	```
149	hotel-web/
150	├── package.json
151	├── vite.config.js                 # dev proxy：/api → localhost:8080
152	├── index.html
153	└── src/
154	    ├── main.js
155	    ├── App.vue
156	    ├── api/
157	    │   ├── request.js             # axios 实例：token 注入、统一错误处理
158	    │   ├── auth.js  room.js  order.js  admin.js
159	    ├── router/
160	    │   └── index.js               # hash 模式路由 + 登录/角色守卫
161	    ├── stores/
162	    │   └── session.js             # reactive 单例：当前用户、token
163	    ├── views/
164	    │   ├── guest/                 # 住客端（路径 /，/room/:id，/login 等）
165	    │   │   ├── RoomListView.vue        # P-C1
166	    │   │   ├── RoomDetailView.vue      # P-C2
167	    │   │   ├── LoginView.vue           # P-C3
168	    │   │   ├── RegisterView.vue        # P-C4
169	    │   │   ├── BookingConfirmView.vue  # P-C5
170	    │   │   ├── BookingResultView.vue   # P-C6
171	    │   │   ├── MyOrdersView.vue        # P-C7
172	    │   │   └── OrderDetailView.vue     # P-C8
173	    │   └── admin/                 # 前台管理端（路径前缀 /admin）
174	    │       ├── AdminLoginView.vue      # P-A1
175	    │       ├── AdminOrdersView.vue     # P-A2
176	    │       ├── AdminOrderDetailView.vue# P-A3
177	    │       ├── AdminRoomTypesView.vue  # P-A4
178	    │       └── AdminRoomsView.vue      # P-A5
179	    └── components/                # 少量公共组件（房型卡片、订单状态标签等）
180	```
181	
182	页面与路由一一对应原型文档的 P-C1~C8、P-A1~A5，命名直接可溯。
183	
184	### 3.4 后端模块划分（深模块设计）
185	
186	模块划分遵循 deep module 原则：**小接口、深实现**。调用方和测试只跨越模块接口（seam），接口背后的复杂机制不外泄。
187	
188	| 模块 | 模块接口（对外可见的全部） | 接口背后隐藏的复杂度 |
189	| --- | --- | --- |
190	| **auth** | `register` / `login` / `logout` / `resolveUser(token)` | BCrypt 校验、token 生成与过期、角色判定、住客与 admin 单表共存 |
191	| **room** | 房型/房间查询、房型/房间维护、 `listAssignableRooms(typeId)` | 房间状态不入库、由在住订单推导；维护时"房间数 ≥ 有效订单数"校验（UC-09）；房间数变更同步未来库存行总数 |
192	| **inventory** | `queryAvailability(typeId, checkin, checkout)` / `tryOccupy(...)` / `release(...)` | 逐日库存行的创建与并发原子占用、剩余量取逐日最小值（BR-02）、取消/退房释放 |
193	| **order** | 创建/取消/我的订单/详情（住客侧）；按号或手机号查询/办理入住/办理退房（前台侧） | 订单号生成、金额固化、状态机校验、入住窗口校验、身份证登记、房间分配 |
194	
195	**依赖方向**（单向，无环）：
196	
197	```
198	controller 层  →  本模块 service
199	order service  →  inventory service（占/释库存）
200	order service  →  room service（查可分配房间）
201	auth           →  被拦截器依赖（resolveUser），不依赖其他业务模块
202	inventory      →  不依赖 order（释放库存由 order 调 release 触发）
203	```
204	
205	**模块深度说明**：
206	
207	- **inventory 是最深的模块**：接口只有 3 个方法，但防超卖这一全系统唯一的并发硬规则（BR-02 / AC-08）完全封闭在内。订单模块不需要知道"逐日库存表"的存在，只面对"试着占用，占不到就失败"的语义。seam 在 `InventoryService` 接口，MyBatis-Plus mapper 是该 seam 的 adapter；并发测试直接对着这个接口做。
208	- **order 模块的状态机**：状态流转表（PRD §6.2）收敛为 `OrderService` 内部的转移校验，对外只有 `cancel / checkIn / checkOut` 三个动作方法，非法流转一律在模块内拒绝——controller 不出现状态判断分支。
209	- **不做假 seam**：四个模块各只有一个实现、没有第二个 adapter，因此模块间用普通类注入而非"接口 + Impl + 配置切换"的形式化抽象。`AuthService` 等定义为类即接口，不为抽象而抽象。
210	- **删除测试**：若删除 inventory 模块，逐日计算、并发占用、释放逻辑会散落到 order 和 room 的每个调用点——它确实在挣钱，不是透传层。
211	
212	### 3.5 前后端交互方式
213	
214	- **协议**：REST + JSON，全部业务接口挂在 `/api` 前缀下；前端构建产物由后端静态托管，同源部署无 CORS 问题（CORS 仅在 dev 模式放开）。
215	- **认证传递**：登录成功后后端返回 token，前端存入 `localStorage`，之后每次请求由 axios 拦截器放入请求头 `X-Token: <uuid>`；后端 `AuthInterceptor` 解析 token、注入当前用户，并按路径规则做角色鉴权（`/api/admin/**` 仅 admin 可用）。
216	- **统一响应体**：所有接口返回 `Result<T>`：
217	
218	  ```json
219	  { "code": 0, "message": "ok", "data": { } }
220	  ```
221	
222	  `code = 0` 成功；非 0 为业务错误码（错误码枚举集中在 `common/api`，如 `1001` 已订满、`1002` 手机号已注册、`1003` 不满足入住条件）；完整错误码表在编码阶段补全，前端一律以 `message` 为展示准绳，不对 code 做分支逻辑（预订失败跳结果页等少数流程除外）。HTTP 状态码仅用 200/401/403/500 表达传输层语义。
223	- **错误呈现**：`request.js` 统一拦截非 0 code，用 Element Plus `ElMessage` 弹出后端 `message`——后端业务异常的 message 直接采用原型文档中的提示文案（如"该房型所选日期已订满"），保证演示讲解与原型一致；页面可自行捕获做定制展示（如预订失败跳 P-C6）。
224	- **日期与金额的接口格式**：日期用 `yyyy-MM-dd` 字符串（LocalDate）。金额在前后端接口间一律以**"分"为单位的整数**传输（如 288.50 元 ↔ `28850`），避免 JS 浮点数精度问题；后端内部计算仍用 BigDecimal（单位元，DB 存 DECIMAL(10,2)），元 ↔ 分的换算收敛在 DTO 序列化层一处完成；前端展示时 `分 ÷ 100` 格式化即可。
225	
226	---
227	
228	## 4. 数据库设计
229	
230	字符集 utf8mb4，InnoDB。表名避开关键字（订单表用 `hotel_order`）。
231	
232	### 4.1 表清单
233	
234	| 表 | 用途 | 关键字段与约束 |
235	| --- | --- | --- |
236	| `user` | 住客与 admin 账号（决策 11） | `login_name` 唯一（住客=手机号，admin="admin"）、`password_hash`（BCrypt）、`role`（GUEST/ADMIN）、`created_at` |
237	| `auth_token` | 登录 token（决策 6、12） | `token`（UUID）主键、`user_id`、`expires_at`、`created_at` |
238	| `room_type` | 房型 | `name`、`price` DECIMAL(10,2)、`description`、`created_at`；不提供删除（PRD 无下线需求） |
239	| `room` | 物理房间 | `room_no` 唯一、`room_type_id`、`created_at`；**无状态字段**，"空闲/入住中"由在住订单推导 |
240	| `hotel_order` | 订单 | `order_no` 唯一、`user_id`（下单账号）、`guest_name` + `guest_phone`（住客信息，支持代订 BR-01）、`room_type_id`、`room_id`（入住前为 NULL）、`id_card`（入住前为 NULL）、`checkin_date`、`checkout_date`、`nights`、`amount` DECIMAL(10,2)（下单时固化 BR-03）、`status` 枚举字符串、`created_at`（下单时间，P-C8/P-A2 展示用）、`updated_at`；生成列 `active_room_id = IF(status='CHECKED_IN', room_id, NULL)` 加唯一索引，DB 级保证同一房间最多一笔已入住订单（BR-05，MySQL 唯一索引允许多个 NULL） |
241	| `daily_inventory` | 逐日库存（决策 5） | `room_type_id` + `stay_date` 唯一索引、`total_count`、`occupied_count` |
242	| `order_seq` | 订单号每日序号（决策 8） | `seq_date` 主键、`current_value` |
243	
244	### 4.2 关键设计说明
245	
246	- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。占用库存时从 `room` 表取当前房间总数写入新行；UC-09 维护房间（增/减/改挂）时，先校验"新房间数 ≥ 当前有效订单数"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步。**
247	- **房间状态推导**：某房型当前空闲房间 = 该房型全部房间 − 被状态为"已入住"订单占用的房间。满足 BR-05"同一房间同一时间只分配给一笔已入住订单"，由"分配时房间行锁 + 生成列唯一索引"双重保证（见 §4.1、§5.3），也让 UC-09 的房间维护不受"房间状态"字段拖累。
248	- **订单状态**：Java 枚举 `CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED`，DB 存字符串，便于 SQL 直读与演示讲解。
249	- **初始化脚本幂等**：`schema.sql` 一律 `CREATE TABLE IF NOT EXISTS`，`data.sql` 用 `INSERT IGNORE`（或等价写法），应用重复启动不报错、预置数据不重复插入。
250	
251	---
252	
253	## 5. 核心机制设计
254	
255	### 5.1 防超卖（BR-02 / AC-08）
256	
257	创建订单先由 service 层做**业务规则校验**（BR-01：入住日期 ≥ 当天、离店日期 > 入住日期、住客姓名非空、手机号格式合法），校验不通过直接拒绝，不进入库存占用。
258	
259	校验通过后在**一个事务**内，对日期区间 `[checkin, checkout)` 的每一天**按日期升序**执行（固定加锁顺序，降低并发事务死锁概率）：
260	
261	1. 原子条件更新：
262	   `UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count`
263	2. 影响行数 = 1 → 该日占用成功；= 0 → 该行可能不存在（首次预订该日），尝试 `INSERT`（`total_count` 取当前房间数）；INSERT 撞唯一索引说明并发已建行，回到第 1 步重试一次。
264	3. 重试后仍为 0 → 当日已订满，**抛业务异常、整单回滚**（已占用的日期行随事务一并回滚），返回"该房型所选日期已订满"。
265	4. 全部日期占用成功 → 插入订单，提交事务。
266	
267	并发正确性由两点保证：`occupied_count < total_count` 的条件更新在 MySQL 行锁下是原子的（不超卖）；`(room_type_id, stay_date)` 唯一索引防止并发建行产生重复行。
268	
269	查询剩余量（无需事务）：区间内每日 `total_count − occupied_count` 的最小值；无库存行的日期按 `total_count = 当前房间数, occupied = 0` 计。
270	
271	取消（已确认 → 已取消）与提前退房（已入住 → 已完成）时，对订单区间每日执行 `occupied_count − 1`（带 `occupied_count > 0` 保护，同样按日期升序）。退房时剩余晚数立即可再售，正好由"逐日释放"天然满足（BR-07）。
272	
273	### 5.2 订单号生成（BR-08 / 决策 8）
274	
275	`OrderNoGenerator`（order 模块内部实现，不对外）：
276	
277	1. 事务内对 `order_seq` 当日行执行 `UPDATE ... SET current_value = current_value + 1 WHERE seq_date = 今天`，影响 0 行则 INSERT 初始行（撞主键则重试 UPDATE）；
278	2. 格式化 `HR + yyyyMMdd + '-' + 序号补零4位`，如 `HR20261003-0001`；
279	3. 与订单插入同一事务，序号不回滚复用（允许跳号，唯一性优先）。
280	
281	### 5.3 状态流转与条件更新
282	
283	所有状态变更用**带源状态条件的 UPDATE** 实现，杜绝"先查后改"的并发缝隙：
284	
285	- 取消：`UPDATE hotel_order SET status='CANCELLED' WHERE order_no=? AND status='CONFIRMED' AND user_id=?`，影响 0 行 → "订单状态已变更，请刷新查看"。
286	- 办理入住（一个事务内，按序执行）：
287	  1. BR-06 四项校验：订单状态 = 已确认、入住窗口 `入住日 ≤ 今天 < 离店日`、身份证 18 位格式、该房型有空闲房间；
288	  2. 对选中的房间行加行锁 `SELECT * FROM room WHERE id=? FOR UPDATE`——这是防止"同一房间被两笔订单并发分配"的关键：并发事务在此串行化；
289	  3. 锁内复查该房间无在住订单（`SELECT COUNT(*) FROM hotel_order WHERE room_id=? AND status='CHECKED_IN'`，> 0 则抛业务异常回滚，提示"房间刚被分配，请刷新重选"）；
290	  4. `UPDATE hotel_order SET status='CHECKED_IN', room_id=?, id_card=? WHERE id=? AND status='CONFIRMED'`，影响 0 行 → 订单状态已变更，回滚。
291	
292	  兜底：`hotel_order` 上的生成列唯一索引（§4.1）把"同一房间最多一笔已入住订单"固化为数据库约束，代码路径即使失守也写不进去。
293	- 办理退房：`UPDATE ... SET status='COMPLETED' WHERE order_no=? AND status='CHECKED_IN'`，随后释放库存（§5.1）。
294	
295	终态（已完成/已取消）无任何出口流转，与 PRD §6.2 一致。
296	
297	### 5.4 认证与鉴权
298	
299	- 注册：校验手机号格式与唯一性，BCrypt 加密入库。
300	- 登录：按 `login_name` 查用户、BCrypt 比对，成功则生成 UUID token 写入 `auth_token`（含过期时间，如 7 天），返回 token 与角色。
301	- `AuthInterceptor`：从 `X-Token` 解析用户并放入请求上下文；未登录访问需登录接口 → 401；住客访问 `/api/admin/**` → 403（AC-04）。
302	- 住客"我的订单"一律以 token 中的 `user_id` 过滤，不接受前端传 userId（AC-03）。
303	
304	---
305	
306	## 6. API 接口清单
307	
308	### 6.1 住客侧（`/api`，部分公开）
309	
310	| 方法 | 路径 | 说明 | 登录 |
311	| --- | --- | --- | --- |
312	| POST | `/api/auth/register` | 住客注册 | 否 |
313	| POST | `/api/auth/login` | 登录（住客/前台共用入口，按 role 区分） | 否 |
314	| POST | `/api/auth/logout` | 登出 | 是 |
315	| GET | `/api/auth/me` | 当前登录用户信息 | 是 |
316	| GET | `/api/room-types` | 房型列表（P-C1） | 否 |
317	| GET | `/api/room-types/{id}` | 房型详情（P-C2） | 否 |
318	| GET | `/api/room-types/availability?checkin=&checkout=` | 各房型日期区间剩余量 | 否 |
319	| POST | `/api/orders` | 创建预订（P-C5） | 住客 |
320	| GET | `/api/orders/mine` | 我的订单列表（P-C7） | 住客 |
321	| GET | `/api/orders/{orderNo}` | 我的订单详情（P-C8） | 住客（仅本人） |
322	| POST | `/api/orders/{orderNo}/cancel` | 取消订单 | 住客（仅本人） |
323	
324	### 6.2 前台管理侧（`/api/admin`，仅 ADMIN 角色）
325	
326	| 方法 | 路径 | 说明 |
327	| --- | --- | --- |
328	| GET | `/api/admin/orders?keyword=` | 订单查询：按订单号或住客手机号（P-A2）；keyword 为空返回全部 |
329	| GET | `/api/admin/orders/{orderNo}` | 订单详情（P-A3） |
330	| GET | `/api/admin/orders/{orderNo}/assignable-rooms` | 该订单房型当前可分配房间列表 |
331	| POST | `/api/admin/orders/{orderNo}/check-in` | 办理入住（入参：身份证号、房间号） |
332	| POST | `/api/admin/orders/{orderNo}/check-out` | 办理退房 |
333	| GET/POST/PUT | `/api/admin/room-types` | 房型列表/新增/修改（P-A4，UC-09） |
334	| GET/POST/PUT | `/api/admin/rooms` | 房间列表/新增/修改（P-A5，UC-09） |
335	
336	---
337	
338	## 7. 与需求的覆盖核对
339	
340	| 需求项 | 落点 |
341	| --- | --- |
342	| BR-01 预订规则 / 代订 | §5.1 创建订单 service 层业务校验；`hotel_order` 同时存 `user_id` 与 `guest_name/guest_phone`；前台按订单上的 `guest_phone` 查询 |
343	| BR-02 防超卖 / AC-08 | §5.1 逐日库存 + 原子条件更新 |
344	| BR-03 金额固化 | 下单时计算写入 `hotel_order.amount`，房型调价不回溯 |
345	| BR-04 宽松取消 | §5.3 条件更新 + 库存释放 |
346	| BR-05 房间分配 | `room` 无状态字段 + 入住时分配（§4.2、§5.3） |
347	| BR-06 入住窗口校验 | §5.3 办理入住四项校验，提示文案对齐原型 P-A3 |
348	| BR-07 提前退房不退差价、剩余晚数可再售 | §5.1 逐日释放天然满足 |
349	| BR-08 订单号 | §5.2 每日序号表 |
350	| UC-09 维护约束 | room 模块"新房间数 ≥ 有效订单数"校验 + 未来库存行同步（§4.2） |
351	| AC-20 预置数据 | `data.sql`：admin、3~4 个房型、每房型若干房间 |
352	| 已知简化（超期未入住不自动关闭） | 与 PRD BR-06 补充一致，不做定时任务 |
353	
354	---
355	
356	## 8. 风险与已知简化
357	
358	| 项 | 说明 | 应对 |
359	| --- | --- | --- |
360	| 演示机需 Docker / JDK / Maven / Node | 决策 3、7 的代价 | README 写明前置条件；start.sh 逐项检测并给出友好报错 |
361	| 超期未退房订单悬挂 | 订单"已入住"但过了离店日仍未退房时，房间与库存一直被占（PRD 只豁免了超期未入住，未覆盖此项） | 演示中由前台人工退房兜底；口头说明生产版应有定时任务自动退房 |
362	| 逐日库存行随时间累积 | 每房型每日一行，演示规模（几百订单）无压力 | 不清理；口头说明生产版可归档 |
363	| token 长期有效无踢出 | 演示从简 | `auth_token` 有过期字段，过期即 401 重新登录 |
364	| 超期未入住订单不自动关闭 | PRD 明确的已知简化 | 面试被问及按 PRD 口径回答 |
365	
366	---
367	
368	## 9. 后续阶段衔接
369	
370	- 测试用例（`docs/03-测试用例.md`）：按 PRD §7 验收标准逐条展开，防超卖（AC-08）以 inventory 模块接口为测试面做并发用例。
371	- 编码顺序建议：建表与预置数据 → auth → room（含预置数据可读）→ inventory → order → 前端页面按主链路（预订 → 我的订单 → 前台入住/退房）推进，P1 的房型/房间维护最后做。

</details>

<details><summary>Thinking</summary>

Now let me look at existing code: AuthController, AuthService, entity patterns, ErrorCode, Result, WebConfig, AuthInterceptor, schema.sql, and test structure. Also check docs/03 test cases TC-F01, TC-A01.

</details>

我先查看现有代码结构和相关测试用例，再实现 T6。

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/auth/AuthContro…`)
<!-- call_id: tool_kgKzb80eEF0nPt8vp2PPHL8J -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/auth/AuthController.java"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/auth/entity/Use…`)
<!-- call_id: tool_JQXJCbwvzv09T3SwjYqXzYfL -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/auth/entity/User.java"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/config/WebConfi…`)
<!-- call_id: tool_LV01XWykmfUiFE55eQS8u83v -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/config/WebConfig.java"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/config/AuthInte…`)
<!-- call_id: tool_0Mutv9cgynQBCpXdnoBvlrER -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/config/AuthInterceptor.java"
}
```

#### Tool Call: Grep (`docs/03-测试用例.md`)
<!-- call_id: tool_mJfSUpSnqN0J8WOvOBF6GLwN -->
```json
{
  "pattern": "TC-F01|TC-A01",
  "path": "docs/03-测试用例.md",
  "output_mode": "content",
  "-C": 6
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/auth/AuthContro…`)</summary>

<!-- call_id: tool_kgKzb80eEF0nPt8vp2PPHL8J -->
1	package com.example.hotel.auth;
2	
3	import com.example.hotel.auth.dto.LoginRequest;
4	import com.example.hotel.auth.dto.LoginResponse;
5	import com.example.hotel.auth.dto.RegisterRequest;
6	import com.example.hotel.auth.dto.UserInfo;
7	import com.example.hotel.common.api.Result;
8	import jakarta.servlet.http.HttpServletRequest;
9	import jakarta.validation.Valid;
10	import org.springframework.web.bind.annotation.GetMapping;
11	import org.springframework.web.bind.annotation.PostMapping;
12	import org.springframework.web.bind.annotation.RequestBody;
13	import org.springframework.web.bind.annotation.RequestMapping;
14	import org.springframework.web.bind.annotation.RestController;
15	
16	@RestController
17	@RequestMapping("/api/auth")
18	public class AuthController {
19	
20	    private final AuthService authService;
21	
22	    public AuthController(AuthService authService) {
23	        this.authService = authService;
24	    }
25	
26	    @PostMapping("/register")
27	    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
28	        authService.register(request);
29	        return Result.ok();
30	    }
31	
32	    @PostMapping("/login")
33	    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
34	        return Result.ok(authService.login(request));
35	    }
36	
37	    @PostMapping("/logout")
38	    public Result<Void> logout(HttpServletRequest request) {
39	        authService.logout(request.getHeader("X-Token"));
40	        return Result.ok();
41	    }
42	
43	    @GetMapping("/me")
44	    public Result<UserInfo> me() {
45	        return Result.ok(authService.currentUser());
46	    }
47	}

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/config/AuthInte…`)</summary>

<!-- call_id: tool_0Mutv9cgynQBCpXdnoBvlrER -->
1	package com.example.hotel.config;
2	
3	import com.example.hotel.auth.AuthService;
4	import com.example.hotel.common.context.LoginUser;
5	import com.example.hotel.common.context.UserContext;
6	import jakarta.servlet.http.HttpServletRequest;
7	import jakarta.servlet.http.HttpServletResponse;
8	import org.springframework.http.HttpMethod;
9	import org.springframework.stereotype.Component;
10	import org.springframework.web.servlet.HandlerInterceptor;
11	
12	import java.io.IOException;
13	import java.nio.charset.StandardCharsets;
14	
15	@Component
16	public class AuthInterceptor implements HandlerInterceptor {
17	
18	    private final AuthService authService;
19	
20	    public AuthInterceptor(AuthService authService) {
21	        this.authService = authService;
22	    }
23	
24	    @Override
25	    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
26	        String path = request.getRequestURI();
27	        if (isPublic(request, path)) {
28	            return true;
29	        }
30	        String token = request.getHeader("X-Token");
31	        LoginUser user = token != null && !token.isBlank() ? authService.resolveUser(token) : null;
32	        if (user == null) {
33	            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
34	            return false;
35	        }
36	        if (path.startsWith("/api/admin/") && !user.isAdmin()) {
37	            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
38	            return false;
39	        }
40	        UserContext.set(user);
41	        return true;
42	    }
43	
44	    private boolean isPublic(HttpServletRequest request, String path) {
45	        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
46	            return true;
47	        }
48	        return HttpMethod.GET.matches(request.getMethod()) && path.startsWith("/api/room-types");
49	    }
50	
51	    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
52	        response.setStatus(status);
53	        response.setContentType("application/json;charset=UTF-8");
54	        response.getOutputStream().write(
55	                ("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}")
56	                        .getBytes(StandardCharsets.UTF_8));
57	    }
58	
59	    @Override
60	    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
61	        UserContext.clear();
62	    }
63	}

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/config/WebConfi…`)</summary>

<!-- call_id: tool_LV01XWykmfUiFE55eQS8u83v -->
1	package com.example.hotel.config;
2	
3	import org.springframework.context.annotation.Configuration;
4	import org.springframework.web.servlet.config.annotation.CorsRegistry;
5	import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
6	import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
7	
8	@Configuration
9	public class WebConfig implements WebMvcConfigurer {
10	
11	    private final AuthInterceptor authInterceptor;
12	
13	    public WebConfig(AuthInterceptor authInterceptor) {
14	        this.authInterceptor = authInterceptor;
15	    }
16	
17	    @Override
18	    public void addInterceptors(InterceptorRegistry registry) {
19	        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
20	    }
21	
22	    @Override
23	    public void addCorsMappings(CorsRegistry registry) {
24	        // 仅 dev 模式（vite 5173 端口）需要；交付模式前后端同源部署
25	        registry.addMapping("/api/**")
26	                .allowedOrigins("http://localhost:5173")
27	                .allowedMethods("GET", "POST", "PUT", "DELETE")
28	                .allowedHeaders("*");
29	    }
30	}

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/auth/entity/Use…`)</summary>

<!-- call_id: tool_JQXJCbwvzv09T3SwjYqXzYfL -->
1	package com.example.hotel.auth.entity;
2	
3	import com.baomidou.mybatisplus.annotation.IdType;
4	import com.baomidou.mybatisplus.annotation.TableId;
5	import com.baomidou.mybatisplus.annotation.TableName;
6	
7	import java.time.LocalDateTime;
8	
9	@TableName("user")
10	public class User {
11	
12	    @TableId(type = IdType.AUTO)
13	    private Long id;
14	    private String loginName;
15	    private String passwordHash;
16	    private String role;
17	    private LocalDateTime createdAt;
18	
19	    public Long getId() {
20	        return id;
21	    }
22	
23	    public void setId(Long id) {
24	        this.id = id;
25	    }
26	
27	    public String getLoginName() {
28	        return loginName;
29	    }
30	
31	    public void setLoginName(String loginName) {
32	        this.loginName = loginName;
33	    }
34	
35	    public String getPasswordHash() {
36	        return passwordHash;
37	    }
38	
39	    public void setPasswordHash(String passwordHash) {
40	        this.passwordHash = passwordHash;
41	    }
42	
43	    public String getRole() {
44	        return role;
45	    }
46	
47	    public void setRole(String role) {
48	        this.role = role;
49	    }
50	
51	    public LocalDateTime getCreatedAt() {
52	        return createdAt;
53	    }
54	
55	    public void setCreatedAt(LocalDateTime createdAt) {
56	        this.createdAt = createdAt;
57	    }
58	}

</details>

<details><summary>Tool Result: Grep (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_mJfSUpSnqN0J8WOvOBF6GLwN -->
docs/03-测试用例.md-36----
docs/03-测试用例.md-37-
docs/03-测试用例.md-38-## 2. 账户与权限（AC-01~AC-04）
docs/03-测试用例.md-39-
docs/03-测试用例.md-40-| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
docs/03-测试用例.md-41-| --- | --- | --- | --- | --- | --- |
docs/03-测试用例.md:42:| TC-A01 | 游客浏览房型并引导登录 | 未登录（无 token / 已清 localStorage） | ① 打开首页房型列表；② 查看房型名称、单价、介绍；③ 点击某房型"预订" | ①② 正常展示，无需登录；③ 被引导至登录页 | AC-01、UC-02 |
docs/03-测试用例.md-43-| TC-A02 | 注册成功并登录 | 手机号 13800000001 未注册 | ① 进入注册页，输入手机号、密码 `Test1234`，提交；② 用该手机号+密码登录 | ① 注册成功；② 登录成功，进入住客首页，后续请求携带 token | AC-02、UC-01 |
docs/03-测试用例.md-44-| TC-A03 | 重复手机号注册被拒 | 13800000001 已注册 | 用同一手机号再次注册 | 提示手机号已注册（错误码 1002），不创建新账号 | AC-02、UC-01 异常 |
docs/03-测试用例.md-45-| TC-A04 | 密码错误登录被拒 | 住客甲已注册 | 用正确手机号 + 错误密码登录 | 提示"手机号或密码错误"，不发放 token | UC-01 异常 |
docs/03-测试用例.md-46-| TC-A05 | 注册参数格式校验 | — | ① 手机号填 `123`（非手机号格式）提交；② 手机号或密码留空提交 | ① 提示手机号格式不正确；② 对应输入框提示"请输入手机号/密码"，均不发起有效注册 | BR-01 手机号格式、原型 P-C3/C4 |
docs/03-测试用例.md-47-| TC-A06 | 我的订单数据隔离 | 住客甲、乙各有至少 1 笔订单 | ① 住客甲登录，进入"我的订单"；② 直接调 API：`GET /api/orders/{住客乙的订单号}`（带甲的 token） | ① 仅展示甲的订单；② 接口拒绝访问（不返回乙的订单数据） | AC-03、技术方案 §5.4（按 token 中 user_id 过滤） |
docs/03-测试用例.md-48-| TC-A07 | 住客访问前台接口被拒 | 住客甲已登录 | 带甲的 token 调 `GET /api/admin/orders` | 返回 403，无订单数据泄露 | AC-04、技术方案 §5.4 |
--
docs/03-测试用例.md-105-| TC-E05 | 终态不可再流转 | 订单 O3"已完成"、O4"已取消" | 对 O3、O4 分别尝试：取消、办理入住、办理退房（直接调 API） | 全部拒绝，状态不变——终态无任何出口 | AC-19、PRD §6.2 |
docs/03-测试用例.md-106-
docs/03-测试用例.md-107-## 7. 基础数据与房型/房间维护（AC-20、AC-21、UC-09）
docs/03-测试用例.md-108-
docs/03-测试用例.md-109-| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
docs/03-测试用例.md-110-| --- | --- | --- | --- | --- | --- |
docs/03-测试用例.md:111:| TC-F01 | 预置数据开箱可用 | 全新环境，`start.sh` 一键启动 | 启动后：① 未登录打开房型列表；② admin 登录后台 | ① 有 3~4 种房型、每个房型有若干房间，主链路可直接演示；② admin 可登录，无需手工录入数据 | AC-20 |
docs/03-测试用例.md-112-| TC-F02 | 初始化脚本幂等 | 系统已启动过一次 | 重启应用（`schema.sql`/`data.sql` 再次执行） | 启动不报错；预置房型/房间/admin 不重复；已有业务数据（订单等）不受影响 | 技术方案 §4.2 |
docs/03-测试用例.md-113-| TC-F03 | 新增房型同步住客侧 | admin 已登录 | ① 后台新增房型"套房 S"（单价 588.00）并为该房型新增 2 个房间；② 住客侧刷新房型列表并查询可订数量 | ② 列表出现"套房 S"，剩余可订数量为 2 | AC-21、UC-09 |
docs/03-测试用例.md-114-| TC-F04 | 修改房型信息同步 | 存在房型大床房 A | ① 后台修改名称/单价/介绍；② 住客侧查看 | 住客侧展示更新后的信息（已生成订单金额不受影响，同 TC-B09） | AC-21 |
docs/03-测试用例.md-115-| TC-F05 | 房型表单校验 | admin 已登录 | 新增/编辑房型时：① 名称或单价留空；② 单价填 0 或负数 | 提示"请填写完整的房型信息"，不保存 | 原型 P-A4 |
docs/03-测试用例.md-116-| TC-F06 | 房间号唯一性 | admin 已登录 | 新增/编辑房间：① 房间号留空；② 房间号与现有房间重复 | 提示"房间号不能为空且不可重复"，不保存 | 原型 P-A5 |
docs/03-测试用例.md-117-| TC-F07 | 减少房间低于有效订单数被拒 | 大床房 A 共 3 间，当前有效订单（已确认+已入住）= 3 | 尝试把房间数减到 2（删除/改挂 1 间到其他房型） | 拒绝，提示"该房型存在有效订单，请先处理相关订单"；房间数与库存不变 | UC-09 约束 |
--
docs/03-测试用例.md-144----
docs/03-测试用例.md-145-
docs/03-测试用例.md-146-## 10. 验收标准覆盖对照
docs/03-测试用例.md-147-
docs/03-测试用例.md-148-| 验收标准 | 对应用例 |
docs/03-测试用例.md-149-| --- | --- |
docs/03-测试用例.md:150:| AC-01 | TC-A01 |
docs/03-测试用例.md-151-| AC-02 | TC-A02、TC-A03、TC-A04 |
docs/03-测试用例.md-152-| AC-03 | TC-A06、TC-C05、TC-H03 |
docs/03-测试用例.md-153-| AC-04 | TC-A07、TC-A09 |
docs/03-测试用例.md-154-| AC-05 | TC-B01、TC-B02 |
docs/03-测试用例.md-155-| AC-06 | TC-B03（另 TC-B04 覆盖 BR-01 日期下界） |
docs/03-测试用例.md-156-| AC-07 | TC-B05（另 TC-B08、TC-B10 补充晚数与金额口径） |
--
docs/03-测试用例.md-163-| AC-14 | TC-D06 |
docs/03-测试用例.md-164-| AC-15 | TC-D07 |
docs/03-测试用例.md-165-| AC-16 | TC-D02、TC-D08、TC-G04 |
docs/03-测试用例.md-166-| AC-17 | TC-E01 |
docs/03-测试用例.md-167-| AC-18 | TC-E04 |
docs/03-测试用例.md-168-| AC-19 | TC-C04、TC-D06、TC-E04、TC-E05 |
docs/03-测试用例.md:169:| AC-20 | TC-F01、TC-F02 |
docs/03-测试用例.md-170-| AC-21 | TC-F03、TC-F04（另 TC-F05~F08 覆盖 UC-09 维护约束） |
docs/03-测试用例.md-171-
docs/03-测试用例.md-172-**补充覆盖（非 AC 直接要求）**：BR-03 金额固化 TC-B09、晚到不退差价 TC-D03；BR-07 提前退房可再售/不退差价 TC-E02、TC-E03；BR-08 订单号并发唯一 TC-G07；代订 TC-B06；库存回滚与先校验后占用 TC-B12、TC-B13；并发状态流转 TC-G05、TC-G06；统一响应/密码/token TC-H01~H04；端到端冒烟 TC-H05。
docs/03-测试用例.md-173-
docs/03-测试用例.md-174----
docs/03-测试用例.md-175-

</details>

<details><summary>Thinking</summary>

Now look at existing tests structure, schema.sql, Result/ErrorCode, and how tests were written for auth (they said TC-A02~A10 API tested). Check src/test directory.

</details>

#### Tool Call: Bash (`find hotel-server/src/test -type f | sort; echo ---; ls hote…`)
<!-- call_id: tool_mxPBPzbW2epWQay7AazXyD54 -->
```json
{
  "command": "find hotel-server/src/test -type f | sort; echo ---; ls hotel-server/src/main/resources"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/common/api/Erro…`)
<!-- call_id: tool_JaN3n224vkueCSvkyNNr2z21 -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/common/api/ErrorCode.java"
}
```

<details><summary>Tool Result: Bash (`find hotel-server/src/test -type f | sort; echo ---; ls hote…`)</summary>

<!-- call_id: tool_mxPBPzbW2epWQay7AazXyD54 -->
find: hotel-server/src/test: No such file or directory
---
application.yml
data.sql
schema.sql


</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/common/api/Erro…`)</summary>

<!-- call_id: tool_JaN3n224vkueCSvkyNNr2z21 -->
1	package com.example.hotel.common.api;
2	
3	public enum ErrorCode {
4	
5	    SOLD_OUT(1001, "该房型所选日期已订满"),
6	    PHONE_REGISTERED(1002, "手机号已注册"),
7	    CHECKIN_NOT_ALLOWED(1003, "不满足入住条件"),
8	    PARAM_INVALID(1004, "参数校验失败"),
9	    LOGIN_FAILED(1005, "手机号或密码错误"),
10	    ORDER_STATUS_CHANGED(1006, "订单状态已变更，请刷新查看"),
11	    NOT_FOUND(1007, "数据不存在"),
12	    SYSTEM_ERROR(5000, "系统异常，请稍后重试");
13	
14	    private final int code;
15	    private final String defaultMessage;
16	
17	    ErrorCode(int code, String defaultMessage) {
18	        this.code = code;
19	        this.defaultMessage = defaultMessage;
20	    }
21	
22	    public int getCode() {
23	        return code;
24	    }
25	
26	    public String getDefaultMessage() {
27	        return defaultMessage;
28	    }
29	}

</details>

#### Tool Call: Read (`hotel-server/src/main/resources/schema.sql`)
<!-- call_id: tool_anDw4HP5v9im7VZOsklJvHmC -->
```json
{
  "path": "hotel-server/src/main/resources/schema.sql"
}
```

#### Tool Call: Read (`hotel-server/src/main/resources/data.sql`)
<!-- call_id: tool_VG13fPgblgllnRJrUlMWkHpX -->
```json
{
  "path": "hotel-server/src/main/resources/data.sql"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/auth/AuthServic…`)
<!-- call_id: tool_Ng1prFSM6VqjZQ58cEskDpgK -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/auth/AuthService.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/resources/data.sql`)</summary>

<!-- call_id: tool_VG13fPgblgllnRJrUlMWkHpX -->
1	-- 预置数据（幂等：INSERT IGNORE）
2	-- admin 预置账号：登录名 admin，明文密码 admin123（BCrypt 密文如下，演示用）
3	
4	INSERT IGNORE INTO `user` (`id`, `login_name`, `password_hash`, `role`) VALUES
5	  (1, 'admin', '$2a$10$HZ9v9LU77InQmy68viHTGOkNrz7dbcRbkU6hAD5.heWwYwtaBxKQG', 'ADMIN');
6	
7	INSERT IGNORE INTO `room_type` (`id`, `name`, `price`, `description`) VALUES
8	  (1, '大床房',   288.50, '1.8 米大床，含双早，免费 WiFi'),
9	  (2, '双床房',   328.00, '两张 1.2 米单人床，适合结伴出行'),
10	  (3, '家庭房',   458.00, '一张大床加一张儿童床，适合三口之家'),
11	  (4, '行政套房', 588.00, '独立客厅与卧室，含行政礼遇');
12	
13	INSERT IGNORE INTO `room` (`room_no`, `room_type_id`) VALUES
14	  ('A101', 1), ('A102', 1), ('A103', 1),
15	  ('B101', 2), ('B102', 2), ('B103', 2),
16	  ('C101', 3), ('C102', 3),
17	  ('D101', 4), ('D102', 4);

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/resources/schema.sql`)</summary>

<!-- call_id: tool_anDw4HP5v9im7VZOsklJvHmC -->
1	-- 酒店预订系统建表脚本（幂等：CREATE TABLE IF NOT EXISTS）
2	-- 字符集 utf8mb4，InnoDB
3	
4	CREATE TABLE IF NOT EXISTS `user` (
5	  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
6	  `login_name`    VARCHAR(32)  NOT NULL COMMENT '登录名（住客=手机号，admin=admin）',
7	  `password_hash` VARCHAR(72)  NOT NULL COMMENT 'BCrypt 密文',
8	  `role`          VARCHAR(16)  NOT NULL COMMENT 'GUEST / ADMIN',
9	  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
10	  PRIMARY KEY (`id`),
11	  UNIQUE KEY `uk_user_login_name` (`login_name`)
12	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住客与前台账号';
13	
14	CREATE TABLE IF NOT EXISTS `auth_token` (
15	  `token`      VARCHAR(36) NOT NULL COMMENT 'UUID token',
16	  `user_id`    BIGINT      NOT NULL,
17	  `expires_at` DATETIME    NOT NULL,
18	  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
19	  PRIMARY KEY (`token`),
20	  KEY `idx_auth_token_user` (`user_id`)
21	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录 token';
22	
23	CREATE TABLE IF NOT EXISTS `room_type` (
24	  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
25	  `name`        VARCHAR(64)   NOT NULL,
26	  `price`       DECIMAL(10,2) NOT NULL COMMENT '单价（元/晚）',
27	  `description` VARCHAR(512)  NOT NULL DEFAULT '',
28	  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
29	  PRIMARY KEY (`id`)
30	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房型';
31	
32	CREATE TABLE IF NOT EXISTS `room` (
33	  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
34	  `room_no`      VARCHAR(16) NOT NULL,
35	  `room_type_id` BIGINT      NOT NULL,
36	  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
37	  PRIMARY KEY (`id`),
38	  UNIQUE KEY `uk_room_room_no` (`room_no`),
39	  KEY `idx_room_type` (`room_type_id`)
40	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理房间（无状态字段，状态由在住订单推导）';
41	
42	CREATE TABLE IF NOT EXISTS `hotel_order` (
43	  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
44	  `order_no`      VARCHAR(32)   NOT NULL COMMENT 'HR+yyyyMMdd-NNNN',
45	  `user_id`       BIGINT        NOT NULL COMMENT '下单账号',
46	  `guest_name`    VARCHAR(32)   NOT NULL COMMENT '住客姓名（支持代订）',
47	  `guest_phone`   VARCHAR(16)   NOT NULL COMMENT '住客手机号',
48	  `room_type_id`  BIGINT        NOT NULL,
49	  `room_id`       BIGINT        NULL COMMENT '入住前为 NULL',
50	  `id_card`       VARCHAR(18)   NULL COMMENT '入住前为 NULL',
51	  `checkin_date`  DATE          NOT NULL,
52	  `checkout_date` DATE          NOT NULL,
53	  `nights`        INT           NOT NULL,
54	  `amount`        DECIMAL(10,2) NOT NULL COMMENT '下单时固化的总金额（元）',
55	  `status`        VARCHAR(16)   NOT NULL COMMENT 'CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED',
56	  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
57	  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
58	  `active_room_id` BIGINT GENERATED ALWAYS AS (IF(`status`='CHECKED_IN', `room_id`, NULL)) VIRTUAL,
59	  PRIMARY KEY (`id`),
60	  UNIQUE KEY `uk_order_order_no` (`order_no`),
61	  UNIQUE KEY `uk_order_active_room` (`active_room_id`),
62	  KEY `idx_order_user` (`user_id`),
63	  KEY `idx_order_guest_phone` (`guest_phone`)
64	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';
65	
66	CREATE TABLE IF NOT EXISTS `daily_inventory` (
67	  `id`             BIGINT  NOT NULL AUTO_INCREMENT,
68	  `room_type_id`   BIGINT  NOT NULL,
69	  `stay_date`      DATE    NOT NULL,
70	  `total_count`    INT     NOT NULL COMMENT '当日该房型房间数快照',
71	  `occupied_count` INT     NOT NULL DEFAULT 0,
72	  PRIMARY KEY (`id`),
73	  UNIQUE KEY `uk_inventory_type_date` (`room_type_id`, `stay_date`)
74	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐日库存';
75	
76	CREATE TABLE IF NOT EXISTS `order_seq` (
77	  `seq_date`      DATE   NOT NULL,
78	  `current_value` BIGINT NOT NULL DEFAULT 0,
79	  PRIMARY KEY (`seq_date`)
80	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单号每日序号';

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/auth/AuthServic…`)</summary>

<!-- call_id: tool_Ng1prFSM6VqjZQ58cEskDpgK -->
1	package com.example.hotel.auth;
2	
3	import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
4	import com.example.hotel.auth.dto.LoginRequest;
5	import com.example.hotel.auth.dto.LoginResponse;
6	import com.example.hotel.auth.dto.RegisterRequest;
7	import com.example.hotel.auth.dto.UserInfo;
8	import com.example.hotel.auth.entity.AuthToken;
9	import com.example.hotel.auth.entity.User;
10	import com.example.hotel.auth.mapper.AuthTokenMapper;
11	import com.example.hotel.auth.mapper.UserMapper;
12	import com.example.hotel.common.api.ErrorCode;
13	import com.example.hotel.common.context.LoginUser;
14	import com.example.hotel.common.context.UserContext;
15	import com.example.hotel.common.exception.BizException;
16	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
17	import org.springframework.stereotype.Service;
18	
19	import java.time.LocalDateTime;
20	import java.util.UUID;
21	
22	@Service
23	public class AuthService {
24	
25	    private static final long TOKEN_VALID_DAYS = 7;
26	
27	    private final UserMapper userMapper;
28	    private final AuthTokenMapper authTokenMapper;
29	    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
30	
31	    public AuthService(UserMapper userMapper, AuthTokenMapper authTokenMapper) {
32	        this.userMapper = userMapper;
33	        this.authTokenMapper = authTokenMapper;
34	    }
35	
36	    public void register(RegisterRequest request) {
37	        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("login_name", request.getPhone()));
38	        if (count > 0) {
39	            throw new BizException(ErrorCode.PHONE_REGISTERED);
40	        }
41	        User user = new User();
42	        user.setLoginName(request.getPhone());
43	        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
44	        user.setRole(LoginUser.ROLE_GUEST);
45	        userMapper.insert(user);
46	    }
47	
48	    public LoginResponse login(LoginRequest request) {
49	        User user = userMapper.selectOne(new QueryWrapper<User>().eq("login_name", request.getLoginName()));
50	        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
51	            throw new BizException(ErrorCode.LOGIN_FAILED);
52	        }
53	        AuthToken authToken = new AuthToken();
54	        authToken.setToken(UUID.randomUUID().toString());
55	        authToken.setUserId(user.getId());
56	        authToken.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_VALID_DAYS));
57	        authTokenMapper.insert(authToken);
58	        return new LoginResponse(authToken.getToken(), user.getRole(), user.getLoginName());
59	    }
60	
61	    public void logout(String token) {
62	        if (token != null && !token.isBlank()) {
63	            authTokenMapper.deleteById(token);
64	        }
65	    }
66	
67	    public UserInfo currentUser() {
68	        LoginUser loginUser = UserContext.require();
69	        return new UserInfo(loginUser.id(), loginUser.loginName(), loginUser.role());
70	    }
71	
72	    public LoginUser resolveUser(String token) {
73	        AuthToken authToken = authTokenMapper.selectById(token);
74	        if (authToken == null) {
75	            return null;
76	        }
77	        if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
78	            authTokenMapper.deleteById(token);
79	            return null;
80	        }
81	        User user = userMapper.selectById(authToken.getUserId());
82	        if (user == null) {
83	            return null;
84	        }
85	        return new LoginUser(user.getId(), user.getLoginName(), user.getRole());
86	    }
87	}

</details>

<details><summary>Thinking</summary>

Also check Result.java, dto patterns (LoginResponse), application.yml, and how the auth tests were run (they said "API 部分已实测通过" — no test source dir, so tests were manual curl? There is no src/test directory. T5 says "TC-A02~A10、H02 API 部分已实测通过" — probably manual curl testing. T6 says "对应测试用例：TC-F01①、TC-A01①②" and the user asked "完成后运行对应测试". The inventory T7 says "并发集成测试" — so eventually tests will be written. For T6, the corresponding tests are TC-F01① (seed data visible without login) and TC-A01①② (browse room types without login) — these can be verified via curl against a running server, or via integration tests.

Task list says tests for T6 are API-level; I could write a small integration test. But project has no test dir yet. The T7 task explicitly mentions concurrent integration tests, so tests will be introduced later. For T6, I'll follow the pattern used for T5: run the server and verify via curl. But mvn test requires... Actually let me check how T5 was "实测" — probably running app + curl. I'll do the same: start MySQL (docker compose up -d), run the app, curl the endpoints.

Wait — user said "运行对应测试". Best to start DB, start the app, and curl the two endpoints, verifying TC-F01① and TC-A01①② (unauthenticated access, amounts in cents).

Now implement:

1. `room/entity/RoomType.java` — id, name, price (BigDecimal), description, createdAt.
2. `room/entity/Room.java` — id, roomNo, roomTypeId, createdAt.
3. `room/mapper/RoomTypeMapper.java`, `room/mapper/RoomMapper.java`.
4. `room/dto/RoomTypeInfo.java` — id, name, price in cents (Long), description. 元↔分换算 in DTO serialization layer. How to do "换算只在 DTO 序列化层"? E.g., a static factory `RoomTypeInfo.from(RoomType)` converting BigDecimal yuan → long cents. That's the DTO layer.
5. `room/RoomService.java` — listRoomTypes(), getRoomType(id) (throw NOT_FOUND if missing), countRooms(typeId), listAssignableRooms(typeId) — rooms of type minus rooms occupied by CHECKED_IN orders. But room module cannot use order's mapper (mapper 不跨模块使用; cross-module calls only order → inventory, order → room). So how does room derive occupied rooms from orders? Hmm. Room module needs to query hotel_order for CHECKED_IN. Options: room module has its own query on hotel_order table? That violates "mapper 不跨模块使用" if it used OrderMapper. The technical方案 §3.4 says room hides "房间状态不入库、由在住订单推导". So room module itself queries the order table. It could define its own mapper/query — e.g., a custom SQL in RoomMapper: 

```sql
SELECT r.* FROM room r WHERE r.room_type_id = #{typeId}
AND r.id NOT IN (SELECT room_id FROM hotel_order WHERE status='CHECKED_IN' AND room_id IS NOT NULL)
```

That's within room module's mapper — acceptable, since room owns the derivation logic. T9 will use this for check-in. I'll add that query to RoomMapper via @Select annotation (MyBatis-Plus supports annotations).

6. `room/RoomTypeController.java` — GET /api/room-types, GET /api/room-types/{id}.

Note technical方案 also lists GET /api/room-types/availability — but that's inventory-related (T7); T6 says only the two read-only endpoints, "不含剩余量". Note: `/api/room-types/{id}` path could conflict with availability later; Spring handles `/api/room-types/availability` vs `/{id}` — literal match wins. Fine.

DTO: check existing dto style (LoginResponse). Let me read it quickly plus Result and application.yml.

</details>

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/auth/dto/LoginR…`)
<!-- call_id: tool_g9eTns6aMIlYbXZ0f5LAzDGA -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/auth/dto/LoginResponse.java"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/common/api/Resu…`)
<!-- call_id: tool_lkuky6aKM9He3es4GqKJokFI -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/common/api/Result.java"
}
```

#### Tool Call: Read (`hotel-server/src/main/resources/application.yml`)
<!-- call_id: tool_8B710K2k21aGJyX67qToXmBp -->
```json
{
  "path": "hotel-server/src/main/resources/application.yml"
}
```

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/auth/mapper/Use…`)
<!-- call_id: tool_WJvHENEHbnwHCCMmesvXx8QI -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/auth/mapper/UserMapper.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/resources/application.yml`)</summary>

<!-- call_id: tool_8B710K2k21aGJyX67qToXmBp -->
1	server:
2	  port: 8080
3	
4	spring:
5	  application:
6	    name: hotel-server
7	  datasource:
8	    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/hotel?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
9	    username: ${MYSQL_USER:root}
10	    password: ${MYSQL_PASSWORD:root123}
11	  sql:
12	    init:
13	      mode: always
14	      encoding: UTF-8
15	
16	mybatis-plus:
17	  configuration:
18	    map-underscore-to-camel-case: true
19	
20	logging:
21	  level:
22	    com.example.hotel: info

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/common/api/Resu…`)</summary>

<!-- call_id: tool_lkuky6aKM9He3es4GqKJokFI -->
1	package com.example.hotel.common.api;
2	
3	public class Result<T> {
4	
5	    private int code;
6	    private String message;
7	    private T data;
8	
9	    public Result() {
10	    }
11	
12	    public Result(int code, String message, T data) {
13	        this.code = code;
14	        this.message = message;
15	        this.data = data;
16	    }
17	
18	    public static <T> Result<T> ok(T data) {
19	        return new Result<>(0, "ok", data);
20	    }
21	
22	    public static Result<Void> ok() {
23	        return new Result<>(0, "ok", null);
24	    }
25	
26	    public static <T> Result<T> error(int code, String message) {
27	        return new Result<>(code, message, null);
28	    }
29	
30	    public static <T> Result<T> error(ErrorCode errorCode) {
31	        return new Result<>(errorCode.getCode(), errorCode.getDefaultMessage(), null);
32	    }
33	
34	    public int getCode() {
35	        return code;
36	    }
37	
38	    public void setCode(int code) {
39	        this.code = code;
40	    }
41	
42	    public String getMessage() {
43	        return message;
44	    }
45	
46	    public void setMessage(String message) {
47	        this.message = message;
48	    }
49	
50	    public T getData() {
51	        return data;
52	    }
53	
54	    public void setData(T data) {
55	        this.data = data;
56	    }
57	}

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/auth/dto/LoginR…`)</summary>

<!-- call_id: tool_g9eTns6aMIlYbXZ0f5LAzDGA -->
1	package com.example.hotel.auth.dto;
2	
3	public class LoginResponse {
4	
5	    private String token;
6	    private String role;
7	    private String loginName;
8	
9	    public LoginResponse(String token, String role, String loginName) {
10	        this.token = token;
11	        this.role = role;
12	        this.loginName = loginName;
13	    }
14	
15	    public String getToken() {
16	        return token;
17	    }
18	
19	    public String getRole() {
20	        return role;
21	    }
22	
23	    public String getLoginName() {
24	        return loginName;
25	    }
26	}

</details>

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/auth/mapper/Use…`)</summary>

<!-- call_id: tool_WJvHENEHbnwHCCMmesvXx8QI -->
1	package com.example.hotel.auth.mapper;
2	
3	import com.baomidou.mybatisplus.core.mapper.BaseMapper;
4	import com.example.hotel.auth.entity.User;
5	
6	public interface UserMapper extends BaseMapper<User> {
7	}

</details>

<details><summary>Thinking</summary>

Check if @MapperScan exists in HotelApplication.

Now create files:

1. `room/entity/RoomType.java`
2. `room/entity/Room.java`
3. `room/mapper/RoomTypeMapper.java`
4. `room/mapper/RoomMapper.java` — with @Select for assignable rooms
5. `room/dto/RoomTypeInfo.java` — id, name, price (Long, cents), description; static from(RoomType) doing conversion
6. `room/RoomService.java` — listRoomTypes(), getRoomType(id), listAssignableRooms(typeId)
7. `room/RoomTypeController.java`

Check HotelApplication for MapperScan.

</details>

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/HotelApplicatio…`)
<!-- call_id: tool_dDAl1xInvqe8Z1nkrn4IvYSJ -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/HotelApplication.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/HotelApplicatio…`)</summary>

<!-- call_id: tool_dDAl1xInvqe8Z1nkrn4IvYSJ -->
1	package com.example.hotel;
2	
3	import org.mybatis.spring.annotation.MapperScan;
4	import org.springframework.boot.SpringApplication;
5	import org.springframework.boot.autoconfigure.SpringBootApplication;
6	
7	@SpringBootApplication
8	@MapperScan("com.example.hotel.**.mapper")
9	public class HotelApplication {
10	
11	    public static void main(String[] args) {
12	        SpringApplication.run(HotelApplication.class, args);
13	    }
14	}

</details>

<details><summary>Thinking</summary>

Now create the room module files. Following existing style (plain getters/setters, no Lombok).

RoomTypeInfo DTO: id, name, price (Long, 分), description. Static factory from(RoomType) — 换算在 DTO 层. Use `price.multiply(BigDecimal.valueOf(100)).longValueExact()` — safer with setScale. DECIMAL(10,2) so scale 2. `movePointRight(2).longValueExact()`.

RoomMapper assignable query:

```java
@Select("SELECT * FROM room WHERE room_type_id = #{typeId} AND id NOT IN " +
        "(SELECT room_id FROM hotel_order WHERE status = 'CHECKED_IN' AND room_id IS NOT NULL)")
List<Room> selectAssignable(Long typeId);
```

RoomService: listRoomTypes() returns List<RoomTypeInfo>, getRoomType(Long id) returns RoomTypeInfo or throws BizException(NOT_FOUND), listAssignableRooms(Long typeId) returns List<Room>.

Should service return DTO or entity? Controller 不直接向外暴露 entity, service 承载业务规则. Returning DTO from service is fine for read-only. I'll have service return entity lists and controller map to DTO? "controller 只做参数校验与组装响应" — 组装响应 including DTO mapping is OK. But 元↔分换算只在 DTO 序列化层 — the DTO's from() method does it. I'll have RoomService return entities (listRoomTypes, getRoomType), and controller map to RoomTypeInfo via RoomTypeInfo.from. Actually for listAssignableRooms internal use (T9), returning List<Room> entities is right.

RoomTypeController:

```java
@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {
    @GetMapping
    public Result<List<RoomTypeInfo>> list() {...}
    @GetMapping("/{id}")
    public Result<RoomTypeInfo> detail(@PathVariable Long id) {...}
}
```

AuthInterceptor already allows GET /api/room-types* without login.

Note: future availability endpoint `/api/room-types/availability?...` — with `/{id}` pattern, "availability" would match {id} as Long and fail with 400 unless availability is registered as a literal path (Spring prefers literal). T7 will add it; fine.

Write files now.

</details>

现在开始实现 T6（room 模块）。创建实体、mapper、service、DTO 和 controller：

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/entity/Roo…`)
<!-- call_id: tool_AWyBXYcUKcu9GrAAhiLESHOz -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/room/entity/RoomType.java",
  "content": "package com.example.hotel.room.entity;\n\nimport com.baomidou.mybatisplus.annotation.IdType;\nimport com.baomidou.mybatisplus.annotation.TableId;\nimport com.baomidou.mybatisplus.annotation.TableName;\n\nimport java.math.BigDecimal;\nimport java.time.LocalDateTime;\n\n@TableName(\"room_type\")\npublic class RoomType {\n\n    @TableId(type = IdType.AUTO)\n    private Long id;\n    private String name;\n    private BigDecimal price;\n    private String description;\n    private LocalDateTime createdAt;\n\n    public Long getId() {\n        return id;\n    }\n\n    public void setId(Long id) {\n        this.id = id;\n    }\n\n    public String getName() {\n        return name;\n    }\n\n    public void setName(String name) {\n        this.name = name;\n    }\n\n    public BigDecimal getPrice() {\n        return price;\n    }\n\n    public void setPrice(BigDecimal price) {\n        this.price = price;\n    }\n\n    public String getDescription() {\n        return description;\n    }\n\n    public void setDescription(String description) {\n        this.description = description;\n    }\n\n    public LocalDateTime getCreatedAt() {\n        return createdAt;\n    }\n\n    public void setCreatedAt(LocalDateTime createdAt) {\n        this.createdAt = createdAt;\n    }\n}\n"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/entity/Roo…`)
<!-- call_id: tool_B4FrhneAeZgyfKL6cRP87cEA -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/room/entity/Room.java",
  "content": "package com.example.hotel.room.entity;\n\nimport com.baomidou.mybatisplus.annotation.IdType;\nimport com.baomidou.mybatisplus.annotation.TableId;\nimport com.baomidou.mybatisplus.annotation.TableName;\n\nimport java.time.LocalDateTime;\n\n@TableName(\"room\")\npublic class Room {\n\n    @TableId(type = IdType.AUTO)\n    private Long id;\n    private String roomNo;\n    private Long roomTypeId;\n    private LocalDateTime createdAt;\n\n    public Long getId() {\n        return id;\n    }\n\n    public void setId(Long id) {\n        this.id = id;\n    }\n\n    public String getRoomNo() {\n        return roomNo;\n    }\n\n    public void setRoomNo(String roomNo) {\n        this.roomNo = roomNo;\n    }\n\n    public Long getRoomTypeId() {\n        return roomTypeId;\n    }\n\n    public void setRoomTypeId(Long roomTypeId) {\n        this.roomTypeId = roomTypeId;\n    }\n\n    public LocalDateTime getCreatedAt() {\n        return createdAt;\n    }\n\n    public void setCreatedAt(LocalDateTime createdAt) {\n        this.createdAt = createdAt;\n    }\n}\n"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/mapper/Roo…`)
<!-- call_id: tool_IWAtT0rgc9JfooHDbGb2PAhz -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/room/mapper/RoomTypeMapper.java",
  "content": "package com.example.hotel.room.mapper;\n\nimport com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport com.example.hotel.room.entity.RoomType;\n\npublic interface RoomTypeMapper extends BaseMapper<RoomType> {\n}\n"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/mapper/Roo…`)
<!-- call_id: tool_A9AHtxHdEoCfSui9VIWNnmHZ -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/room/mapper/RoomMapper.java",
  "content": "package com.example.hotel.room.mapper;\n\nimport com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport com.example.hotel.room.entity.Room;\nimport org.apache.ibatis.annotations.Select;\n\nimport java.util.List;\n\npublic interface RoomMapper extends BaseMapper<Room> {\n\n    @Select(\"SELECT * FROM room WHERE room_type_id = #{typeId} AND id NOT IN \"\n            + \"(SELECT room_id FROM hotel_order WHERE status = 'CHECKED_IN' AND room_id IS NOT NULL)\")\n    List<Room> selectAssignable(Long typeId);\n}\n"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/dto/RoomTy…`)
<!-- call_id: tool_FRSAYMD7VTHxd2JI6pOtsYW2 -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/room/dto/RoomTypeInfo.java",
  "content": "package com.example.hotel.room.dto;\n\nimport com.example.hotel.room.entity.RoomType;\n\npublic class RoomTypeInfo {\n\n    private Long id;\n    private String name;\n    /** 单价，单位：分 */\n    private Long price;\n    private String description;\n\n    public static RoomTypeInfo from(RoomType roomType) {\n        RoomTypeInfo info = new RoomTypeInfo();\n        info.id = roomType.getId();\n        info.name = roomType.getName();\n        info.price = roomType.getPrice().movePointRight(2).longValueExact();\n        info.description = roomType.getDescription();\n        return info;\n    }\n\n    public Long getId() {\n        return id;\n    }\n\n    public String getName() {\n        return name;\n    }\n\n    public Long getPrice() {\n        return price;\n    }\n\n    public String getDescription() {\n        return description;\n    }\n}\n"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/entity/Roo…`)</summary>

<!-- call_id: tool_AWyBXYcUKcu9GrAAhiLESHOz -->
Wrote 1223 bytes to hotel-server/src/main/java/com/example/hotel/room/entity/RoomType.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/entity/Roo…`)</summary>

<!-- call_id: tool_B4FrhneAeZgyfKL6cRP87cEA -->
Wrote 1012 bytes to hotel-server/src/main/java/com/example/hotel/room/entity/Room.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/dto/RoomTy…`)</summary>

<!-- call_id: tool_FRSAYMD7VTHxd2JI6pOtsYW2 -->
Wrote 831 bytes to hotel-server/src/main/java/com/example/hotel/room/dto/RoomTypeInfo.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/mapper/Roo…`)</summary>

<!-- call_id: tool_IWAtT0rgc9JfooHDbGb2PAhz -->
Wrote 209 bytes to hotel-server/src/main/java/com/example/hotel/room/mapper/RoomTypeMapper.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/mapper/Roo…`)</summary>

<!-- call_id: tool_A9AHtxHdEoCfSui9VIWNnmHZ -->
Wrote 495 bytes to hotel-server/src/main/java/com/example/hotel/room/mapper/RoomMapper.java

</details>

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/RoomServic…`)
<!-- call_id: tool_6oHtPwgNpiTUKssAyHUVIgqF -->
```json
{
  "content": "package com.example.hotel.room;\n\nimport com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;\nimport com.example.hotel.common.api.ErrorCode;\nimport com.example.hotel.common.exception.BizException;\nimport com.example.hotel.room.entity.Room;\nimport com.example.hotel.room.entity.RoomType;\nimport com.example.hotel.room.mapper.RoomMapper;\nimport com.example.hotel.room.mapper.RoomTypeMapper;\nimport org.springframework.stereotype.Service;\n\nimport java.util.List;\n\n@Service\npublic class RoomService {\n\n    private final RoomTypeMapper roomTypeMapper;\n    private final RoomMapper roomMapper;\n\n    public RoomService(RoomTypeMapper roomTypeMapper, RoomMapper roomMapper) {\n        this.roomTypeMapper = roomTypeMapper;\n        this.roomMapper = roomMapper;\n    }\n\n    public List<RoomType> listRoomTypes() {\n        return roomTypeMapper.selectList(new QueryWrapper<RoomType>().orderByAsc(\"id\"));\n    }\n\n    public RoomType getRoomType(Long id) {\n        RoomType roomType = roomTypeMapper.selectById(id);\n        if (roomType == null) {\n            throw new BizException(ErrorCode.NOT_FOUND, \"房型不存在\");\n        }\n        return roomType;\n    }\n\n    public int countRooms(Long typeId) {\n        return Math.toIntExact(roomMapper.selectCount(new QueryWrapper<Room>().eq(\"room_type_id\", typeId)));\n    }\n\n    /** 该房型当前可分配房间：全部房间 − 被已入住订单占用的房间（房间状态不入库，由在住订单推导） */\n    public List<Room> listAssignableRooms(Long typeId) {\n        return roomMapper.selectAssignable(typeId);\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/room/RoomService.java"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/room/RoomTypeCo…`)
<!-- call_id: tool_DmVGimTSXqJVD6ROUfTbGMss -->
```json
{
  "content": "package com.example.hotel.room;\n\nimport com.example.hotel.common.api.Result;\nimport com.example.hotel.room.dto.RoomTypeInfo;\nimport org.springframework.web.bind.annotation.GetMapping;\nimport org.springframework.web.bind.annotation.PathVariable;\nimport org.springframework.web.bind.annotation.RequestMapping;\nimport org.springframework.web.bind.annotation.RestController;\n\nimport java.util.List;\n\n@RestController\n@RequestMapping(\"/api/room-types\")\npublic class RoomTypeController {\n\n    private final RoomService roomService;\n\n    public RoomTypeController(RoomService roomService) {\n        this.roomService = roomService;\n    }\n\n    @GetMapping\n    public Result<List<RoomTypeInfo>> list() {\n        List<RoomTypeInfo> roomTypes = roomService.listRoomTypes().stream()\n                .map(RoomTypeInfo::from)\n                .toList();\n        return Result.ok(roomTypes);\n    }\n\n    @GetMapping(\"/{id}\")\n    public Result<RoomTypeInfo> detail(@PathVariable Long id) {\n        return Result.ok(RoomTypeInfo.from(roomService.getRoomType(id)));\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/room/RoomTypeController.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/RoomTypeCo…`)</summary>

<!-- call_id: tool_DmVGimTSXqJVD6ROUfTbGMss -->
Wrote 1052 bytes to hotel-server/src/main/java/com/example/hotel/room/RoomTypeController.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/room/RoomServic…`)</summary>

<!-- call_id: tool_6oHtPwgNpiTUKssAyHUVIgqF -->
Wrote 1578 bytes to hotel-server/src/main/java/com/example/hotel/room/RoomService.java

</details>

<details><summary>Thinking</summary>

Check BizException has a constructor (ErrorCode, String) — need to verify.

</details>

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/common/exceptio…`)
<!-- call_id: tool_Mqx7DkXYH3SftlWLNSq4xtxE -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/common/exception/BizException.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/common/exceptio…`)</summary>

<!-- call_id: tool_Mqx7DkXYH3SftlWLNSq4xtxE -->
1	package com.example.hotel.common.exception;
2	
3	import com.example.hotel.common.api.ErrorCode;
4	
5	public class BizException extends RuntimeException {
6	
7	    private final int code;
8	
9	    public BizException(ErrorCode errorCode) {
10	        super(errorCode.getDefaultMessage());
11	        this.code = errorCode.getCode();
12	    }
13	
14	    public BizException(ErrorCode errorCode, String message) {
15	        super(message);
16	        this.code = errorCode.getCode();
17	    }
18	
19	    public BizException(int code, String message) {
20	        super(message);
21	        this.code = code;
22	    }
23	
24	    public int getCode() {
25	        return code;
26	    }
27	}

</details>

<details><summary>Thinking</summary>

Good. Now compile and run tests. Corresponding tests: TC-F01① (unauthenticated room list returns seed room types) and TC-A01①② (browse without login). No unit test infra yet — project has no src/test. T5 was "实测" via API. I'll compile, start DB + app, and curl.

Compile first: mvn -q compile. Then docker compose up -d, run app in background, curl endpoints.

</details>

代码完成。现在编译并启动服务，实测 T6 对应用例（TC-F01①、TC-A01①②）：

#### Tool Call: Bash (`cd hotel-server && mvn -q compile 2>&1 | tail -5; echo "comp…`)
<!-- call_id: tool_VJrPaLH27cDFqBaN1ksk0vSs -->
```json
{
  "command": "cd hotel-server && mvn -q compile 2>&1 | tail -5; echo \"compile exit: $?\"",
  "timeout": 180
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn -q compile 2>&1 | tail -5; echo "comp…`)</summary>

<!-- call_id: tool_VJrPaLH27cDFqBaN1ksk0vSs -->
compile exit: 0


</details>

#### Tool Call: Bash (`docker compose up -d 2>&1 | tail -2`)
<!-- call_id: tool_fhPwI9OI3UGerSbbF9TNKbPA -->
```json
{
  "command": "docker compose up -d 2>&1 | tail -2"
}
```

<details><summary>Tool Result: Bash (`docker compose up -d 2>&1 | tail -2`)</summary>

<!-- call_id: tool_fhPwI9OI3UGerSbbF9TNKbPA -->
 Container hotel-mysql Running 


</details>

#### Tool Call: Bash (`cd hotel-server && mvn -q spring-boot:run`)
<!-- call_id: tool_nuNRZiIyqG1W89QCG5vTQD0o -->
```json
{
  "command": "cd hotel-server && mvn -q spring-boot:run",
  "run_in_background": true,
  "description": "启动后端服务（spring-boot:run）"
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn -q spring-boot:run`)</summary>

<!-- call_id: tool_nuNRZiIyqG1W89QCG5vTQD0o -->
task_id: bash-a2p6626x
pid: 67718
description: 启动后端服务（spring-boot:run）
status: running
automatic_notification: true
next_step: The completion arrives automatically in a later turn — do NOT wait, poll, or call TaskOutput on it; continue with your current work.
next_step: Use TaskStop only if the task must be cancelled.
human_shell_hint: The task is visible in the background-task panel.

</details>

#### Tool Call: Bash (`for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_cod…`)
<!-- call_id: tool_4wa1jjdACHMkjHN6lrobe83F -->
```json
{
  "command": "for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/room-types 2>/dev/null | grep -q 200 && break; sleep 2; done; echo ready",
  "timeout": 90
}
```

<details><summary>Tool Result: Bash (`for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_cod…`)</summary>

<!-- call_id: tool_4wa1jjdACHMkjHN6lrobe83F -->
ready


</details>

<details><summary>Thinking</summary>

Wait — that returned in 0.112s meaning first curl returned 200? Possibly something else on 8080 already? Let me actually verify the responses. Run the actual test curls.

</details>

#### Tool Call: Bash (`echo '== TC-F01①/TC-A01① 未登录 GET /api/room-types =='; curl -…`)
<!-- call_id: tool_YJ3NZzKvfEk42Mmnq46O9OF6 -->
```json
{
  "command": "echo '== TC-F01①/TC-A01① 未登录 GET /api/room-types =='; curl -s http://localhost:8080/api/room-types; echo; echo '== TC-A01② 未登录 GET /api/room-types/1 =='; curl -s http://localhost:8080/api/room-types/1; echo; echo '== 不存在的房型 =='; curl -s http://localhost:8080/api/room-types/999; echo"
}
```

<details><summary>Tool Result: Bash (`echo '== TC-F01①/TC-A01① 未登录 GET /api/room-types =='; curl -…`)</summary>

<!-- call_id: tool_YJ3NZzKvfEk42Mmnq46O9OF6 -->
== TC-F01①/TC-A01① 未登录 GET /api/room-types ==
{"code":0,"message":"ok","data":[{"id":1,"name":"大床房","price":28850,"description":"1.8 米大床，含双早，免费 WiFi"},{"id":2,"name":"双床房","price":32800,"description":"两张 1.2 米单人床，适合结伴出行"},{"id":3,"name":"家庭房","price":45800,"description":"一张大床加一张儿童床，适合三口之家"},{"id":4,"name":"行政套房","price":58800,"description":"独立客厅与卧室，含行政礼遇"}]}
== TC-A01② 未登录 GET /api/room-types/1 ==
{"code":0,"message":"ok","data":{"id":1,"name":"大床房","price":28850,"description":"1.8 米大床，含双早，免费 WiFi"}}
== 不存在的房型 ==
{"code":1007,"message":"房型不存在","data":null}


</details>

<details><summary>Thinking</summary>

All pass. Also verify the 288.50 → 28850 cents conversion is correct (yes). Should I also verify listAssignableRooms? It's internal for T9; could quickly test via DB: no CHECKED_IN orders so all rooms of a type are assignable. There's no endpoint for it. I could verify with a quick check... there's no easy way without a test. Could insert a CHECKED_IN order in DB and... no endpoint exposes it. It's fine — the SQL is simple; T9 will exercise it. But I can at least verify the SQL works by running it directly in MySQL.

</details>

再直接验证 `listAssignableRooms` 的推导 SQL（T9 要用）：

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
IN…`)
<!-- call_id: tool_tfJgnK85vguYNEJCefOfMETv -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 hotel -e \"\nINSERT INTO hotel_order (order_no,user_id,guest_name,guest_phone,room_type_id,room_id,checkin_date,checkout_date,nights,amount,status)\nVALUES ('HR-T6TEST',1,'测试','13800000000',1,1,'2026-10-04','2026-10-06',2,577.00,'CHECKED_IN');\nSELECT r.id, r.room_no FROM room r WHERE r.room_type_id = 1 AND r.id NOT IN\n  (SELECT room_id FROM hotel_order WHERE status='CHECKED_IN' AND room_id IS NOT NULL);\nDELETE FROM hotel_order WHERE order_no='HR-T6TEST';\" 2>&1 | grep -v Warning"
}
```

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
IN…`)</summary>

<!-- call_id: tool_tfJgnK85vguYNEJCefOfMETv -->
id	room_no
2	A102
3	A103


</details>

<details><summary>Thinking</summary>

Verified: with a CHECKED_IN order on room id 1 (A101), assignable rooms for type 1 are A102, A103. Test data cleaned up.

Also run mvn test? There's no test dir — mvn test would just compile. Compile already passed. Stop the background server now.

</details>

#### Tool Call: TaskStop (`bash-a2p6626x`)
<!-- call_id: tool_5MNrAfUe8MIOVIUMPOQN6OIp -->
```json
{
  "task_id": "bash-a2p6626x",
  "reason": "T6 测试完成，停止开发服务"
}
```

<details><summary>Tool Result: TaskStop (`bash-a2p6626x`)</summary>

<!-- call_id: tool_5MNrAfUe8MIOVIUMPOQN6OIp -->
Wall time: 67.475 seconds
task_id: bash-a2p6626x
status: killed
reason: T6 测试完成，停止开发服务

</details>

<details><summary>Thinking</summary>

Done. Report results. Should I update the task list 编码任务清单.md marking T6 complete? The user said "不要进入下一个任务" — updating the checklist for T6 status is reasonable, but per AGENTS.md "每个阶段完成后必须停下来等用户确认". The task list is updated at验收. T5 was marked ✅ after 验收. Maybe I should not mark it ✅ until user confirms. I'll report and let user decide; or I can leave the doc untouched and mention it. Safer: report results, ask nothing, mention I haven't updated the checklist or committed yet — awaiting confirmation. Actually previous pattern: "T1~T5 已完成并验收" — updated after验收. So I'll leave the file as is and tell the user.

</details>

T6 已完成，测试结果全部通过。

## 实现内容（room 模块）

- `room/entity/RoomType.java`、`room/entity/Room.java` — 对应 room_type / room 表
- `room/mapper/RoomTypeMapper.java`、`room/mapper/RoomMapper.java` — RoomMapper 含 `selectAssignable(typeId)`，用 `NOT IN (在住订单)` 推导空闲房间（房间状态不入库）
- `room/RoomService.java` — `listRoomTypes` / `getRoomType`（不存在抛 1007）/ `countRooms` / `listAssignableRooms`（供 T9 用）
- `room/RoomTypeController.java` — `GET /api/room-types`、`GET /api/room-types/{id}`（住客只读，不含剩余量）
- `room/dto/RoomTypeInfo.java` — 元→分换算收敛在 DTO 序列化层（`movePointRight(2).longValueExact()`），接口输出"分"整数

未登录访问已由既有 `AuthInterceptor` 的公开规则（GET `/api/room-types`）放行，无需改动。

## 测试结果（实测通过）

| 用例 | 结果 |
| --- | --- |
| TC-F01① / TC-A01① 未登录 `GET /api/room-types` | ✅ 返回 4 个预置房型，`code:0`，金额为分（288.50 元 → `28850`） |
| TC-A01② 未登录 `GET /api/room-types/1` | ✅ 返回大床房详情，`price: 28850` |
| 附加：不存在的房型 `/999` | ✅ `{"code":1007,"message":"房型不存在"}` |
| 附加：`selectAssignable` SQL 验证 | ✅ 插入一笔 A101 的 CHECKED_IN 订单后可分配房间为 A102/A103（测试数据已清理） |

`mvn compile` 通过。项目暂无 `src/test` 目录（并发集成测试从 T7 开始引入），以上按 T5 的方式用真实服务 + curl/DB 实测。

编码任务清单中 T6 状态我暂未改为 ✅，等你验收确认后再更新；也未提交 git。

## Turn 2

### User

提交 然后继续 T7

### Assistant

<details><summary>Thinking</summary>

User asks to commit T6, then continue T7 (inventory module).

Commit message format: `阶段: 简述`. Stage is 编码测试. Previous commit af5b058 was presumably "编码测试: ..." Let me check git log style, then commit.

Then T7: inventory module — InventoryService with queryAvailability, tryOccupy, release. Concurrency integration test (TC-G01 inventory part, TC-B01/B02 query semantics). Acceptance: N(≥5) threads concurrent tryOccupy on same range, successes exactly = room count, occupied never exceeds total; queryAvailability boundary correct when no inventory rows.

This requires introducing src/test with spring-boot-starter-test (already whitelisted). Tests need MySQL running (per AGENTS.md: mvn test 需 MySQL 已启动).

Let me commit first.

</details>

#### Tool Call: Bash (`git log --oneline -5; git status --short`)
<!-- call_id: tool_IexK6pewwlGmQDzCG14Vg759 -->
```json
{
  "command": "git log --oneline -5; git status --short"
}
```

<details><summary>Tool Result: Bash (`git log --oneline -5; git status --short`)</summary>

<!-- call_id: tool_IexK6pewwlGmQDzCG14Vg759 -->
af5b058 编码测试: 工程骨架、数据库脚本、公共层与auth模块
2bdc133 测试用例: 输出覆盖主线/异常/并发的测试用例
66b0d47 技术方案: 同步AGENTS.md技术栈与编码规范
d47d476 技术方案: 输出技术方案文档
9eb49a3 需求方案: 评审修订PRD与原型至v1.1（明确库存逐日口径、代订、退改差价等规则）
?? ai-process/conversations/kimi-export-session_-20261004-085925.md
?? "ai-process/\347\274\226\347\240\201\344\273\273\345\212\241\346\270\205\345\215\225.md"
?? hotel-server/src/main/java/com/example/hotel/room/


</details>

<details><summary>Thinking</summary>

The 编码任务清单.md is untracked — it was created during the previous coding stage but never committed? It says progress snapshot at commit af5b058 but the file itself is untracked. Should I include it in this commit? It documents the coding stage; commit it too, with T6 marked done. Let me update the checklist (T6 → ✅) since user confirmed ("提交" implies acceptance), then commit both. Also the conversations export file — leave untracked? It appeared as untracked; not mine to decide... It's a session export, probably auto-generated. I'll leave it out (or maybe it should be committed?). Safer to only commit the checklist + room code. Actually, maybe add conversations export too? I'll leave it — not part of my change.

</details>

用户已确认 T6，先更新任务清单中 T6 状态，然后提交：

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_RxJStd8nqko8OY6sMY0aYYu9 -->
```json
{
  "path": "ai-process/编码任务清单.md",
  "old_string": "> 进度快照：2026-10-04。**T1~T5 已完成并验收**（commit `af5b058`），下一个任务从 **T6** 开始。",
  "new_string": "> 进度快照：2026-10-04。**T1~T6 已完成并验收**（T1~T5 commit `af5b058`），下一个任务从 **T7** 开始。"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_RxJStd8nqko8OY6sMY0aYYu9 -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_atir0oJ3KnwEbG9mqyrc4FYK -->
```json
{
  "new_string": "| T5 auth 模块 | ✅ 完成 | 注册/登录/登出/me/resolveUser，TC-A02~A10、H02 API 部分已实测通过 |\n| T6 room 模块（住客侧只读） | ✅ 完成 | room_type/room 实体与 mapper、两个只读接口、listAssignableRooms；TC-F01①、TC-A01①② 实测通过 |\n| T7~T12 | ⬜ 待做 | 见下方任务定义 |",
  "old_string": "| T5 auth 模块 | ✅ 完成 | 注册/登录/登出/me/resolveUser，TC-A02~A10、H02 API 部分已实测通过 |\n| T6~T12 | ⬜ 待做 | 见下方任务定义 |",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_atir0oJ3KnwEbG9mqyrc4FYK -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_IQTEDL1iC6U4i5X4XeKlPBlS -->
```json
{
  "new_string": "### T6 room 模块（住客侧只读 + 实体/mapper）✅（已验收）",
  "old_string": "### T6 room 模块（住客侧只读 + 实体/mapper）",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_IQTEDL1iC6U4i5X4XeKlPBlS -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Bash (`git add "ai-process/编码任务清单.md" hotel-server/src/main/java/co…`)
<!-- call_id: tool_LRZ7KCYngjmsLrLJZdoFDr5K -->
```json
{
  "command": "git add \"ai-process/编码任务清单.md\" hotel-server/src/main/java/com/example/hotel/room && git commit -m \"编码测试: room模块（住客侧只读接口与可分配房间推导）\" && git log --oneline -1"
}
```

<details><summary>Tool Result: Bash (`git add "ai-process/编码任务清单.md" hotel-server/src/main/java/co…`)</summary>

<!-- call_id: tool_LRZ7KCYngjmsLrLJZdoFDr5K -->
[main 739e4de] 编码测试: room模块（住客侧只读接口与可分配房间推导）
 8 files changed, 359 insertions(+)
 create mode 100644 "ai-process/\347\274\226\347\240\201\344\273\273\345\212\241\346\270\205\345\215\225.md"
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/RoomService.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/RoomTypeController.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/dto/RoomTypeInfo.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/entity/Room.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/entity/RoomType.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/mapper/RoomMapper.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/room/mapper/RoomTypeMapper.java
739e4de 编码测试: room模块（住客侧只读接口与可分配房间推导）


</details>

<details><summary>Thinking</summary>

Committed as 739e4de. Now T7: inventory module.

Requirements:
- `InventoryService` three methods:
  - `queryAvailability(typeId, checkin, checkout)` — 逐日最小剩余量; 无库存行按当前房间数计
  - `tryOccupy(typeId, checkin, checkout)` — 按日期升序原子条件更新; INSERT 撞唯一索引重试一次; 失败抛业务异常（整单回滚由调用方事务保证）
  - `release(typeId, checkin, checkout)` — 逐日 −1 带 >0 保护
- Needs room count: inventory → cannot depend on order; can it depend on room? Dependency rules: "跨模块调用只允许 order → inventory、order → room". So inventory cannot call room service! But tryOccupy needs "total_count 取当前房间数" and queryAvailability needs "无库存行按当前房间数计". Hmm.

How to get room count without depending on room module? Inventory could query the room table via its own... no, mapper 不跨模块使用 means room's mapper isn't used by inventory. But inventory could have its own query? That's gray. Options:

1. InventoryMapper or a small query in inventory module counting room table directly (SQL in inventory module). This breaks module layering somewhat but keeps the one-direction dependency rule literal.
2. Pass total count as parameter from the caller (order service). But tryOccupy is called by order → order can call room (allowed). So `tryOccupy(typeId, checkin, checkout, totalRooms)`? Hmm, but then the concurrency test calling InventoryService directly would supply total. Also queryAvailability — who calls it? The availability endpoint `/api/room-types/availability` — that's probably served by... which controller? It's a guest read endpoint under /api/room-types. But room module can't call inventory (only order → inventory allowed). Hmm, then who composes room types + availability? 

Wait — dependency rule says controller 只依赖本模块 service; cross-module only order → inventory, order → room. The availability endpoint query semantics: TC-B01/B02. Perhaps the availability endpoint belongs to... Actually maybe it's fine for the room controller or an order controller? Technical方案 §6.1 lists GET /api/room-types/availability. Hmm, but that would need room+inventory. Since room can't call inventory, maybe OrderController or an endpoint in order module serves it? Or maybe queryAvailability takes total as param.

Alternative pragmatic approach: inventory module queries the `room` table directly via its own mapper method (a @Select count). It's within inventory's seam — the room count is a data dependency. The task says "无库存行按当前房间数计" and "INSERT（total_count 取当前房间数）". For T7, T8 will call it. The acceptance test: concurrency test calls tryOccupy directly with N threads on same range; successes == room count; occupied never exceeds total.

Let me think about what's cleanest given the 分层纪律 in AGENTS.md: "mapper 不跨模块使用；跨模块调用只允许 order → inventory、order → room". Strictly, inventory cannot inject RoomMapper or RoomService. So inventory needs room count another way: either SQL in its own mapper (reading another module's table — a data-level coupling, arguably against the spirit), or accept it as a parameter.

Design decision: `tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout, int totalRooms)` and `queryAvailability(typeId, checkin, checkout, totalRooms)`. Hmm, but that leaks "房间数" knowledge to callers. Actually the deep-module design says inventory hides "逐日库存行的创建与并发原子占用". The caller (order) already knows room count via room service. Passing totalRooms as parameter is clean and respects dependency rules. The concurrency test can call tryOccupy(typeId, d1, d2, 3) directly with the room count — fine.

But wait, the task says "无库存行按当前房间数计" — with param, the caller supplies current room count. OK.

Alternatively query availability semantics: for each date in [checkin, checkout): remaining = row ? total-occupied : totalRooms; result = min. Also for tryOccupy: for each date ascending:
1. UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id=? AND stay_date=? AND occupied_count < total_count
2. if 0 rows → INSERT (room_type_id, stay_date, total_count=totalRooms, occupied_count=1); on duplicate key → retry UPDATE once; if still 0 → throw BizException(SOLD_OUT)

Wait careful: INSERT with occupied_count=1 directly? Yes — first occupy creates the row already occupied by 1. If INSERT succeeds, that date is occupied. If duplicate key → someone created concurrently → retry the conditional UPDATE once.

release: for each date ascending: UPDATE ... SET occupied_count = occupied_count - 1 WHERE room_type_id=? AND stay_date=? AND occupied_count > 0. (Row should exist.)

Transactions: tryOccupy's rollback is by caller's transaction (T8 order creation). Should InventoryService methods be @Transactional? The task says "失败整单回滚由调用方事务保证". If InventoryService.tryOccupy is annotated @Transactional(propagation=REQUIRED) it joins caller's tx. Without caller tx, each method has its own tx. For the concurrency test, calling tryOccupy directly: without @Transactional, each UPDATE is auto-committed — that's fine for the test since we count successes. But if tryOccupy partially fails mid-range without a surrounding tx, earlier dates stay occupied — in the test that pollutes. For the test, better to have the test manage cleanup or use tryOccupy where all succeed except the overflow ones... Actually in concurrency test: N threads, room count R, N > R. Some threads fail → their partial occupies? All threads occupy the same date range; the first R succeed fully; threads R+1..N fail on day 1 (occupied == total) → no partial occupies since they fail on the first date. Good, no residue if every thread fails on first day. But without per-method transaction, the failed threads' update affects 0 rows — nothing written. OK.

But to be safe and match "整单回滚由调用方事务保证", annotate service methods @Transactional — when called from test without outer tx, each tryOccupy runs in its own tx: on failure (SOLD_OUT thrown after day k), days before k roll back. Good — no residue. When called from order's tx (REQUIRED default), joins it. That's the right design.

Entity DailyInventory: id, roomTypeId, stayDate, totalCount, occupiedCount.

InventoryMapper: BaseMapper + custom update methods:

```java
@Update("UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = #{typeId} AND stay_date = #{date} AND occupied_count < total_count")
int tryOccupyOne(@Param("typeId") Long typeId, @Param("date") LocalDate date);

@Update("UPDATE daily_inventory SET occupied_count = occupied_count - 1 WHERE room_type_id = #{typeId} AND stay_date = #{date} AND occupied_count > 0")
int releaseOne(...);
```

INSERT: use BaseMapper insert(entity) and catch DuplicateKeyException.

queryAvailability: selectList where room_type_id and stay_date between checkin and checkout-1... Use QueryWrapper: eq typeId, ge checkin, lt checkout. Then map by date; for each date in range compute remaining; min.

Where does queryAvailability get called from in T8/T10? The availability endpoint — T8 or T10 will decide. For T7 we just implement the service + tests.

Tests: introduce src/test/java with spring-boot-starter-test (in whitelist, check pom). Concurrency test: 
- TC-G01 (inventory part): N threads (e.g., 10) tryOccupy same type/date range, room count R (use a room type with known count, e.g. type 3 家庭房 has 2 rooms). Successes == 2, occupied_count == 2, never exceeds.
- TC-B01/B02 query semantics: queryAvailability with no inventory rows returns room count; boundary correct.

Let me check TC-B01/B02 and TC-G01 in docs/03.

</details>

提交完成（`739e4de`）。现在开始 T7（inventory 防超卖模块）。先看相关测试用例定义和 pom 依赖：

#### Tool Call: Grep (`docs/03-测试用例.md`)
<!-- call_id: tool_4LPWaLD40TRi0iwTDqR4trZF -->
```json
{
  "-C": 2,
  "output_mode": "content",
  "path": "docs/03-测试用例.md",
  "pattern": "TC-G01|TC-B01|TC-B02"
}
```

#### Tool Call: Read (`hotel-server/pom.xml`)
<!-- call_id: tool_M7ujbEFE2XvYTMrpnjMrTkT2 -->
```json
{
  "path": "hotel-server/pom.xml"
}
```

<details><summary>Tool Result: Read (`hotel-server/pom.xml`)</summary>

<!-- call_id: tool_M7ujbEFE2XvYTMrpnjMrTkT2 -->
1	<?xml version="1.0" encoding="UTF-8"?>
2	<project xmlns="http://maven.apache.org/POM/4.0.0"
3	         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
4	         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
5	    <modelVersion>4.0.0</modelVersion>
6	
7	    <parent>
8	        <groupId>org.springframework.boot</groupId>
9	        <artifactId>spring-boot-starter-parent</artifactId>
10	        <version>3.4.1</version>
11	        <relativePath/>
12	    </parent>
13	
14	    <groupId>com.example</groupId>
15	    <artifactId>hotel-server</artifactId>
16	    <version>1.0.0</version>
17	    <name>hotel-server</name>
18	
19	    <properties>
20	        <java.version>17</java.version>
21	    </properties>
22	
23	    <dependencies>
24	        <dependency>
25	            <groupId>org.springframework.boot</groupId>
26	            <artifactId>spring-boot-starter-web</artifactId>
27	        </dependency>
28	        <dependency>
29	            <groupId>org.springframework.boot</groupId>
30	            <artifactId>spring-boot-starter-validation</artifactId>
31	        </dependency>
32	        <dependency>
33	            <groupId>com.baomidou</groupId>
34	            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
35	            <version>3.5.9</version>
36	        </dependency>
37	        <dependency>
38	            <groupId>com.mysql</groupId>
39	            <artifactId>mysql-connector-j</artifactId>
40	            <scope>runtime</scope>
41	        </dependency>
42	        <dependency>
43	            <groupId>org.springframework.security</groupId>
44	            <artifactId>spring-security-crypto</artifactId>
45	        </dependency>
46	        <dependency>
47	            <groupId>org.springframework.boot</groupId>
48	            <artifactId>spring-boot-starter-test</artifactId>
49	            <scope>test</scope>
50	        </dependency>
51	    </dependencies>
52	
53	    <build>
54	        <finalName>hotel-server</finalName>
55	        <plugins>
56	            <plugin>
57	                <groupId>org.springframework.boot</groupId>
58	                <artifactId>spring-boot-maven-plugin</artifactId>
59	            </plugin>
60	        </plugins>
61	    </build>
62	</project>

</details>

<details><summary>Tool Result: Grep (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_4LPWaLD40TRi0iwTDqR4trZF -->
docs/03-测试用例.md-53-## 3. 浏览与预订（AC-05~AC-08、BR-01~BR-03）
docs/03-测试用例.md-54-
docs/03-测试用例.md:55:> 预订提交的并发控制用例（防超卖、并发抢最后一间）见 §8 TC-G01~G03，本节 TC-B12 覆盖单线程下的库存回滚。
docs/03-测试用例.md-56-
docs/03-测试用例.md-57-| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
docs/03-测试用例.md-58-| --- | --- | --- | --- | --- | --- |
docs/03-测试用例.md:59:| TC-B01 | 按日期查询剩余可订数量 | 大床房 A 共 3 间，所选区间无订单 | 在房型列表选择入住 `D`、离店 `D+2`，查询 | 各房型展示剩余可订数量，大床房 A 显示 3 | AC-05、UC-02 |
docs/03-测试用例.md:60:| TC-B02 | 剩余为 0 展示不可订 | 大床房 A 在 `D~D+2` 已被订满 3 间（可通过先创建 3 笔订单构造） | 用相同日期区间查询可订状态 | 大床房 A 展示"不可订"，预订按钮不可点击/无法提交 | AC-05、UC-02 异常 |
docs/03-测试用例.md-61-| TC-B03 | 日期区间非法 | 住客甲已登录 | ① 离店日期 = 入住日期；② 离店日期早于入住日期 | 均无法提交，提示"离店日期必须晚于入住日期" | AC-06、BR-01 |
docs/03-测试用例.md-62-| TC-B04 | 入住日期早于当天 | 住客甲已登录 | 入住日期选择 `D-1` 提交预订 | 拒绝并提示，不生成订单 | BR-01 |
--
docs/03-测试用例.md-124-| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
docs/03-测试用例.md-125-| --- | --- | --- | --- | --- | --- |
docs/03-测试用例.md:126:| TC-G01 | 并发预订防超卖（核心） | 大床房 A 共 3 间，清空区间 D~D+2 的全部订单与库存行（剩余量 = 3） | 用 N（≥ 5）个不同住客账号同时提交同房型同区间预订 | ① 恰好 3 笔成功，其余全部失败并提示"该房型所选日期已订满"；② `daily_inventory` 中每日 `occupied_count` ≤ 3（不超卖）；③ 数据库中该区间有效订单数 = 3；④ 失败方无残留库存占用（事务整体回滚） | AC-08、BR-02、技术方案 §5.1 |
docs/03-测试用例.md-127-| TC-G02 | 并发预订仅剩 1 间 | 大床房 A 区间 D~D+2 仅剩 1 间可订 | 住客甲、乙同时提交该区间预订 | 一笔成功、一笔失败提示已订满；成功订单数不超过物理房间数 | AC-08、UC-03 异常 |
docs/03-测试用例.md-128-| TC-G03 | 并发预订 + 取消交叉 | 大床房 A 某区间仅剩 1 间；住客甲持有一笔该区间"已确认"订单 O1 | 同时发起：① 甲取消 O1；② 乙、丙各提交 1 笔该区间预订 | 终态自洽：乙丙中至多 1 笔成功；每日 `occupied_count` 恒等于"已确认+已入住"订单覆盖数（不多不少）；无负库存 | BR-02/04、技术方案 §5.1 |
--
docs/03-测试用例.md-152-| AC-03 | TC-A06、TC-C05、TC-H03 |
docs/03-测试用例.md-153-| AC-04 | TC-A07、TC-A09 |
docs/03-测试用例.md:154:| AC-05 | TC-B01、TC-B02 |
docs/03-测试用例.md-155-| AC-06 | TC-B03（另 TC-B04 覆盖 BR-01 日期下界） |
docs/03-测试用例.md-156-| AC-07 | TC-B05（另 TC-B08、TC-B10 补充晚数与金额口径） |
docs/03-测试用例.md:157:| AC-08 | TC-G01、TC-G02（另 TC-G03 覆盖与取消交叉） |
docs/03-测试用例.md-158-| AC-09 | TC-C01、TC-C02 |
docs/03-测试用例.md-159-| AC-10 | TC-C03、TC-C04 |

</details>

<details><summary>Thinking</summary>

Good — spring-boot-starter-test already in pom (whitelisted). Now implement inventory module:

- `inventory/entity/DailyInventory.java`
- `inventory/mapper/InventoryMapper.java`
- `inventory/InventoryService.java`

Design per earlier reasoning: methods take totalRooms param? Or inventory queries room count itself?

AGENTS.md 硬规则: "mapper 不跨模块使用；跨模块调用只允许 order → inventory、order → room". So InventoryService cannot inject RoomService/RoomMapper. Options:

A. Pass totalRooms as param: `tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout, int totalRooms)`, `queryAvailability(..., int totalRooms)`.
B. InventoryMapper has its own @Select count on room table.

Option A keeps data flow explicit and dependency rule clean. The caller in T8 (order service) gets room count via room service (`countRooms` — I already added that to RoomService in T6). And the availability endpoint: /api/room-types/availability — who serves it? RoomTypeController can't call inventory. Per dependency rules, that endpoint must live in order module? Hmm, "controller 只依赖本模块 service" — an order controller serving /api/room-types/availability is odd but possible. Or a dedicated controller in... Actually maybe availability should be part of OrderController or a query endpoint. That's a T8/T10 decision. For T7, implement InventoryService with the param design.

Hmm wait, but the task says "无库存行按当前房间数计" — "当前房间数" supplied by caller as param is fine.

Actually, let me reconsider: maybe inventory could have a small RoomCountProvider? Overengineering. Go with param.

Concurrency test calls: tryOccupy(typeId, d, d+2, 3) with 10 threads → 3 success.

But note: with @Transactional on tryOccupy and concurrency, when thread 4 fails on day1 and throws, tx rolls back — fine.

One subtlety: @Transactional + DuplicateKeyException catch — catching DuplicateKeyException inside a transaction in MySQL: after a duplicate key error, the transaction is NOT aborted in MySQL (unlike PostgreSQL), so we can continue and retry the UPDATE within same tx. Good.

Also INSERT: set total_count = totalRooms param, occupied_count = 1.

queryAvailability: fetch rows for range, build map, iterate dates, min remaining. Return int (remaining ≥ 0).

Edge: if checkin >= checkout? Service could just... BR-01 validation is order module's job. For robustness, if range empty return totalRooms? Let it return... I'll not add extra validation — single responsibility; order validates. But queryAvailability with empty range would return totalRooms (min of nothing). Fine.

Tests: create src/test/java/com/example/hotel/inventory/InventoryServiceTest.java with @SpringBootTest. Needs MySQL up (docker compose up -d). Test cases:

1. TC-B01/B02 semantics: queryAvailability with no rows → room count (3 for 大床房 type 1); after occupying, decreases.
2. TC-G01 inventory part: 10 threads tryOccupy same range with totalRooms=3 → exactly 3 successes, occupied_count per day ≤ 3 and == 3, failures got SOLD_OUT.
3. release: occupy then release → back to 0.

Cleanup: tests must clean up daily_inventory rows they create (delete where room_type_id = X and stay_date in test range). Use unique far-future dates to avoid clashing with seed/demo data? Seed has no inventory rows. Use dates like 2099-01-01. Cleanup in @AfterEach via mapper delete.

Test style: JUnit 5. Use CountDownLatch to start threads simultaneously, ExecutorService. Count successes with AtomicInteger. Assert DB state via mapper.

Let me also verify GlobalExceptionHandler handles BizException — already exists from T3.

Naming: InventoryService methods per task: queryAvailability(typeId, checkin, checkout), tryOccupy, release. With totalRooms param... Hmm, wait. Let me reconsider whether the seam should include totalRooms. §3.4: inventory interface `queryAvailability(typeId, checkin, checkout)` / `tryOccupy(...)` / `release(...)`. The doc shows queryAvailability with 3 args. If inventory needs room count and can't call room... The doc's signature suggests inventory resolves room count itself. How? It could read the room table directly via SQL in InventoryMapper — data-level read of another aggregate's table. The layering rule says mapper 不跨模块使用 (room's mapper not used elsewhere), but it doesn't explicitly forbid a module querying another table via its own mapper. Given §3.4 explicitly documents the 3-arg signature, and the room count is needed in two places, I think adding `selectRoomCount(typeId)` to InventoryMapper (a @Select count on room table) is the pragmatic choice matching the documented interface.

Hmm, but 分层纪律 says "跨模块调用只允许 order → inventory、order → room". Inventory reading room table via own SQL isn't a "调用" of room module. The room module owns room business rules; inventory just needs a count. I'll go with option B to match documented signatures.

Actually wait — there's a wrinkle: queryAvailability "无库存行按当前房间数计". Also tryOccupy INSERT uses 当前房间数. Both need the count. OK, add to InventoryMapper:

```java
@Select("SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}")
int countRooms(Long typeId);
```

Hmm, that duplicates RoomService.countRooms logic. Acceptable trade-off to keep dependency direction. Alternatively define signatures with totalRooms... Let me decide firmly: the tech doc §3.4 explicitly lists the interface as `queryAvailability(typeId, checkin, checkout) / tryOccupy(...) / release(...)`. Follow the doc. InventoryMapper.countRooms it is.

Hmm, but one more consideration: RoomService.countRooms I added in T6 — T9 will use it for maintenance (UC-09). Keep both; fine.

Now write code.

DailyInventory entity:
```java
@TableName("daily_inventory")
public class DailyInventory {
    @TableId(type = IdType.AUTO) Long id;
    Long roomTypeId;
    LocalDate stayDate;
    Integer totalCount;
    Integer occupiedCount;
}
```

InventoryMapper:
```java
public interface InventoryMapper extends BaseMapper<DailyInventory> {
    @Update("UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count < total_count")
    int occupyOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);

    @Update("UPDATE daily_inventory SET occupied_count = occupied_count - 1 WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count > 0")
    int releaseOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);

    @Select("SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}")
    int countRooms(@Param("typeId") Long typeId);
}
```

InventoryService:
```java
@Service
public class InventoryService {
    public int queryAvailability(Long typeId, LocalDate checkin, LocalDate checkout) {
        int totalRooms = inventoryMapper.countRooms(typeId);
        List<DailyInventory> rows = inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
            .eq("room_type_id", typeId).ge("stay_date", checkin).lt("stay_date", checkout));
        Map<LocalDate, DailyInventory> byDate = ...;
        int min = totalRooms;
        for (LocalDate d = checkin; d.isBefore(checkout); d = d.plusDays(1)) {
            DailyInventory row = byDate.get(d);
            int remaining = row == null ? totalRooms : row.getTotalCount() - row.getOccupiedCount();
            min = Math.min(min, remaining);
        }
        return min;
    }

    @Transactional
    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
        for (LocalDate d = checkin; d.isBefore(checkout); d = d.plusDays(1)) {
            occupyOneDate(typeId, d);
        }
    }

    private void occupyOneDate(Long typeId, LocalDate date) {
        if (inventoryMapper.occupyOne(typeId, date) == 1) return;
        // 行不存在（首次预订该日）或已订满：先尝试建行
        DailyInventory row = new DailyInventory();
        row.setRoomTypeId(typeId); row.setStayDate(date);
        row.setTotalCount(inventoryMapper.countRooms(typeId));
        row.setOccupiedCount(1);
        try {
            inventoryMapper.insert(row);
            return;
        } catch (DuplicateKeyException e) {
            // 并发已建行，重试一次原子条件更新
        }
        if (inventoryMapper.occupyOne(typeId, date) != 1) {
            throw new BizException(ErrorCode.SOLD_OUT);
        }
    }

    @Transactional
    public void release(Long typeId, LocalDate checkin, LocalDate checkout) {
        for (LocalDate d = checkin; d.isBefore(checkout); d = d.plusDays(1)) {
            inventoryMapper.releaseOne(typeId, d);
        }
    }
}
```

Wait — subtle bug in tryOccupy logic: if occupyOne returns 0 because the row exists but is full, we then attempt INSERT which throws DuplicateKeyException, then retry occupyOne → still 0 → SOLD_OUT. Correct but wasteful; matches spec: "影响行数 = 1 → 成功；= 0 → 该行可能不存在，尝试 INSERT；撞唯一索引回到第 1 步重试一次；重试后仍为 0 → 订满". Matches §5.1 exactly. Good.

One concern: catching DuplicateKeyException inside @Transactional in Spring — DataAccessException doesn't mark tx rollback-only unless it propagates through a transactional proxy boundary. Catching it inside the method is fine in MySQL.

Also for the concurrency test with 10 threads calling tryOccupy (each own tx): MySQL row locks — the conditional UPDATE takes a row lock on the inventory row; concurrent txs serialize on it. Each does +1 if occupied < total. Exactly 3 succeed per day. 

Test file: `src/test/java/com/example/hotel/inventory/InventoryServiceTest.java`

```java
@SpringBootTest
class InventoryServiceTest {
    @Autowired InventoryService inventoryService;
    @Autowired InventoryMapper inventoryMapper;

    private static final Long TYPE_ID = 1L; // 大床房 3 间
    private static final LocalDate D1 = LocalDate.of(2099,1,1);
    private static final LocalDate D2 = LocalDate.of(2099,1,3); // 两晚

    @AfterEach cleanup: delete rows for type 1 in [2099-01-01, 2099-01-03)

    @Test TC-B01: queryAvailability no rows == 3; after tryOccupy once == 2 (TC-B02 semantics: occupy 3 → 0)

    @Test TC-G01: 10 threads, latch start, count successes == 3, failures BizException SOLD_OUT code 1001; DB occupied per day == 3 ≤ 3; no residue from failed (they fail at day1 so nothing; also verify total occupied == 3 each day)

    @Test release: occupy 1, release, availability back to 3, occupied_count == 0
}
```

Tests need MySQL running: docker compose up -d (already running). mvn test.

Note @SpringBootTest will run schema.sql/data.sql (sql.init mode always) — idempotent, fine.

Also should the concurrency test use more threads — N=10 ≥ 5 ✓.

Failure message check: BizException message "该房型所选日期已订满".

Edge: threads that fail on day 1 have nothing to roll back; but what if a thread occupies day1 then fails day2? With 10 threads all grabbing the same 2-day range, the 3 winners occupy both days. Other threads fail on day 1 (full). But interleaving: thread A occupies day1, then thread B occupies day1... 3 successes for day1 among A,B,C. When A moves to day2: 3 slots. Could thread A succeed day1 but fail day2? Day2 updates happen-after day1 in each thread; at most 3 threads hold day1 slots, so day2 also has 3 slots for those 3 threads — they can't fail. Actually could a 4th thread grab day2 before some day1-holder gets there? No — only day1-holders proceed to day2, exactly ≤3 of them, and day2 has 3 slots. So no partial residue. Good.

Also add per-day occupied assertion: query rows, assert each occupied_count == 3 and <= total_count == 3.

Write the files now.

</details>

`spring-boot-starter-test` 已在 pom 白名单依赖中，可以直接写集成测试。现在实现 inventory 模块：

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/entit…`)
<!-- call_id: tool_x8PvFPHBEfehSPlU4V6VDaau -->
```json
{
  "content": "package com.example.hotel.inventory.entity;\n\nimport com.baomidou.mybatisplus.annotation.IdType;\nimport com.baomidou.mybatisplus.annotation.TableId;\nimport com.baomidou.mybatisplus.annotation.TableName;\n\nimport java.time.LocalDate;\n\n@TableName(\"daily_inventory\")\npublic class DailyInventory {\n\n    @TableId(type = IdType.AUTO)\n    private Long id;\n    private Long roomTypeId;\n    private LocalDate stayDate;\n    private Integer totalCount;\n    private Integer occupiedCount;\n\n    public Long getId() {\n        return id;\n    }\n\n    public void setId(Long id) {\n        this.id = id;\n    }\n\n    public Long getRoomTypeId() {\n        return roomTypeId;\n    }\n\n    public void setRoomTypeId(Long roomTypeId) {\n        this.roomTypeId = roomTypeId;\n    }\n\n    public LocalDate getStayDate() {\n        return stayDate;\n    }\n\n    public void setStayDate(LocalDate stayDate) {\n        this.stayDate = stayDate;\n    }\n\n    public Integer getTotalCount() {\n        return totalCount;\n    }\n\n    public void setTotalCount(Integer totalCount) {\n        this.totalCount = totalCount;\n    }\n\n    public Integer getOccupiedCount() {\n        return occupiedCount;\n    }\n\n    public void setOccupiedCount(Integer occupiedCount) {\n        this.occupiedCount = occupiedCount;\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/entity/DailyInventory.java"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_uc5YxwOCoHDi9qHdnUzc6JiQ -->
```json
{
  "content": "package com.example.hotel.inventory.mapper;\n\nimport com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Select;\nimport org.apache.ibatis.annotations.Update;\n\nimport java.time.LocalDate;\n\npublic interface InventoryMapper extends BaseMapper<DailyInventory> {\n\n    @Update(\"UPDATE daily_inventory SET occupied_count = occupied_count + 1 \"\n            + \"WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count < total_count\")\n    int occupyOne(@Param(\"typeId\") Long typeId, @Param(\"stayDate\") LocalDate stayDate);\n\n    @Update(\"UPDATE daily_inventory SET occupied_count = occupied_count - 1 \"\n            + \"WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count > 0\")\n    int releaseOne(@Param(\"typeId\") Long typeId, @Param(\"stayDate\") LocalDate stayDate);\n\n    @Select(\"SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}\")\n    int countRooms(@Param(\"typeId\") Long typeId);\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_n0kkMOwRviJJSssoXnUutGCf -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;\nimport com.example.hotel.common.api.ErrorCode;\nimport com.example.hotel.common.exception.BizException;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.springframework.dao.DuplicateKeyException;\nimport org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Transactional;\n\nimport java.time.LocalDate;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.stream.Collectors;\n\n@Service\npublic class InventoryService {\n\n    private final InventoryMapper inventoryMapper;\n\n    public InventoryService(InventoryMapper inventoryMapper) {\n        this.inventoryMapper = inventoryMapper;\n    }\n\n    /** 区间 [checkin, checkout) 剩余可订量：逐日剩余的最小值；无库存行的日期按当前房间数计 */\n    public int queryAvailability(Long typeId, LocalDate checkin, LocalDate checkout) {\n        int totalRooms = inventoryMapper.countRooms(typeId);\n        List<DailyInventory> rows = inventoryMapper.selectList(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .ge(\"stay_date\", checkin)\n                .lt(\"stay_date\", checkout));\n        Map<LocalDate, DailyInventory> byDate = rows.stream()\n                .collect(Collectors.toMap(DailyInventory::getStayDate, row -> row));\n        int minRemaining = totalRooms;\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            DailyInventory row = byDate.get(date);\n            int remaining = row == null ? totalRooms : row.getTotalCount() - row.getOccupiedCount();\n            minRemaining = Math.min(minRemaining, remaining);\n        }\n        return minRemaining;\n    }\n\n    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     */\n    @Transactional\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            occupyOneDate(typeId, date);\n        }\n    }\n\n    /** 释放区间 [checkin, checkout) 每日 1 间，带 occupied_count > 0 保护，按日期升序 */\n    @Transactional\n    public void release(Long typeId, LocalDate checkin, LocalDate checkout) {\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            inventoryMapper.releaseOne(typeId, date);\n        }\n    }\n\n    private void occupyOneDate(Long typeId, LocalDate date) {\n        if (inventoryMapper.occupyOne(typeId, date) == 1) {\n            return;\n        }\n        // 影响 0 行：该行可能不存在（首次预订该日），尝试建行；撞唯一索引说明并发已建行\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 回到原子条件更新重试一次\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_n0kkMOwRviJJSssoXnUutGCf -->
Wrote 3508 bytes to hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/entit…`)</summary>

<!-- call_id: tool_x8PvFPHBEfehSPlU4V6VDaau -->
Wrote 1267 bytes to hotel-server/src/main/java/com/example/hotel/inventory/entity/DailyInventory.java

</details>

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_uc5YxwOCoHDi9qHdnUzc6JiQ -->
Wrote 1061 bytes to hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java

</details>

<details><summary>Thinking</summary>

Now write the integration test. It covers TC-G01 inventory assertions, TC-B01/B02 query semantics, and release.

</details>

现在编写 T7 的并发与查询语义集成测试：

#### Tool Call: Write (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_Di7P8UjrhXgN2wWZOYwKKmtW -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;\nimport com.example.hotel.common.api.ErrorCode;\nimport com.example.hotel.common.exception.BizException;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.junit.jupiter.api.AfterEach;\nimport org.junit.jupiter.api.Test;\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.boot.test.context.SpringBootTest;\n\nimport java.time.LocalDate;\nimport java.util.List;\nimport java.util.concurrent.CountDownLatch;\nimport java.util.concurrent.ExecutorService;\nimport java.util.concurrent.Executors;\nimport java.util.concurrent.TimeUnit;\nimport java.util.concurrent.atomic.AtomicInteger;\n\nimport static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertTrue;\n\n/**\n * 对应测试用例：TC-B01/B02（查询语义）、TC-G01 库存断言部分（并发防超卖）。\n * 大床房（room_type_id=1）预置 3 间房。\n */\n@SpringBootTest\nclass InventoryServiceTest {\n\n    private static final Long TYPE_ID = 1L;\n    private static final int TOTAL_ROOMS = 3;\n    private static final LocalDate CHECKIN = LocalDate.of(2099, 1, 10);\n    private static final LocalDate CHECKOUT = LocalDate.of(2099, 1, 12);\n\n    @Autowired\n    private InventoryService inventoryService;\n    @Autowired\n    private InventoryMapper inventoryMapper;\n\n    @AfterEach\n    void cleanup() {\n        inventoryMapper.delete(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", TYPE_ID)\n                .ge(\"stay_date\", CHECKIN)\n                .lt(\"stay_date\", CHECKOUT));\n    }\n\n    @Test\n    void queryAvailability_noInventoryRow_countsAsCurrentRoomCount() {\n        // TC-B01：无库存行时按当前房间数计\n        assertEquals(TOTAL_ROOMS, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n    }\n\n    @Test\n    void queryAvailability_dailyMinimum_andZeroWhenFull() {\n        // TC-B01/B02：剩余量取逐日最小值；订满后为 0\n        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKIN.plusDays(1));\n        assertEquals(TOTAL_ROOMS - 1, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n\n        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n        // 第二日只被占 2 间，区间最小值仍为 0（第一日已满）\n        assertThrows(BizException.class,\n                () -> inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT));\n    }\n\n    @Test\n    void release_returnsRoomToSellable() {\n        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n        assertEquals(TOTAL_ROOMS - 1, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n\n        inventoryService.release(TYPE_ID, CHECKIN, CHECKOUT);\n        assertEquals(TOTAL_ROOMS, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n        // 释放后占用数不为负\n        List<DailyInventory> rows = selectRows();\n        assertEquals(2, rows.size());\n        rows.forEach(row -> assertEquals(0, row.getOccupiedCount()));\n    }\n\n    @Test\n    void tryOccupy_concurrent_neverOversells() throws InterruptedException {\n        // TC-G01 库存断言部分：10 线程并发占用同区间，成功数恰为房间数、occupied 永不超 total\n        int threads = 10;\n        ExecutorService pool = Executors.newFixedThreadPool(threads);\n        CountDownLatch ready = new CountDownLatch(threads);\n        CountDownLatch start = new CountDownLatch(1);\n        AtomicInteger success = new AtomicInteger();\n        AtomicInteger soldOut = new AtomicInteger();\n        for (int i = 0; i < threads; i++) {\n            pool.submit(() -> {\n                ready.countDown();\n                try {\n                    start.await();\n                    inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n                    success.incrementAndGet();\n                } catch (BizException e) {\n                    assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());\n                    assertEquals(\"该房型所选日期已订满\", e.getMessage());\n                    soldOut.incrementAndGet();\n                } catch (InterruptedException e) {\n                    Thread.currentThread().interrupt();\n                }\n            });\n        }\n        assertTrue(ready.await(10, TimeUnit.SECONDS));\n        start.countDown();\n        pool.shutdown();\n        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));\n\n        assertEquals(TOTAL_ROOMS, success.get());\n        assertEquals(threads - TOTAL_ROOMS, soldOut.get());\n\n        // 每日 occupied_count 恰为 3 且不超过 total_count，失败方无残留占用\n        List<DailyInventory> rows = selectRows();\n        assertEquals(2, rows.size());\n        for (DailyInventory row : rows) {\n            assertEquals(TOTAL_ROOMS, row.getTotalCount());\n            assertEquals(TOTAL_ROOMS, row.getOccupiedCount());\n            assertTrue(row.getOccupiedCount() <= row.getTotalCount());\n        }\n        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));\n    }\n\n    private List<DailyInventory> selectRows() {\n        return inventoryMapper.selectList(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", TYPE_ID)\n                .ge(\"stay_date\", CHECKIN)\n                .lt(\"stay_date\", CHECKOUT)\n                .orderByAsc(\"stay_date\"));\n    }\n}\n",
  "path": "hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_Di7P8UjrhXgN2wWZOYwKKmtW -->
Wrote 5695 bytes to hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java

</details>

#### Tool Call: Bash (`docker compose ps --format '{{.Name}} {{.Status}}' && cd hot…`)
<!-- call_id: tool_aS94YmLXEgMDxjZKYZA1dwmn -->
```json
{
  "command": "docker compose ps --format '{{.Name}} {{.Status}}' && cd hotel-server && mvn -q test 2>&1 | tail -30",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`docker compose ps --format '{{.Name}} {{.Status}}' && cd hot…`)</summary>

<!-- call_id: tool_aS94YmLXEgMDxjZKYZA1dwmn -->
hotel-mysql Up 21 minutes
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.368 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.075 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <3> but was: <1>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertEquals.failNotEqual(AssertEquals.java:197)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:150)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:145)
	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:531)
	at com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells(InventoryServiceTest.java:113)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)

[ERROR] Failures: 
[ERROR]   InventoryServiceTest.tryOccupy_concurrent_neverOversells:113 expected: <3> but was: <1>
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.2:test (default-test) on project hotel-server: There are test failures.
[ERROR] 
[ERROR] See /Users/donglongjun/IdeaProjects/hotel-reservation-system/hotel-server/target/surefire-reports for the individual test results.
[ERROR] See dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException


</details>

<details><summary>Thinking</summary>

The concurrency test failed: at line 113 — `assertEquals(TOTAL_ROOMS, row.getOccupiedCount())` — expected 3 but was 1. So successes == 3 but DB shows occupied 1? Actually line 113 is inside the row loop: `assertEquals(TOTAL_ROOMS, row.getOccupiedCount())`. Rows: day1 occupied 3, day2 occupied 1? Hmm, wait. Success count assertion (line ~110) passed — 3 successes. But day rows show occupied 1?

Ah — this is the classic REPEATABLE READ problem with MyBatis-Plus... no wait, the UPDATE is atomic. Actually the issue: `tryOccupy` is @Transactional, called via proxy from test threads — fine. Each thread's tx: UPDATE ... SET occupied = occupied + 1 WHERE occupied < total. With 10 concurrent txs, MySQL row locks serialize. Each update re-reads the latest committed value (READ COMMITTED semantics for UPDATE? InnoDB locking reads use "semi-consistent read"... in REPEATABLE READ, UPDATE uses current read, sees latest committed + waits on lock). So 3 updates succeed, 4th blocks then re-evaluates condition → 0 rows.

But why day2 occupied = 1? Only 1 of the 3 day1 winners got day2? Hmm, that suggests... day2 row insert: 3 threads each INSERT the day2 row concurrently. Thread A inserts (occupied=1). Thread B's INSERT blocks on unique key (waiting for A's commit). When A commits, B gets DuplicateKeyException → retry occupyOne → occupied=2. C similarly → 3. That should work...

Unless: when thread A does INSERT for day2 while holding locks from day1... deadlock or lock wait? If B's INSERT is blocked waiting on A's uncommitted insert with same unique key, and A is... A proceeds fine. So B eventually gets dup key error after A commits. Retry update succeeds. Unless the retry happens while still in a tx whose snapshot... no, UPDATE is current read.

Wait — the failure was expected 3 but was 1 for a row. Which row? Could be day1 row = 3 ✓, day2 row = 1 ✗. Or day1 = 1?

Hmm, actually maybe the problem is different: test asserts rows in loop; first row (day1) might be 3 fine, second row day2 = 1.

Why would day2 end at 1 while 3 threads succeeded? If thread B and C's INSERTs blocked... then after A commits, B and C throw DuplicateKeyException, retry UPDATE → success → occupied 2, 3. Unless the exception wasn't DuplicateKeyException but something else — then tryOccupy would throw non-BizException, and the thread would... catch only BizException and InterruptedException; other exceptions propagate out of the lambda silently (ExecutorService swallows into Future). Success counter then < 3. But success == 3 passed? Line 110: assertEquals(TOTAL_ROOMS, success.get()) — that passed (failure at 113). So 3 threads succeeded but day2 shows 1.

Ohh — I see: duplicate-key error inside a transaction may leave... In MySQL, when an INSERT gets duplicate key error, it's fine. But under concurrency with INSERT waiting on unique index gap/next-key locks held by an uncommitted tx, when that tx commits, waiter gets DUPLICATE KEY. Then retry UPDATE works. That's standard.

Alternative explanation: REPEATABLE READ + UPDATE WHERE condition. The UPDATE re-reads, fine.

Hmm wait — maybe the issue is that the *three successful threads* each had their own transaction, and each thread's second-day INSERT... Only ONE of the 3 winners inserted day2 row; the other two got DuplicateKeyException, then retried occupyOne... but their transaction had earlier done UPDATE on day1 (acquired locks). The retry UPDATE on day2: at that moment, is the day2 row visible/lockable? After the inserter commits, yes. But there's a potential ordering: threads B and C blocked on unique index insert intention lock while A uncommitted. A commits → B and C each get duplicate key errors. They retry the UPDATE in the same tx → current read, see committed row, occupied=1 < 3 → +1. That should give 3.

Unless the retry happened BEFORE A committed? No — the dup key error only surfaces after A commits or rolls back.

Let me reconsider: maybe successes were 3 but not the same 3 threads for both days. Could thread X occupy day1, then on day2 INSERT conflict → retry UPDATE fail (0)? Retry fails only if occupied >= total at that moment. With 3 winners, day2 max 3. Hmm.

Wait, actually — maybe more than 3 threads passed day1! Scenario: all 10 threads initially find day1 row doesn't exist. Thread 1 does conditional UPDATE on nonexistent row → 0 rows. Then INSERTs row (total 3, occupied 1). Threads 2..10 also do conditional UPDATE on nonexistent row (0 rows), then all INSERT — block on unique key. Thread 1 moves to day2: UPDATE 0 rows (no row), INSERT day2 (occupied 1). Thread 1 commits both days. Now threads 2..10's day1 INSERTs fail with dup key → retry UPDATE day1 → 2 of them succeed (occupied 2, 3), 7 fail → SOLD_OUT → they roll back (no day changes... they hadn't touched day2 yet) → fine. Threads 2 and 3 proceed to day2: INSERT dup key (thread 1 already committed day2 row) → immediate error → retry UPDATE → occupied 2 → 3. Total day2 = 3. Still consistent!

Hmm so why 1? Let me think about lock wait timeout or deadlock. With 10 threads INSERTing the same unique key while others hold transaction locks... deadlock detection could kill some with "Deadlock found when trying to get lock". That's not BizException → escapes lambda → success < 3. But success == 3.

Actually wait, re-read the failure: line 113. Let me count lines in my file. The loop:

```
List<DailyInventory> rows = selectRows();
assertEquals(2, rows.size());
for (DailyInventory row : rows) {
    assertEquals(TOTAL_ROOMS, row.getTotalCount());     // line ~112
    assertEquals(TOTAL_ROOMS, row.getOccupiedCount());  // line 113
```

Hmm `assertEquals(2, rows.size())` passed. So both rows exist, one has occupied 1. totalCount passed as 3 for the row? The loop fails on first row maybe (day1 occupied=1?). 

New hypothesis — the real culprit: `@Transactional` + catching DuplicateKeyException and then... the JDBC connection in a tx that had a constraint violation — MySQL allows continuing. Fine.

Alternative hypothesis: the test itself raced — assertions run after pool termination, all txs committed. Committed data should be final.

Let me think about "semi-consistent read" behavior under REPEATABLE READ with concurrent UPDATEs: A locking UPDATE in REPEATABLE READ uses current reads — it reads the latest committed version and locks it. Two concurrent UPDATEs on the same row serialize via row lock; second waits, then re-reads latest version and re-applies WHERE. Standard and correct.

Hmm, wait. What about INSERT ... occupied_count = 1 with total = countRooms — countRooms is a SELECT COUNT(*) consistent read — fine, 3.

Let me actually look at the surefire report / rerun with more diagnostics. Maybe the failure is: rows.size() == 2 but one row is for date... Actually cleanup from a previous run? @AfterEach deletes rows in range. But if a previous test left rows... cleanup ran after each test. Hmm, but the failing run: `queryAvailability_dailyMinimum_andZeroWhenFull` runs before (test order not guaranteed). If tests run in order: queryAvailability_noInventoryRow (no rows created), dailyMinimum (creates rows, cleans up), release (creates, cleans), concurrent. AfterEach deletes rows for the range. OK.

But wait — is @AfterEach cleanup transactional issue? inventoryMapper.delete autocommits. Fine.

Hypothesis: deadlock. 3 successes + day2 occupied 1: maybe threads 2,3 hit deadlock/lock-wait-timeout on day2 retry, threw a non-BizException? No — then success would be 1, not 3. Unless... hmm success == 3 with day2 == 1: the 3 successful threads all returned from tryOccupy without exception. Each successful thread must have occupied day2 (UPDATE returned 1 or INSERT succeeded). If 3 threads succeeded, day2 occupied must be 3 — unless a later ROLLBACK... no, successful threads committed.

Unless — the three successes happened but two of them their day2 occupies were rolled back due to... no.

OR: the assertion that failed is actually `assertEquals(TOTAL_ROOMS, success.get())`? Line 113... let me count precisely. I'll read the surefire report or count lines. Let me count my written file:

Line numbers roughly: package(1) ... Let me count from the content. Rather than count, check the surefire report which shows the line. It said line 113. Let me view lines 100-120 of the test file.

Actually let me just look: the failure trace shows `at com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells(InventoryServiceTest.java:113)` and "expected: <3> but was: <1>". And earlier `AssertionFailedError` at line 113 via `InventoryServiceTest.java:113` inside forEach? The trace shows `at java.base/java.util.ArrayList.forEach` — hmm, trace shows `at com.example...InventoryServiceTest.tryOccupy_concurrent_neverOversells(InventoryServiceTest.java:113)` then `ArrayList.forEach`... The forEach with lambda is in `release_returnsRoomToSellable` (rows.forEach). Wait no, the trace bottom: `InventoryServiceTest.tryOccupy_concurrent_neverOversells(InventoryServiceTest.java:113)` called from ArrayList.forEach? That doesn't make sense — forEach is caller of lambda, not of the test method. The trace shows:

```
at org.junit...Assertions.assertEquals(Assertions.java:531)
at com.example...InventoryServiceTest.tryOccupy_concurrent_neverOversells(InventoryServiceTest.java:113)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
```

So assertion at line 113 in tryOccupy_concurrent_neverOversells. ArrayList.forEach in trace is JUnit platform internals? Whatever. Let me just count lines to find line 113.

</details>

并发测试失败了（`expected: <3> but was: <1>`）。先定位 113 行并查看数据库实际状态：

#### Tool Call: Read (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_7ukjUjkRz6mg6lCEFrkDz5IT -->
```json
{
  "line_offset": 100,
  "n_lines": 25,
  "path": "hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_7ukjUjkRz6mg6lCEFrkDz5IT -->
100	                    assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
101	                    assertEquals("该房型所选日期已订满", e.getMessage());
102	                    soldOut.incrementAndGet();
103	                } catch (InterruptedException e) {
104	                    Thread.currentThread().interrupt();
105	                }
106	            });
107	        }
108	        assertTrue(ready.await(10, TimeUnit.SECONDS));
109	        start.countDown();
110	        pool.shutdown();
111	        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
112	
113	        assertEquals(TOTAL_ROOMS, success.get());
114	        assertEquals(threads - TOTAL_ROOMS, soldOut.get());
115	
116	        // 每日 occupied_count 恰为 3 且不超过 total_count，失败方无残留占用
117	        List<DailyInventory> rows = selectRows();
118	        assertEquals(2, rows.size());
119	        for (DailyInventory row : rows) {
120	            assertEquals(TOTAL_ROOMS, row.getTotalCount());
121	            assertEquals(TOTAL_ROOMS, row.getOccupiedCount());
122	            assertTrue(row.getOccupiedCount() <= row.getTotalCount());
123	        }
124	        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));

</details>

<details><summary>Thinking</summary>

Line 113 is `assertEquals(TOTAL_ROOMS, success.get())` — only 1 thread succeeded out of 10. So 9 threw... but soldOut assertion never reached. So 8 threads threw something that is NOT BizException (swallowed by executor), or deadlocks.

Likely cause: deadlock or lock wait timeout among concurrent INSERTs of the same unique key, combined with the day-1 conditional UPDATE row locks. MySQL deadlock: thread A holds row lock on day1 row (updated), then INSERTs day2 row. Thread B updated day1? No wait — only one thread can hold day1 row lock at a time; others wait. Hmm.

Scenario: All 10 threads run conditional UPDATE on day1 — row doesn't exist! UPDATE on nonexistent row in REPEATABLE READ takes gap locks? A locking read (UPDATE) that finds no row in REPEATABLE READ sets a next-key/gap lock on the scanned range. The scan is by unique index (room_type_id, stay_date). With no matching row, each tx takes a gap lock (shared-ish for the gap). Gap locks are compatible with each other. Then all 10 INSERT into the gap → insert intention locks → wait for gap lock holders... classic mutual deadlock: all threads hold gap lock on the same gap and all want insert intention lock in that gap → MySQL detects deadlock immediately, rolls back one... Actually with 10 waiters all holding the gap lock, no one can proceed → deadlock detector picks victims. One survivor inserts. But then day2 same thing.

So many threads die with Deadlock (SQLException → CannotAcquireLockException / DeadlockLoserDataAccessException), not BizException → swallowed → success < 3.

Hmm, but this is exactly the flow described in the tech doc §5.1 — "INSERT 撞唯一索引重试一次". In real order creation (T8), the flow is the same. Under real concurrency, MySQL deadlock on concurrent first-booking of same dates is plausible. The tech doc's approach: "UPDATE 影响 0 → 尝试 INSERT → 撞唯一索引 → 回到 UPDATE 重试". Deadlocks would manifest as exceptions causing rollback — in T8, the order creation would fail with a system error rather than SOLD_OUT. For T7's acceptance "成功数恰为房间数", the test expects 3 successes.

How to fix? Options:

1. Order of operations: instead of UPDATE-then-INSERT, do INSERT ... ON DUPLICATE KEY UPDATE? Not in MyBatis whitelist issue — no, it's just SQL, allowed. But ON DUPLICATE KEY UPDATE with conditional occupied<total... e.g.:

```sql
INSERT INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)
VALUES (#{typeId}, #{date}, #{total}, 1)
ON DUPLICATE KEY UPDATE occupied_count = IF(occupied_count < total_count, occupied_count + 1, occupied_count)
```
Then check affected rows: INSERT → 1, successful increment → 2, no-increment (full) → 0. Actually with ON DUPLICATE KEY UPDATE, affected rows: 1 for insert, 2 for update, 0 if update no-op. This handles atomically in one statement! But it also takes the same gap locks + insert intention — same deadlock potential? With ON DUPLICATE KEY UPDATE, concurrent statements on same unique key serialize better? Still gap-lock/insert-intention issues in RR.

2. Reduce isolation for these statements or set tx isolation READ COMMITTED — in READ COMMITTED, gap locks are mostly not taken for non-matching rows; insert intention conflicts handled via unique key wait (no deadlock). The classic fix: concurrent INSERTs of same unique key in RC block until the holder commits, then get duplicate-key error. No gap-lock deadlock.

3. Synchronized approach: create inventory rows in advance? Not in design.

4. Catch deadlock exceptions too and retry once? Retry the whole day occupy on deadlock/duplicate. The spec says retry once on duplicate key. Deadlocks are a known issue; the doc didn't cover it.

Simplest robust approach honoring the design: set the inventory operations to run under READ COMMITTED? Changing tx isolation per-method via @Transactional(isolation = Isolation.READ_COMMITTED) — but if joining a caller's transaction (T8), changing isolation isn't allowed (must not change isolation of existing tx). It would throw when joining existing tx with different isolation. Hmm.

Alternative: make occupyOneDate retry loop handle transient concurrency errors (DuplicateKeyException, CannotAcquireLockException/DeadlockLoserDataAccessException) with a couple of retries. But if the row is genuinely full, the UPDATE returns 0 repeatedly → SOLD_OUT. Retrying deadlock is safe.

Actually, let me reconsider the deadlock. 10 threads: UPDATE day1 (no row) → gap lock each. All try INSERT day1 → all wait on insert intention vs others' gap locks → deadlock detector kills 9 of 10? MySQL deadlock detection rolls back ONE victim at a time; after victim rolls back (releasing its gap lock), others may still be blocked by remaining gap locks... each waiter waits for ALL conflicting gap locks to release. So they wake sequentially as victims roll back; eventually one INSERT succeeds. The victims got Deadlock exception. So ~8-9 die. Only 1 survives day1; then day2 same pattern → 1 success. Matches observed success=1.

With retry on deadlock: deadlocked tx in InnoDB — the whole transaction is rolled back by deadlock detector. Retrying within the same @Transactional method is useless (tx is doomed). The retry must be in a NEW transaction. So retry needs to happen outside the @Transactional boundary... 

Hmm wait, actually when InnoDB detects deadlock it rolls back the victim's entire transaction (in RR, it rolls back the whole tx). Spring marks tx rollback-only. So retry inside the method can't work.

Options to make this work cleanly:

Option A: Use a single atomic upsert statement and set session isolation? No.

Option B: Retry at a higher level — the test calls tryOccupy, catches transient exceptions and retries the whole method (new tx each call). But that pushes responsibility to callers (T8 order service would need same retry logic) — ugly, and failure message wouldn't be "已订满".

Option C: Avoid the gap-lock deadlock by pre-creating rows: on first occupy of a date, do the INSERT in a way that doesn't conflict... 

Option D: Change the UPDATE to not take gap locks — gap locks arise because no matching row. If the row existed, UPDATE takes a record lock and serializes cleanly. The problem is only the first booking of a date. What if INSERT is attempted FIRST (before UPDATE)? Sequence: try INSERT (total=count, occupied=0)? Then UPDATE? No wait: try INSERT with occupied=0 first: 10 threads → gap locks? Plain INSERT doesn't take gap locks; it takes insert intention locks which conflict with gap locks held by other locking reads — but if nobody did a locking read yet... Plain concurrent INSERTs of same unique key: thread 1 inserts (uncommitted), threads 2-10 wait on the unique index record's lock (S lock on the duplicate key record). No deadlock (they don't hold anything thread 1 needs — unless they also hold other locks). When thread 1 commits, 2-10 get duplicate key error → then do conditional UPDATE → serialize on row lock cleanly. When thread 1 rolls back, one waiter gets the insert.

But hold on: in tryOccupy, threads each first UPDATE day1 (taking gap locks when row absent) — that's what creates deadlock. If we flip: first try INSERT (occupied=0? no...). Hmm, but the day-1 row doesn't exist; the natural first step per spec is conditional UPDATE. What if we make the conditional UPDATE avoid gap locks? 

Actually — the cleaner classic solution: single statement upsert:

```sql
INSERT INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)
VALUES (?, ?, ?, 1)
ON DUPLICATE KEY UPDATE occupied_count = occupied_count + 1
WHERE occupied_count < total_count  -- not valid syntax; MySQL doesn't allow WHERE there
```

MySQL 8.0.20+: 
```sql
INSERT ... VALUES (...) AS new ON DUPLICATE KEY UPDATE occupied_count = IF(occupied_count < total_count, occupied_count + 1, occupied_count)
```
Affected rows: 1 inserted; 2 updated; 0 when full (value unchanged). So: affected==0 → SOLD_OUT. This is one atomic statement — InnoDB handles it with the unique key check and row lock. Does it deadlock under concurrency? Concurrent upserts on same unique key: thread 1 INSERTs (holds insert intention + record lock), threads 2-10 detect duplicate → they need X lock on existing record to update → wait for thread 1's commit (S lock on record? for ON DUP KEY UPDATE they set a next-key lock on the duplicate record and wait). This is the well-known INSERT ... ON DUPLICATE KEY UPDATE deadlock scenario too! There are known deadlock issues with concurrent upserts on the same key.

Hmm. But empirically, the common outcome: waiters block until commit, then perform the update. Deadlocks in the upsert case typically occur with multiple unique keys or multiple rows. With a single row and single unique key, it generally serializes without deadlock... Not 100% sure.

Alternative Option E: handle at service level with retry in a fresh transaction: make occupyOneDate's retry catch DataAccessException... no, tx doomed.

Option F: Serialize the "first insert" using a per-type-date lock via GET_LOCK? Adds complexity.

Option G: Change transaction isolation for the whole app connection to READ COMMITTED? Global change — big decision.

Option H: Accept deadlock errors as retriable at the InventoryService level by using REQUIRES_NEW retry wrapper? A self-injected method with Propagation.REQUIRES_NEW that retries the day-occupy a few times in fresh transactions. But tryOccupy must be atomic across days within the caller's tx (T8: occupy days + insert order in one tx). REQUIRES_NEW inside would break atomicity — day occupies would commit independently. No good.

Option I: Retry the whole tryOccupy in the test? No — acceptance says success count exactly 3 through the interface.

Let me reconsider: maybe simply catch deadlock/lock-wait in occupyOneDate and... tx doomed, no.

Actually wait — is the tx really doomed on deadlock? Yes, InnoDB rolls back victim's entire transaction.

Let me test Option D empirically: the single-statement upsert. Quick experiment in MySQL directly with concurrent clients, or just implement and run the test. MySQL 8 in docker (check version). Affected-rows semantics with ON DUPLICATE KEY UPDATE: returns 1 if inserted, 2 if updated, 0 if update caused no change. With MyBatis @Insert returning int = affected rows. But JDBC by default... Connector/J returns affected rows (not found rows) unless useAffectedRows... default is affected rows. For no-op update: MySQL returns 0 affected when the update sets the same values? By default CLIENT_FOUND_ROWS off → 0. 

Deadlock behavior: let me just try. Actually, I recall that concurrent INSERT...ON DUPLICATE KEY UPDATE on the same unique key under REPEATABLE READ is a notorious deadlock source ("replace into"/"ondup" deadlock with next-key locks). Threads doing upsert on same key: T1 inserts record (X lock). T2 upsert sees duplicate → sets S next-key lock on record, waits for T1. T3 same, waits for T1. T1 commits → T2 and T3 proceed: T2 converts... there's a classic deadlock: T1 (after commit gone)... Actually the known deadlock involves 3 sessions where T1 INSERT waits on T2's S lock while T2 waits on T3... With only one row and all doing the same upsert, after T1 commits T2 and T3 both hold S locks on the record (acquired while waiting? they were granted S locks to check duplicate) — then both want to convert S→X to update → deadlock between T2 and T3! Yes! This is the documented classic: two transactions holding S lock on duplicate record, both requesting X → deadlock. One gets killed.

Hmm. So upsert also deadlock-prone.

Option J: Pessimistic per-type lock ordering — acquire a lock on something shared before touching inventory, e.g., `SELECT ... FROM room WHERE room_type_id = ? ... FOR UPDATE`? That serializes ALL occupies for that room type → kills concurrency but correctness-wise the test would pass: 3 successes. Performance is fine for demo scale. But does it respect the design? §5.1 describes atomic conditional updates + unique index retry. Adding a per-type serialization point isn't in the doc... The doc explicitly says "并发正确性由两点保证：occupied_count < total_count 的条件更新 + 唯一索引". Hmm.

Wait — let me reconsider the actual deadlock more carefully. Is my gap-lock theory right? All 10 threads: UPDATE on nonexistent row → gap lock. Then INSERT → wait. MySQL deadlock detector kills one; its gap lock released; but the other 9 still hold gap locks and wait on each other... Eventually detector kills 8 more? Each deadlock detection resolves one cycle... The last one INSERTs successfully. Result: 1 success, 8 deadlock victims (swallowed), matches success=1 and day1 occupied=1. And the message said rows.size()=2 both days occupied 1. Yes consistent.

Now what does real-world code do about this? Common patterns:
1. Retry the whole transaction on deadlock (application level).
2. READ COMMITTED isolation (no gap locks for non-matching unique scans... actually in RC, InnoDB still takes gap locks for foreign keys/duplicate checks but not for regular locking reads on non-matching rows).

For our case: the caller is order creation in T8 — a short tx. Retrying the entire tryOccupy... but again tx-doomed issue means retry must be outside @Transactional. We could restructure: InventoryService.tryOccupy NOT @Transactional itself (documented "整单回滚由调用方事务保证"), and retry logic... no, same problem: deadlock kills the caller's tx too.

OK here's the thing: maybe simplest correct fix consistent with design: change global MySQL transaction isolation to READ COMMITTED via connection URL? That's a global behavioral change needing care.

Alternative simplest: eliminate the gap-lock deadlock by ensuring the row exists before the conditional UPDATE — i.e., do the INSERT first with occupied=0... wait, but semantics: INSERT occupied=1 if this thread wins creation? If INSERT first with occupied_count=1: thread 1 inserts (occupied 1). Others wait on unique key (plain INSERT: waiter requests insert intention lock; conflict with T1's record lock → wait; no gap locks held by waiters since plain INSERT doesn't take gap locks, and they hold no other locks at day1... but they DO hold day-1... hmm wait each thread processes dates sequentially; at day1 they hold nothing else. Day2: they hold day1 row lock (X) from their successful UPDATE, then INSERT day2: waits on T1's uncommitted day2 insert. No cycle: waiters hold day1 lock but T1 doesn't need day1 (already has it). Actually could T1 wait on a day1 lock held by another? No, T1 has day1 done. So wait graph: T2..T10 → T1. No cycle. T1 commits; T2..T10 get DuplicateKey → retry conditional UPDATE → serialize on record lock. Day1: 3 total successes... wait no — day1 row created by T1 with occupied=1, then T2..T10 retry UPDATE: 2 more succeed (occupied 2, 3), 7 fail → SOLD_OUT ✓. The 2 winners proceed to day2: T1 already committed day2 row (occupied 1) → UPDATE → 2, 3 ✓. 

But wait — while T1 hasn't committed, T2..T10 blocked on INSERT day1. T1 proceeds to day2 INSERT (no conflict), commits. Then T2..T10 unblock. Deadlock-free because plain INSERT doesn't take gap locks and waiters hold nothing T1 needs. But in general multi-day: T2 holds day1 X-lock, then INSERT day2 — but day2 row exists (committed) → INSERT fails immediately with dup key (no wait) → retry UPDATE day2 → needs record lock; held by no one → success. Fine.

However, order of spec says UPDATE first, then INSERT on 0 rows. My change: INSERT-first changes behavior when row exists: INSERT fails immediately (fast dup key check on existing committed row — no wait) → then do conditional UPDATE. That's just a different implementation of the same seam. But is it correct under concurrency where row exists but another tx holds X lock on it (mid-update)? INSERT dup-key check against committed data: duplicate found in committed index → but the record is locked by another tx → InnoDB sets S lock wait on that record... waits until that tx commits → then returns dup error → then UPDATE. OK still fine (possible S→X upgrade deadlock? Waiter requests S lock (shared) for dup check when record is X-locked by an active updater. Multiple waiters hold nothing, just wait for S. When updater commits, all get S, read committed value, return dup error, release S, then UPDATE requests X — serialized. No cycle.)

Hmm wait, one more subtle case: INSERT with occupied=1 — the creator wins a slot atomically. But if the creator's tx later rolls back, row disappears, fine.

But there's an issue: INSERT-first on existing row always throws DuplicateKeyException which we catch for EVERY occupy of an existing date — exception overhead per call. Acceptable for demo scale? It's a bit ugly: using exceptions for control flow on the hot path.

Alternative cleaner: keep UPDATE-first (matches spec), but avoid gap locks by scoping: the UPDATE's gap lock only occurs when row absent. We could do: INSERT IGNORE first (creates row with occupied=0 if absent, no-op if exists), then conditional UPDATE. INSERT IGNORE: plain insert, no gap locks, waiters block on uncommitted same-key insert then proceed with 0 affected after commit. Then conditional UPDATE serializes on record lock. Deadlock-free by same argument. And semantics stay exactly per spec: conditional UPDATE is the single source of truth for occupy; INSERT just ensures row existence with occupied=0.

INSERT IGNORE + conditional UPDATE:

```java
private void occupyOneDate(typeId, date) {
    if (occupyOne(typeId, date) == 1) return;  // fast path: row exists
    insertRowIgnoringDuplicate(typeId, date);   // ensure row exists (occupied 0)
    if (occupyOne(typeId, date) != 1) throw SOLD_OUT;
}
```

Concurrency trace: 10 threads, row absent. All UPDATE → 0 rows (gap locks taken!). Then all INSERT IGNORE → T1 inserts, T2..T10 wait on insert intention vs gap locks of others → SAME DEADLOCK. The gap locks from the UPDATE are the problem! Even though INSERT IGNORE doesn't take gap locks, the waiters' own earlier UPDATE took gap locks, and T1's INSERT needs insert intention lock conflicting with those gap locks → T1 waits on T2..T10's gap locks, T2..T10 wait on T1's record lock → cycle → deadlock.

So to be deadlock-free, must avoid the UPDATE-on-absent-row gap lock entirely → INSERT-first:

```java
private void occupyOneDate(typeId, date) {
    try {
        insert row (occupied=1, total=countRooms);  // fast fail if row exists
        return;
    } catch (DuplicateKeyException e) { }
    if (occupyOne(typeId, date) != 1) throw SOLD_OUT;
}
```

Trace: 10 threads INSERT day1 concurrently. Plain INSERT: T1 proceeds (insert intention granted, no conflicting gap locks since nobody did locking reads). T2..T10: see uncommitted duplicate → wait for T1 (S lock request on the record). Hold nothing. No cycle. T1 commits → T2..T10 get dup error → conditional UPDATE day1 → serialize cleanly → 2 succeed, 7 get 0 → SOLD_OUT. Winners proceed day2 similarly. Result: exactly 3 successes. ✓

Row exists (common case): INSERT → immediate DuplicateKeyException → UPDATE → +1. Every occupy of an existing date pays an exception. Hmm. Alternatively check existence with SELECT first? That reintroduces race but harmless: SELECT (consistent read, no locks) → if exists, UPDATE; else INSERT path. 

```java
private void occupyOneDate(typeId, date) {
    if (inventoryMapper.exists(typeId, date)) {   // plain SELECT, no lock
        if (occupyOne == 1) return;
        else throw SOLD_OUT;   // row exists and full → 0... 
```

wait, if row exists and UPDATE returns 0 → full → SOLD_OUT directly (no need to retry insert). If row doesn't exist (by SELECT), INSERT; on dup (race), UPDATE once; if 0 → SOLD_OUT. This matches spec structure (UPDATE first for existing rows, INSERT for missing, retry once on race) and avoids gap-lock deadlock, because when row is absent we go straight to INSERT without the locking UPDATE. The SELECT is a non-locking consistent read → no gap locks. 

Edge: SELECT says absent, INSERT succeeds → occupied=1 ✓. SELECT says absent but another tx created it uncommitted → INSERT waits → dup error after commit → UPDATE → maybe success or SOLD_OUT ✓. SELECT says present (committed) → UPDATE; concurrent creator rolled back? Row was committed, so it stays. ✓

SELECT present but UPDATE 0 (full) → SOLD_OUT without retry. Correct — row exists and is full; retry pointless.

This is clean. One question: does the existence SELECT add an extra query per date? Yes but fine.

Implement:
- InventoryMapper: add @Select exists or use BaseMapper selectCount. Use selectCount with QueryWrapper in service (no new mapper method needed).

Also: does InnoDB plain INSERT take gap locks? INSERT sets an insert intention lock on the gap, which only conflicts with other tx's gap locks from locking reads. We eliminated locking reads on absent rows (SELECT is consistent read, no locks; UPDATE only runs when SELECT found the row — row exists so record lock, no gap lock needed... well UPDATE by unique key on existing row takes record lock only in RR for unique index exact match). ✓

Also the earlier failing behavior: keep the spec's "INSERT 撞唯一索引重试一次" — yes retained.

Update the service:

```java
private void occupyOneDate(Long typeId, LocalDate date) {
    boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
            .eq("room_type_id", typeId).eq("stay_date", date)) > 0;
    if (exists) {
        if (inventoryMapper.occupyOne(typeId, date) != 1) {
            throw new BizException(ErrorCode.SOLD_OUT);
        }
        return;
    }
    // 首次预订该日：建行（occupied=1），撞唯一索引说明并发已建行，回到原子条件更新重试一次
    DailyInventory row = new DailyInventory();
    ...
    try { inventoryMapper.insert(row); return; }
    catch (DuplicateKeyException ignored) { }
    if (inventoryMapper.occupyOne(typeId, date) != 1) throw new BizException(ErrorCode.SOLD_OUT);
}
```

Wait, subtle: exists==true but UPDATE 0 → full → SOLD_OUT. But is there a case where exists SELECT (consistent read at tx snapshot) sees committed row while... yes it's committed data, fine. But snapshot reads in RR within tx: first SELECT establishes snapshot; if row created after snapshot by another committed tx, SELECT says absent → INSERT → immediate dup error (index sees latest) → UPDATE → fine. ✓

Also note: test `queryAvailability_dailyMinimum_andZeroWhenFull` expects tryOccupy on full → BizException ✓.

Now also think: do deadlocks still possible in day2 phase? Thread holds day1 record X lock, INSERTs day2 (waits for uncommitted creator). Creator of day2: got there after committing day1? No — creator's tx is uncommitted while doing day2! Wait: T1 (winner day1 via INSERT, holds day1 row uncommitted) proceeds day2 INSERT (uncommitted). T2 (got day1 via UPDATE after T1 committed? NO — T1 hasn't committed! T2's INSERT day1 was blocked waiting on T1's uncommitted insert. T1 continues day2, commits ALL, then T2 unblocks. So T2..T10 all wait for T1's full commit. Then they UPDATE day1 (record lock, free now) — 2 succeed. They proceed day2: row exists (committed) → SELECT sees it → UPDATE → serialize → 2 succeed? Wait day2 has occupied=1 from T1, total 3 → T2,T3 both succeed → day2 occupied 3 ✓. 7 others SOLD_OUT at day1, roll back nothing. Total success = 3 ✓.

Great — no deadlock because at any time waiters hold no locks that the holder-of-record needs... T2 holds nothing while INSERT-waiting on day1 (its first action). After T1 commits, T2 does UPDATE day1 (gets record lock), then INSERT day2 → immediate dup (committed) → UPDATE day2. Fine.

Multi-day with different interleavings: any thread's lock holdings: record locks on days 1..k while waiting on INSERT for day k+1 held by uncommitted T_j. T_j holds records for its days and is actively working; could T_j be waiting on something T2 holds? T_j's day k+1 INSERT waits only on uncommitted same-key inserts. Suppose T_a created day2 row uncommitted and is now working day3, while T_b (holding day1 lock) INSERTs day2 waiting on T_a. Could T_a wait on T_b? T_a would need day1 — T_a already passed day1 (holds its own lock... wait if T_a passed day1 via UPDATE, it holds day1 record lock; T_b's day1 UPDATE would have blocked until T_a commits. So T_b can't hold day1 while T_a active and waiting... 

Hmm, is there a possible cycle: T_a holds day1 (via UPDATE after T_x committed), T_a INSERT day2 waiting on T_c's uncommitted day2 insert; T_c holds day1?? T_c holds day1 record lock only if T_c occupied day1 — but day1 total 3 slots: T_x, T_a, T_c could each hold day1 slots simultaneously! Yes — up to 3 concurrent txs can hold day1 slots (occupied=3 with 3 uncommitted txs!). Then T_c INSERTs day2 (creates row, uncommitted), T_a INSERTs day2 → waits on T_c. T_b (4th) waits day1 UPDATE on record lock... held by T_x/T_a/T_c (multiple holders of... no, record lock X on day1 row — only ONE tx can hold X lock on day1 row at a time! UPDATEs serialize on the record lock. So day1 slots are acquired sequentially but HELD concurrently by up to 3 txs.

Cycle possibility: T_c holds day1, created day2 row uncommitted, now INSERTs day3... waits on whoever created day3 uncommitted, say T_a. T_a holds day1? No—T_a needs day1 lock, which it got before day2... T_a holds day1, waits day2 on T_c. T_c holds day1 (same row?!) — wait, day1 is ONE row (type 1, date1). Only one tx holds its X record lock at a time. T_a occupied day1 means T_a's UPDATE got the record lock, incremented, and KEEPS the lock until commit. Then T_c's UPDATE day1 BLOCKS on T_a's lock! So T_c can't proceed past day1 while T_a is active. So holders of different day slots: at most one tx passes day1 at a time... no wait — T_a holds day1 X lock until commit. T_c blocks on day1 UPDATE. So only ONE active tx can be past day1 at any moment?! 

Hmm no — the X record lock on day1 row is held by T_a until commit. Any other tx's UPDATE day1 blocks. So only T_a can be in days 2..n at a time?? Then what about T_x (the INSERT creator of day1): T_x inserted day1 row (X lock on its insert) and moved to day2 while uncommitted. Others blocked. So indeed only one tx holds day1 at a time. So no cross-day deadlock possible: the active frontier tx holds day1; everyone else waits on day1. Wait — but T_a got day1 via UPDATE only after T_x committed... and T_x only commits after finishing all days. Hmm, so actually only one tx occupies days at a time for the same type+date prefix... That's serialization but correct.

BUT deadlock scenario: T_a holds day1 (X record lock via UPDATE), INSERTs day2 — day2 row created uncommitted by T_x? T_x is the day1 INSERT creator who committed already (since T_a's UPDATE succeeded). T_x committed → day2 row committed → T_a's INSERT day2 → immediate dup error → UPDATE. Fine.

What if two different date ranges: thread A occupies dates [d1, d2], thread B occupies [d2, d3]? A holds d1, inserts d2 row (uncommitted), moves on... A commits. B: INSERT d2? B's SELECT sees no d2 row (before A commits) → INSERT → blocks on A's uncommitted d2 insert → A commits → dup error → UPDATE d2 → +1 ✓. No cycle: B holds nothing before d2 (d2 is its first date).

Range order differences: all threads process dates in ascending order (fixed lock ordering per spec) — cycles would require opposite ordering, which can't happen. And INSERT-waits only target uncommitted inserters of the same key, who are always "ahead" in the ordering. Ascending order + no locking reads on absent rows ⇒ deadlock-free. 

One more: what about the SOLD_OUT threads that did UPDATE day1 = 0 (row full): UPDATE on existing row needs record lock — blocks until current holder commits, then re-evaluates → 0 rows. No gap locks, no deadlock. ✓

And release(): UPDATE existing rows only, ascending. Fine.

Also `queryAvailability_dailyMinimum_andZeroWhenFull` test: tryOccupy(CHECKIN, CHECKIN+1) → row absent → INSERT occupied=1 ✓.

Now also reconsider: `exists` SELECT — use selectCount. In the exists branch, if UPDATE returns 0, could it be because row is full — SOLD_OUT ✓.

Wait, actually one more consideration on the `exists` check and RR snapshot: within one tx, first consistent read sets snapshot. Multi-day occupy: day1 INSERT is a write (current read). Day2 SELECT (consistent) — uses snapshot from... the first consistent read in the tx. If SELECT day2 happens before any consistent read, snapshot is "now". INSERTs don't set snapshot. OK fine — the SELECT is just an optimization/hint; correctness comes from INSERT/UPDATE. Even if SELECT answer is stale: 
- stale "exists" (row created after snapshot, committed): we do UPDATE → current read, works on latest → +1 or full. ✓ (UPDATE uses current read regardless of snapshot)
- stale "absent" (row deleted? rows never deleted in prod): INSERT → dup error → UPDATE. ✓

All good. Update the service code.

Also the exception-path: when row exists, we avoid exceptions entirely (SELECT then UPDATE). Nice.

Let me now rewrite InventoryService.occupyOneDate. Also update the design comment.

Then rerun tests. Also verify no deadlock exceptions. Let's implement.

</details>

找到原因了：line 113 是成功数断言——10 线程只有 1 个成功，其余 8 个是被 MySQL 死锁检测杀掉的（异常被线程池吞掉）。

根因：按当前实现，10 个线程先对**不存在的行**执行条件 UPDATE（REPEATABLE READ 下对不存在的唯一索引记录会加间隙锁），随后全部 INSERT 同一间隙 → 互相等待间隙锁，形成经典死锁环，InnoDB 逐个回滚受害者，只剩 1 个存活。

修复：先做一次**无锁的一致性读**判断行是否存在——存在则直接走原子条件更新（行存在时是记录锁，无间隙锁）；不存在才 INSERT（普通 INSERT 不加间隙锁，并发时只等待建行者提交后撞唯一索引重试）。这样消除了间隙锁，从机制上避免该死锁，且仍符合技术方案 §5.1"条件更新 + INSERT 撞唯一索引重试一次"的语义：

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_6bPbavCGcD1W4cwHgvSdHdau -->
```json
{
  "new_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 无锁一致性读判断行是否存在：行存在时直接走原子条件更新（记录锁），\n        // 避免对不存在的行做加锁 UPDATE 产生间隙锁导致并发 INSERT 死锁\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (exists) {\n            if (inventoryMapper.occupyOne(typeId, date) != 1) {\n                throw new BizException(ErrorCode.SOLD_OUT);\n            }\n            return;\n        }\n        // 首次预订该日：建行（occupied=1）；撞唯一索引说明并发已建行，回到原子条件更新重试一次\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 并发已建行，重试条件更新\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "old_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        if (inventoryMapper.occupyOne(typeId, date) == 1) {\n            return;\n        }\n        // 影响 0 行：该行可能不存在（首次预订该日），尝试建行；撞唯一索引说明并发已建行\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 回到原子条件更新重试一次\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_6bPbavCGcD1W4cwHgvSdHdau -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

#### Tool Call: Bash (`cd hotel-server && mvn -q test 2>&1 | tail -15`)
<!-- call_id: tool_NyOYSaTkkIr5v4XNoQSJqEJT -->
```json
{
  "command": "cd hotel-server && mvn -q test 2>&1 | tail -15",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn -q test 2>&1 | tail -15`)</summary>

<!-- call_id: tool_NyOYSaTkkIr5v4XNoQSJqEJT -->

[ERROR] Failures: 
[ERROR]   InventoryServiceTest.tryOccupy_concurrent_neverOversells:113 expected: <3> but was: <2>
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.2:test (default-test) on project hotel-server: There are test failures.
[ERROR] 
[ERROR] See /Users/donglongjun/IdeaProjects/hotel-reservation-system/hotel-server/target/surefire-reports for the individual test results.
[ERROR] See dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException


</details>

<details><summary>Thinking</summary>

Progress: success went 1 → 2, but still not 3. One thread still dying with a non-BizException. Need to see what exception. Let me capture the exceptions in the test — record them into a concurrent collection and print on failure, or log. Quick approach: collect unexpected exceptions and fail with details. Let me modify test to store errors, then rerun to see the actual exception.

</details>

成功数从 1 提升到 2，但仍有线程因非业务异常死亡。先让测试把未知异常收集起来看看具体是什么：

#### Tool Call: Edit (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_m1cW1ijbkQUw23Uv0rkh24qt -->
```json
{
  "new_string": "        AtomicInteger success = new AtomicInteger();\n        AtomicInteger soldOut = new AtomicInteger();\n        List<Throwable> unexpected = new java.util.concurrent.CopyOnWriteArrayList<>();\n        for (int i = 0; i < threads; i++) {\n            pool.submit(() -> {\n                ready.countDown();\n                try {\n                    start.await();\n                    inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n                    success.incrementAndGet();\n                } catch (BizException e) {\n                    assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());\n                    assertEquals(\"该房型所选日期已订满\", e.getMessage());\n                    soldOut.incrementAndGet();\n                } catch (InterruptedException e) {\n                    Thread.currentThread().interrupt();\n                } catch (Throwable e) {\n                    unexpected.add(e);\n                }\n            });\n        }",
  "old_string": "        AtomicInteger success = new AtomicInteger();\n        AtomicInteger soldOut = new AtomicInteger();\n        for (int i = 0; i < threads; i++) {\n            pool.submit(() -> {\n                ready.countDown();\n                try {\n                    start.await();\n                    inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);\n                    success.incrementAndGet();\n                } catch (BizException e) {\n                    assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());\n                    assertEquals(\"该房型所选日期已订满\", e.getMessage());\n                    soldOut.incrementAndGet();\n                } catch (InterruptedException e) {\n                    Thread.currentThread().interrupt();\n                }\n            });\n        }",
  "path": "hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_m1cW1ijbkQUw23Uv0rkh24qt -->
Replaced 1 occurrence in hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java

</details>

#### Tool Call: Edit (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_1MmB3JW3UYlsajXq5k6bCQHU -->
```json
{
  "new_string": "        assertTrue(unexpected.isEmpty(), () -> \"未知异常: \" + unexpected);\n        assertEquals(TOTAL_ROOMS, success.get());",
  "old_string": "        assertEquals(TOTAL_ROOMS, success.get());",
  "path": "hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_1MmB3JW3UYlsajXq5k6bCQHU -->
Replaced 1 occurrence in hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java

</details>

#### Tool Call: Bash (`cd hotel-server && mvn -q test -Dtest=InventoryServiceTest#t…`)
<!-- call_id: tool_TMkUDlJ0CosbmELShA7EssFd -->
```json
{
  "command": "cd hotel-server && mvn -q test -Dtest=InventoryServiceTest#tryOccupy_concurrent_neverOversells 2>&1 | grep -A5 \"未知异常\\|FAILURE!\" | head -20",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn -q test -Dtest=InventoryServiceTest#t…`)</summary>

<!-- call_id: tool_TMkUDlJ0CosbmELShA7EssFd -->
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.303 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.395 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 
未知异常: [org.springframework.dao.DeadlockLoserDataAccessException: 
### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
### The error may exist in com/example/hotel/inventory/mapper/InventoryMapper.java (best guess)
### The error may involve com.example.hotel.inventory.mapper.InventoryMapper.occupyOne-Inline
### The error occurred while setting parameters
### SQL: UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count
--
[ERROR]   InventoryServiceTest.tryOccupy_concurrent_neverOversells:116 未知异常: [org.springframework.dao.DeadlockLoserDataAccessException: 
### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
### The error may exist in com/example/hotel/inventory/mapper/InventoryMapper.java (best guess)
### The error may involve com.example.hotel.inventory.mapper.InventoryMapper.occupyOne-Inline
### The error occurred while setting parameters
### SQL: UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count


</details>

<details><summary>Thinking</summary>

Still a deadlock, now on the UPDATE itself (occupyOne), on day1 presumably. Why?

Scenario: row exists (created by T1's INSERT, but uncommitted?). Hmm. Let's think: T1 INSERTs day1 (uncommitted). T2..T10: their SELECT (consistent read) — does NOT see T1's uncommitted row → all go INSERT → wait for T1. T1 INSERTs day2, commits. T2..T10 get DuplicateKeyException → UPDATE day1 → serialize → 2 succeed (occupied 2, 3), 7 get 0 → SOLD_OUT. The 2 winners go to day2: SELECT sees committed day2 row (T1 created) → UPDATE day2 → 2,3 ✓.

Where's the deadlock on UPDATE? Hmm. Deadlock on the UPDATE statement itself. Consider: T2 got day1 via UPDATE (holds day1 X record lock), proceeds to day2 UPDATE (gets day2 lock). T3 also got day1?? — day1 row: occupied 1→2 by T2, T2 holds X lock until commit. T3's UPDATE day1 must WAIT for T2's lock. So T3 can't hold day1 while T2 active. So only one tx beyond day1 at a time... unless the lock taken by UPDATE... hmm wait — actually no. The UPDATE's record lock: T2 updates day1 → X lock on day1 record held to commit. T3's UPDATE day1 blocks on that X lock. So T3 waits; only T2 proceeds to day2. So how can deadlock occur on UPDATE?

Unless the deadlock is between an UPDATE waiting for record lock and... deadlock requires a cycle. T2 holds day1, wants day2. Who holds day2? Only T1's insert — committed. Hmm.

Wait, maybe the deadlock involves the SELECT? No, consistent reads take no locks.

Alternative: the deadlock happens on day1 UPDATE where multiple waiters queue: T2 waiting (X lock request) on day1 held by T3? T3 holds day1 X lock; T3 wants... day2. Who holds day2 lock that T3 needs? Another tx T4 that created day2 row via INSERT but uncommitted — T4 must have gotten past day1... but day1 lock is held by T3! Contradiction — unless T4 created the day2 row via INSERT while NOT holding day1?? A tx only reaches day2 after day1 success. Hmm.

Wait — actually maybe the failing UPDATE is for a date where row was created but creator hasn't committed. SELECT (consistent read, snapshot established earlier) says "not exists" → INSERT → dup wait... no, that path throws DuplicateKeyException not deadlock on UPDATE.

Let me reconsider: could the deadlock be between two UPDATEs on day1 with... one row only. Two txs can't hold the same row's X lock.

Hmm — REPEATABLE READ next-key locks! UPDATE on existing record via unique index: takes next-key lock (record + gap before it) in RR for... For a unique index exact match, InnoDB downgrades to record lock. For the UPDATE's WHERE occupied_count < total_count — the search uses the unique index (room_type_id, stay_date) → exact match → record lock. But the UPDATE also must check occupied_count — no additional index. So record lock only. Hmm.

But wait: what about the SELECT COUNT(*) for `exists` — consistent read, no locks. And countRooms SELECT COUNT(*) FROM room WHERE room_type_id — consistent read, no locks.

So where's the cycle? Let me think about INSERT-wait scenario again: T2..T10 blocked on INSERT waiting for T1's uncommitted insert. T1 does INSERT day2 — but wait, do T2..T10's pending INSERTs on day1 hold insert intention locks in the gap on day1's index? And T1's day2 insert is a different index record. No conflict.

Hmm, what about this: after T1 commits, T2..T10 wake with dup errors. Then each issues UPDATE day1. They serialize: say T2 gets the lock (occupied 1→2), T3 waits... T2 proceeds day2, commits. T3 gets lock (2→3). T4 gets lock: occupied=3, condition fails → 0 rows → SOLD_OUT. T5..T10 same. Then T3 proceeds day2: UPDATE → 2→3? Wait T2 already did day2: 1→2, T3: 2→3. ✓ Total success = T1, T2, T3 = 3. 

But observed 2 successes + 1 deadlock on UPDATE. So one of T2/T3 got deadlocked on some UPDATE. Which UPDATE could deadlock? 

AH WAIT. I see it — the `releaseOne`? No, not used here. Hmm.

Let me reconsider: maybe the deadlock is INSERT vs UPDATE: T_a holds day1 X lock (via UPDATE), proceeds to INSERT day2?? No — day2 row exists (SELECT says exists → UPDATE). Hmm, but what if T_a's SELECT for day2 says "not exists" (snapshot stale? first consistent read in its tx established the snapshot BEFORE T1 committed day2 row). RR snapshot: T_a's first consistent read (SELECT exists day1) happens early — before T1 commits. That snapshot has NO day1 row and NO day2 row. So T_a sees day1 "not exists" → INSERT day1 → waits for T1 → dup error → UPDATE day1 → gets lock → +1. Then day2: SELECT exists? Uses SAME snapshot → says "not exists" (day2 row created by T1 after snapshot) → INSERT day2 → day2 row committed by T1 already → immediate DuplicateKeyException → catch → UPDATE day2 → +1. Fine, no deadlock there.

Hmm OK so where's the deadlock?? Let me get the full deadlock info from MySQL: SHOW ENGINE INNODB STATUS. But by now it's gone. Let me instead reproduce with logging, or check surefire output for more. Actually, better: run the test again while capturing `SHOW ENGINE INNODB STATUS` right after failure? The deadlock info persists in innodb status (latest deadlock). The test just ran — but other deadlocks since? Let me check now.

</details>

仍有 1 个线程死锁，这次发生在 UPDATE 上。查看 InnoDB 最近一次的死锁详情：

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 -e "SHOW ENGI…`)
<!-- call_id: tool_RArjM87FwzyPihuIjWLtg6Xl -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 -e \"SHOW ENGINE INNODB STATUS\\G\" 2>/dev/null | sed -n '/LATEST DETECTED DEADLOCK/,/^------------$/p' | head -60"
}
```

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 -e "SHOW ENGI…`)</summary>

<!-- call_id: tool_RArjM87FwzyPihuIjWLtg6Xl -->
LATEST DETECTED DEADLOCK
------------------------
2026-10-04 17:20:58 281471973191424
*** (1) TRANSACTION:
TRANSACTION 1961, ACTIVE 0 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 3 row lock(s)
MySQL thread id 58, OS thread handle 281472430034688, query id 792 192.168.65.1 root updating
UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = 1 AND stay_date = '2099-01-10' AND occupied_count < total_count

*** (1) HOLDS THE LOCK(S):
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 1961 lock mode S
Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001; asc         ;;
 1: len 3; hex 90662a; asc  f*;;
 2: len 8; hex 8000000000000022; asc        ";;


*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 1961 lock_mode X locks rec but not gap waiting
Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001; asc         ;;
 1: len 3; hex 90662a; asc  f*;;
 2: len 8; hex 8000000000000022; asc        ";;


*** (2) TRANSACTION:
TRANSACTION 1963, ACTIVE 0 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 3 row lock(s)
MySQL thread id 56, OS thread handle 281472431152896, query id 795 192.168.65.1 root updating
UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = 1 AND stay_date = '2099-01-10' AND occupied_count < total_count

*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 1963 lock mode S
Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001; asc         ;;
 1: len 3; hex 90662a; asc  f*;;
 2: len 8; hex 8000000000000022; asc        ";;


*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 1963 lock_mode X locks rec but not gap waiting
Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001; asc         ;;
 1: len 3; hex 90662a; asc  f*;;
 2: len 8; hex 8000000000000022; asc        ";;

*** WE ROLL BACK TRANSACTION (2)
------------


</details>

<details><summary>Thinking</summary>

Now it's clear: two transactions each HOLD an S (shared) record lock on the day1 row, and both request X lock → deadlock. Where does the S lock come from? The INSERT's duplicate-key check! When INSERT encounters a duplicate key (against a committed row), InnoDB sets an S lock on the existing record to verify the duplicate. So: T2 INSERTs day1 (row exists, committed) → gets dup error AND HOLDS an S lock on the record (locks are held until tx end!). Then T2 does UPDATE day1 → wants X. T3 same: holds S (from its failed INSERT), wants X. T2's X waits for T3's S; T3's X waits for T2's S → deadlock. Classic S→X upgrade deadlock.

Key insight: the S locks from duplicate-key checks are held until transaction end. My catch of DuplicateKeyException doesn't release them.

How to avoid? The failed-INSERT S lock is inherent to the design "INSERT then retry UPDATE". In the spec's original design (UPDATE first, INSERT only when row missing), when row exists there is no INSERT → no S lock → no upgrade deadlock. When row is missing → gap locks → the earlier deadlock. Ugh: both orderings have deadlock modes under RR.

Solutions:

1. Run inventory ops under READ COMMITTED. In RC: no gap locks for the UPDATE-on-missing-row (so original spec flow: UPDATE first → no gap locks taken in RC? Actually in RC, locking reads don't take gap locks (except dup key checks / FK). And the INSERT duplicate check S lock in RC: still sets S locks? In READ COMMITTED, for duplicate key checks InnoDB still takes S locks... Yes, dup check S locks happen in RC too. But with the spec's original flow (UPDATE first), when row exists, we never INSERT → no S locks → clean serialization on record X locks. When row missing: UPDATE takes no gap locks in RC → then concurrent INSERTs: T1 inserts, others wait on T1's record... then get dup error (with brief S lock acquisition after T1 commits?) — In RC, after the inserter commits, waiting INSERTs acquire S lock on the record to check duplicate → error. They hold S locks... then retry UPDATE → same S→X upgrade deadlock possible among the retriers!

Hmm. Does the S lock from a duplicate check get released? In RC isolation, InnoDB releases locks on records not matching... no — "locks set by INSERT duplicate-key checks are held to end of transaction"? Actually there was a bug/behavior: in RC, S locks from dup checks are released after statement? I'm not sure.

2. Avoid the retry-UPDATE-after-failed-INSERT in the same transaction where others also do it... 

3. Use SELECT ... FOR UPDATE (locking read) to serialize instead: flow per date:
   - Locking read: SELECT * FROM daily_inventory WHERE type AND date FOR UPDATE.
     - If row exists: we now hold X record lock; check occupied < total; if yes UPDATE +1 (or even occupied_count = occupied+1 unconditional since we hold the lock and checked), else SOLD_OUT.
     - If no row: INSERT (we hold next-key lock on the gap from FOR UPDATE in RR... FOR UPDATE on missing row takes gap/next-key lock → concurrent FOR UPDATEs on the same missing row: compatible gap locks again! Then INSERTs → insert intention vs others' gap locks → deadlock again!! Same problem.

   Unless RC isolation: FOR UPDATE on missing row takes NO gap locks in RC. Then: all 10 FOR UPDATE (no row, no locks) → all INSERT → T1 wins, others wait for T1's record... after commit, dup error → catch → FOR UPDATE again (now row exists) → serialize → check occupied<total → update or SOLD_OUT. The S-lock-from-dup-check issue: does the failed INSERT leave an S lock in RC? Hmm, in READ COMMITTED, InnoDB's "semi-consistent read" and lock release: For INSERT duplicate check, the S lock is still acquired. I believe it's held until end of transaction in both isolations. Then the retried FOR UPDATE wants X — deadlock among retriers again? T2 and T3 both hold S (from failed INSERT), both FOR UPDATE want X → deadlock. SAME issue.

   To fully avoid: after failed INSERT, don't retry within same tx... but we must occupy the slot!

4. The cleanest bulletproof approach: serialize per (type,date) row creation via a different mechanism — e.g., INSERT with `INSERT ... ON DUPLICATE KEY UPDATE` as a SINGLE statement: the statement itself handles both. Concurrent upserts: as discussed, waiters set S locks for dup check then want X → the classic IODKU deadlock. Same.

5. Set transaction isolation to READ COMMITTED for the whole app. Does that eliminate ALL these deadlocks? The S-lock-from-dup-check held-to-commit problem persists in RC (I believe InnoDB holds them; there's `innodb_locks_unsafe_for_binlog` legacy...). Hmm, actually let me think again: in READ COMMITTED, "For locking reads (SELECT with FOR UPDATE or FOR SHARE), UPDATE, and DELETE statements, InnoDB locks only index records, not the gaps before them". For INSERT duplicate checks, S locks on the duplicate record are still set. Are they released at statement end in RC? Documentation says: in RC, locks that are not needed are released after the statement evaluates them ("InnoDB releases locks on rows that do not match the WHERE"). But the dup-check S lock... not sure.

6. Sidestep entirely: use MySQL named locks GET_LOCK('inv:1:2026-01-01') as a mutex around the occupy of that date. Serializes per date, completely avoids row-lock deadlocks. But adds a mechanism not in the tech doc. GET_LOCK is connection/session-level; with connection pool + @Transactional it's the same connection within the tx — workable: GET_LOCK at start of occupyOneDate, RELEASE_LOCK... if we don't release explicitly, locks held until released or connection closed — we must release after each. With HikariCP connection reuse, must ensure release. This is extra complexity.

7. Reorder to single-threaded-friendly: serialize ALL tryOccupy calls for the same room_type using a Java-level lock? Not viable across multiple app instances, but demo is single-instance! However the tech doc explicitly wants DB-level guarantees (atomic conditional update). Java lock + atomic update is belt and braces... but the seam's DB guarantees should stand alone for the interview story.

8. Actually — reconsider: does the duplicate-key S lock get held in RR? YES (this deadlock report shows it: trx HOLDS S lock, and the only prior statement touching that row was the failed INSERT... wait, was it? trx 1961 "ACTIVE 0 sec starting index read", holds S lock on day1 record, waiting X. Its statements: SELECT exists (no locks), INSERT (dup error, S lock acquired), UPDATE (waiting X). Yes — the S came from the failed INSERT.

So the failed-INSERT S lock + held-to-commit + retry UPDATE = S→X upgrade deadlock among concurrent retriers. That's what we see.

Fix options that keep everything in RR:
- Never let two txs do failed-INSERT-then-UPDATE concurrently on the same row: i.e., after catching DuplicateKeyException, we know the row exists committed... The S lock is already held — damage done. Any subsequent X request can deadlock with another S holder. Unless only ONE tx at a time can be in that state — can't guarantee.

- Alternative: after DuplicateKeyException, don't UPDATE with condition; instead SELECT ... FOR UPDATE first? Also wants X — same conflict with other S holders... wait FOR UPDATE requests X lock too → same deadlock (T2 holds S wants X, T3 holds S wants X → deadlock). Any X acquisition path deadlocks.

So in RR, the retry path is fundamentally deadlock-prone. UNLESS the failed INSERT doesn't happen concurrently — which happens only on first-booking races.

Realistic assessment: This deadlock occurs only when multiple transactions concurrently create-then-occupy the same date row (or their INSERTs collide). With retry-at-transaction-level, a deadlock victim could retry the whole operation in a NEW transaction and succeed. MySQL itself says "Deadlock found ... try restarting transaction". Retrying deadlocks at the application level is a standard, legitimate pattern!

Design: InventoryService.tryOccupy — deadlock victim's tx is fully rolled back by InnoDB (in RR, InnoDB rolls back the ENTIRE transaction on deadlock — actually InnoDB rolls back only the... on deadlock detection it rolls back the victim transaction completely). So in-method retry impossible; retry must wrap the transactional call. Structure:

- `InventoryService.tryOccupy(typeId, checkin, checkout)` — public, non-transactional, contains retry loop (e.g., up to 3 attempts): calls `doTryOccupy` (self-injected @Transactional method). Catches DeadlockLoserDataAccessException / CannotAcquireLockException → retry. BizException SOLD_OUT → no retry.

But wait — in T8, order creation calls tryOccupy within its own transaction (occupy + insert order atomically). If tryOccupy internally manages its own transactions via REQUIRES_NEW/self-tx, the atomicity with order insert breaks! Hmm.

Unless: the retry wrapper uses REQUIRED propagation: joins caller's tx if present; the retry loop only retries when... if joining an existing tx, a deadlock rolls back the caller's tx too — can't retry internally; the exception propagates to the caller. So: retry only works when tryOccupy is the tx owner. In T8, who owns the tx? Order creation: validate → occupy days → insert order. If a deadlock occurs inside, the whole order tx is victim-rolled-back; the order service would need to retry the whole creation. Realistically, order creation retry on deadlock is acceptable... but adds complexity to T8.

Alternative simpler stance: accept that deadlocks can occur rarely, map them to SOLD_OUT? No — wrong semantics (rooms are available but we'd report sold out). Map to "系统繁忙请重试"? For the demo, the concurrency test TC-G01 requires exactly 3 successes — a deadlock victim mapped to a retriable error would fail the test unless retried.

Practical approach used by many: retry loop INSIDE the service around a self-managed transaction, and have T8's order creation call tryOccupy as the FIRST transactional step... no wait, atomicity.

Hmm, let me reconsider. What if we eliminate the concurrency deadlocks entirely by serializing row creation at a coarser level: use `INSERT IGNORE` into... 

NEW IDEA: pre-create the needed rows in a separate, already-committed transaction?? Still races.

ANOTHER IDEA — the classic solution to "insert or update" hotspot: keep the spec's exact algorithm but make the FIRST step's gap-lock harmless by using `INSERT IGNORE` before any locking read, ensuring the row exists, then do conditional UPDATE:

per date:
1. `INSERT IGNORE INTO daily_inventory (type, date, total, 0)` — plain insert, no gap locks, no S-lock-held problem? INSERT IGNORE on duplicate: does it also take S lock on existing record? INSERT IGNORE does a dup check too — sets S lock?? Hmm, same mechanism. But wait — with INSERT IGNORE, the dup check on a COMMITTED existing row returns immediately with "ignored"... does it still acquire an S lock held to commit? I believe the dup-check S lock acquisition happens on INDEX RECORD — for INSERT IGNORE similarly. Damn.

Let me verify: our current deadlock came from failed plain INSERT (exception). INSERT IGNORE might behave the same (S lock). Probably yes.

OK here's another thought — why not make the occupancy check-and-increment itself the INSERT... no.

FUNDAMENTALLY DIFFERENT ANGLE: avoid concurrent creation races by pre-creating inventory rows for a wide date window at startup/seed time? The design says rows are created lazily... §4.2/§5.1 says rows created on first booking. Pre-creation changes design.

PRAGMATIC ANGLE: The application-level deadlock retry is standard practice. Put the retry inside InventoryService, structured so T8 keeps atomicity:

- Public `tryOccupy(typeId, checkin, checkout)`: 
  ```java
  for (int attempt = 1; ; attempt++) {
      try {
          occupyInTransaction(typeId, checkin, checkout);  // self-injection, @Transactional(REQUIRES_NEW)? 
          return;
      } catch (DeadlockLoserDataAccessException | CannotAcquireLockException e) {
          if (attempt >= 3) throw e;
      }
  }
  ```
  
  For T8 atomicity: T8's order service wants occupy+insert in one tx. If tryOccupy uses REQUIRES_NEW self-tx, occupies commit independently of order insert — WRONG (order insert fails → occupies leak).

Alternative for T8: OrderService.createOrder has its own @Transactional; it calls inventoryService's transactional occupy via REQUIRED (join). If deadlock occurs inside, OrderService catches DeadlockLoserDataAccessException OUTSIDE its transactional method... i.e., OrderService.createOrder is also split: public non-transactional retry wrapper + self-injected transactional createOrderInTransaction. That's a known pattern and keeps atomicity.

So InventoryService.tryOccupy itself (for T7's direct interface and concurrency test) can have the retry wrapper with REQUIRED... but REQUIRED joining + retry doesn't work (join existing tx → deadlock kills whole tx → retry impossible inside). Hmm, but when called directly from the test (no outer tx), each call to the transactional self-method starts a fresh tx; retry loop creates new txs. When called from T8 inside an outer tx: join → deadlock → exception propagates to T8's wrapper, which retries the whole creation. Consistent!

Wait, but there's subtlety: if tryOccupy joins an existing tx and gets a deadlock, its internal retry loop would retry within the doomed tx. So the retry loop must detect "am I in an existing transaction?" → TransactionSynchronizationManager.isActualTransactionActive()... getting hairy.

SIMPLER: InventoryService exposes tryOccupy as @Transactional (REQUIRED) with NO retry. Retry logic lives where the transaction boundary is owned. For T7's acceptance test, the test calls tryOccupy directly (each call = own tx) — test could retry... but acceptance says "对该接口多线程调用，成功数恰为房间数" — retry in test harness is legitimate? TC-G01 in T8 will test via order API with retry at order service. Hmm, but T7's验收 explicitly: "N（≥5）线程并发 tryOccupy 同区间，成功数恰为房间数、occupied 永不超 total". If InventoryService.tryOccupy itself doesn't handle deadlock retries, the test can't guarantee exactly 3 successes without retrying.

Hmm wait — maybe I should reconsider. Is there a way to make concurrent tryOccupy deadlock-FREE under RR? Let me think harder.

Deadlock source: S locks from failed INSERT dup-checks held to commit. What if the INSERT NEVER fails? I.e., ensure row existence via a mechanism without dup collisions. E.g., rows pre-created per date on first use but guarded by... chicken-and-egg.

What if total row creation uses a separate table lock approach: `SELECT COUNT(*) FROM room WHERE room_type_id=? FOR UPDATE`? Lock ALL room rows of that type (record locks on few rows — 3 rooms). This serializes creation AND occupation per room type! Flow per tryOccupy: lock rooms of type (FOR UPDATE on ≤ few rows, consistent order by id) → then for each date: SELECT (now safe)... 

With per-type serialization, the entire tryOccupy for a type is serialized → no inventory-row deadlocks possible (only one tx touches a type's inventory at a time). Concurrency across DIFFERENT types preserved. Demo scale: totally fine. But: all threads in the test serialize — still exactly 3 succeed, occupied never exceeds. ✓ And it's deadlock-free IF the room-row locks are acquired in consistent order (by id) AND no other code path locks room rows in different order (check-in in T9 locks a room row FOR UPDATE! §5.3 step 2: `SELECT * FROM room WHERE id=? FOR UPDATE`). DANGER: T9 check-in locks a single room row while tryOccupy locks all rooms of a type → potential deadlock between check-in and booking?? They touch different tables otherwise: check-in locks room row + updates hotel_order; tryOccupy locks room rows + updates daily_inventory. Cycle: check-in holds room#5, wants hotel_order row; tryOccupy holds nothing on hotel_order... tryOccupy wants room#5 lock (held by check-in) — tryOccupy waits; check-in doesn't need anything tryOccupy holds → no cycle. Wait — tryOccupy holds room#1..4 locks while waiting for room#5; check-in needs only room#5 → no cycle. OK. But lock ordering between multiple check-ins (room ids) and tryOccupy (ascending ids) — check-ins lock ascending too (single room). Fine.

Hmm, but this adds a serialization point NOT in the tech doc, and it reduces booking concurrency per type to 1 — for the demo that's fine, but the interview story "防超卖靠原子条件更新" still holds; the FOR UPDATE on rooms is just ordering. Hmm, honestly it changes the concurrency story significantly.

Let me step back and reconsider: what do real systems do? Application-level retry on deadlock is THE standard answer (MySQL docs: "try restarting transaction"). I'll implement the retry at the InventoryService level for the standalone case, designed so it composes with T8:

Actually wait — simpler: make the retry unconditional but the inner transactional method self-injected with REQUIRED. When NO outer tx: each attempt is a fresh tx → retry works. When outer tx exists (T8): first attempt joins outer tx; if deadlock → exception → the outer tx is doomed; retry inside the loop would rejoin the doomed tx... So guard: if TransactionSynchronizationManager.isSynchronizationActive()/actual tx existed before, don't retry — propagate. 

```java
public void tryOccupy(...) {
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
        doTryOccupy(...);  // 加入调用方事务，死锁重试由事务属主负责
        return;
    }
    for (int attempt = 0; ; attempt++) {
        try { self.doTryOccupy(...); return; }
        catch (CannotAcquireLockException | DeadlockLoserDataAccessException e) {
            if (attempt >= 2) throw e;
        }
    }
}
```

DeadlockLoserDataAccessException extends CannotAcquireLockException? Actually: DeadlockLoserDataAccessException extends PessimisticLockingFailureException extends ConcurrencyFailureException. CannotAcquireLockException extends PessimisticLockingFailureException too. Catch PessimisticLockingFailureException (covers deadlock loser + lock wait timeout + cannot acquire).

Hmm, but is this complexity justified? It preserves: spec algorithm unchanged (UPDATE→INSERT→retry), T7 test passes with exactly 3 successes, T8 composability documented. But "isActualTransactionActive" check is a bit smelly.

Alternative cleaner composition: make T8's OrderService own the retry too — i.e., InventoryService.tryOccupy = pure @Transactional REQUIRED (no retry). Retries belong to whoever owns the transaction boundary. For T7 acceptance, write the TEST to retry deadlocks? That weakens the acceptance ("接口多线程调用，成功数恰为房间数" — with test-side retry of deadlock victims, still meaningful: it verifies no oversell + all-that-can-succeed-do-succeed). But TC-G01's spirit is that exactly room-count succeed and others get SOLD_OUT — a thread that needed 2 attempts still ends with one success. The user-visible semantics preserved.

Hmm, honestly — the cleanest engineering answer that keeps the test honest AND gives T8 a sane building block: put the retry inside InventoryService with the transaction-owner check. It's ~10 lines. T8 will call tryOccupy inside its own tx; if a deadlock happens there (rare: needs concurrent order creations racing), OrderService... would propagate a 500-ish error. Is that acceptable for T8? TC-G01/G02 run 10-20 threads through order creation — deadlocks WILL occur (same pattern, since order creation includes tryOccupy). So OrderService needs the retry as well, or it delegates tx ownership to inventory... 

You know what — let me reconsider ONE more time whether there's a deadlock-free pure-SQL flow under RR.

The S→X upgrade deadlock requires: (a) two txs each hold S on same record, (b) both request X. S comes from failed INSERT dup check. What if retriers, after catching DuplicateKeyException, FIRST release... can't release S lock mid-tx.

What if INSERT uses INSERT IGNORE? Does INSERT IGNORE's dup check also hold S to commit? I think yes for unique checks... Actually, let me test empirically! Quick experiment: two sessions, INSERT IGNORE same key in tx, then check information_schema... simpler: just try implementing with INSERT IGNORE + conditional UPDATE and run the test. Actually hmm, wait: even simpler idea — the dup-check S lock is acquired on the EXISTING record. What if the row creation collision is made extremely rare by including... no.

Actually, let me look at this from a different well-trodden path: `INSERT ... ON DUPLICATE KEY UPDATE occupied = LAST_INSERT_ID(...)`... the classic IODKU deadlock is exactly this S→X problem. Known.

KNOWN GOOD PATTERN for counters under concurrency: single UPDATE on pre-existing row. Row pre-existence is the key. What if daily_inventory rows are created by a separate lightweight process... 

OR: create the row in its OWN transaction before starting the occupy transaction (REQUIRES_NEW, commit immediately):
```
ensureRow(type, date):  // own tx
    INSERT IGNORE ...; commit;
then in main tx: conditional UPDATE (row now exists committed → pure record lock, no gap, no S-from-dup... wait INSERT IGNORE in ensureRow's own tx: the S lock from dup check is released when ensureRow's tx COMMITS immediately! Locks die with the short tx. Then main tx does UPDATE on existing row → clean record-lock serialization!
```

Deadlock analysis: main txs only do UPDATEs on existing rows (record locks, ascending date order) + no INSERTs (INSERTs happen only in tiny independent txs). Cross-tx cycles: tx_A holds day1, wants day2 (record lock held by tx_B?) — tx_B holds day2, wants day1? Impossible: ascending order, B holds day2 only after day1, but day1 held by A → B can't hold day2 while A holds day1... wait B holds day1? No: A holds day1 lock. B's UPDATE day1 blocks. So B can't reach day2. So no tx holds day2 while another holds day1... EXCEPT via the ascending order: A holds day1, gets day2, commits; B then gets day1, then day2. Serialized on day1 effectively, but deadlock-FREE. ✓✓

And the ensureRow mini-tx: INSERT IGNORE may briefly take S lock (dup) but tx commits instantly, releasing all locks. Could ensureRow mini-txs deadlock among themselves? Concurrent INSERT IGNORE same key: waiters wait on creator's commit → get "ignored" (0 affected) — dup-check S locks released at commit (immediately). Any cycle? INSERT IGNORE holds no other locks (fresh tx) while waiting → no cycle. ✓

But WAIT: does the main-tx UPDATE really avoid gap locks if the row was created by ensureRow but ensureRow's tx... it commits before main tx proceeds → row is committed → UPDATE sees existing row via unique index → record lock only. ✓

BUT: mini-tx REQUIRES_NEW inside the caller's transaction (T8): ensureRow commits independently. If outer tx later rolls back, the inventory ROW remains (with occupied=0 — harmless! It's just an empty row; queryAvailability treats existing row with occupied 0 same as room count... remaining = total - 0 = total ✓). Semantically harmless residue. 

Also: does REQUIRES_NEW with same datasource cause connection... it takes a second connection from pool. Deadlock between outer tx's locks and inner tx's? Outer tx at ensureRow time holds locks from previous days (day1 X lock). Inner tx INSERT IGNORE day2: if day2 row exists committed → S lock dup check → no wait... unless another tx holds X on day2 — held only by txs that UPDATEd day2 — they'd hold day1 too — but outer holds day1 → contradiction → no one else holds day2. If row doesn't exist → plain insert, no conflict (unique key race: another ensureRow inserting concurrently → wait on their commit → brief). No cycle with outer. ✓

Hmm, but actually wait: there's still one subtle case — two outer txs both passed day1?? Impossible as shown (day1 record lock serializes). Two outer txs with DISJOINT date ranges for same type: A: [d1,d2), B: [d2,d3). A holds d1 (X), A UPDATEs d2 (X, after its ensureRow). B ensureRows d2 (row exists now? A's UPDATE on d2 row — row exists (A or someone ensureRow'd it) → B's INSERT IGNORE → dup check → S lock request on d2 record → BLOCKS on A's X lock! B's mini-tx waits. A commits → B proceeds. Meanwhile B holds nothing else. No cycle. ✓ B then UPDATEs d2 (X after A commit) etc. Fine.

What about RELEASE path (cancel/checkout): only UPDATEs on existing rows, ascending — same no-cycle argument. ✓

And the earlier observed deadlock mode (S from failed INSERT held in MAIN tx + X upgrade) — eliminated because main tx never INSERTs. The gap-lock deadlock — eliminated because main tx never does locking read on absent rows (ensureRow guarantees committed existence). 

This is a clean, deadlock-free design preserving the spec's core (atomic conditional UPDATE is THE occupancy mechanism; unique index prevents dup rows). The only addition: row creation happens in a tiny independent transaction (documented deviation: 建行与占用分离，空行无害）.

Implementation:
- `InventoryService.tryOccupy` stays @Transactional (REQUIRED).
- Row creation: needs a REQUIRES_NEW transactional method — self-injection or separate component. Options: inject a `InventoryRowCreator` @Component with @Transactional(propagation = REQUIRES_NEW) method `ensureRow(typeId, date, totalRooms)` doing INSERT IGNORE (or plain insert catching dup). Since the mini-tx commits regardless, catching DuplicateKeyException inside is fine (MySQL doesn't doom tx on dup error).
  - Use INSERT IGNORE via @Insert("INSERT IGNORE INTO daily_inventory ...") — no exception needed; if another tx has uncommitted insert, waits, then ignored. 0 or 1 affected, we don't care.
  
- In occupyOneDate: 
  1. conditional UPDATE → 1 → done.
  2. 0 → row may not exist (or full). ensureRow (REQUIRES_NEW). 
  3. conditional UPDATE again → 1 → done; 0 → SOLD_OUT (row exists and full).
  
  This preserves spec structure exactly ("UPDATE → 0 → INSERT → retry UPDATE once → SOLD_OUT") with the INSERT relocated to a mini-tx.

- Self-injection: separate @Component is cleaner than self-injection: `InventoryRowInitializer`? Hmm, keep it inside inventory module: class `InventoryRowCreator` with mapper. Or put ensureRow in InventoryService via self-injected proxy... separate small class is cleaner. Actually — simplest: since the mini-tx has no rollback concern, could use a plain new DataSource connection? No — use Spring tx properly.

Wait, actually there's an even simpler alternative to REQUIRES_NEW: `@Transactional(propagation = REQUIRES_NEW)` on a method in a SEPARATE bean to make proxy work. Fine, create `InventoryRowCreator` component in inventory package.

Hmm, hold on — one more consideration: REQUIRES_NEW suspends outer tx and uses a SECOND pooled connection. Under test with 10 threads + pool size default 10 — each thread's outer tx holds 1 connection; ensureRow needs another → pool exhaustion deadlock (Hikari waits 30s then fails)! Pool default size 10; 10 threads each holding outer connection → 0 left for REQUIRES_NEW → ensureRow waits for connection up to 30s → failure. MUST avoid. Options: increase pool size in config (spring.datasource.hikari.maximum-pool-size=20)? Feels fragile.

Alternative without second connection: JDBC savepoint? No — need commit.

Alternative: restructure so row creation happens BEFORE the main transaction starts?? In T8 the caller is order creation tx. Hmm.

Alternative: do ensureRow OUTSIDE/SUSPENDING... same problem.

Hmm. What if ensureRow uses the SAME connection but commits?? Can't — would commit outer tx's work.

OK so REQUIRES_NEW has a pool-exhaustion hazard: outer tx holds conn, inner needs another. With 10 concurrent tryOccupy each needing a second conn briefly: pool 10 → 10 outer conns held → ensureRow blocks. Mitigate: set hikari maximum-pool-size higher (e.g. 30). For demo fine but... meh, it's a real consideration: max threads = server tomcat 200 default. Outer tx during booking holds conn while ensureRow briefly borrows another — pool needs headroom. Setting pool size 20-30 is normal.

Hmm, wait — actually is there really no simpler deadlock-free pure approach? Let me reconsider the problem from scratch...

What about making the conditional UPDATE not take gap locks on missing rows by using READ COMMITTED just for this tx? @Transactional(isolation = READ_COMMITTED) — can't change isolation when joining existing tx (T8). When standalone (T7 test) it works. In RC:
- UPDATE on missing row: NO gap locks (RC). 
- Then INSERT: T1 inserts; others' INSERT waits on... in RC, INSERT waiting on uncommitted same-key insert: waits on the record lock (S request). Others hold NO locks (their UPDATEs found nothing, took no locks in RC). No cycle → T1 commits → others get dup error → S lock acquired on record (dup check) held to commit? — then retry UPDATE wants X → S→X upgrade deadlock among retriers AGAIN (if S locks held). 

Is the dup-check S lock held to commit in RC? Reading MySQL docs from memory: "INSERT ... sets an exclusive lock on the inserted row ... If a duplicate-key error occurs, a shared lock on the duplicate index record is set." — held till tx end presumably. But in READ COMMITTED, locks on non-matching rows are released after statement evaluation ("InnoDB releases the locks on rows that do not match") — that applies to evaluated rows of locking reads/UPDATEs, unclear for dup-check S locks.

I genuinely don't remember. Could test empirically with RC: set @Transactional(isolation=READ_COMMITTED) on tryOccupy + spec-original flow (UPDATE→INSERT→retry) and run the concurrent test standalone. If it passes, then T8 must run order creation in RC too (or order tx with RC isolation — settable since OrderService owns its tx: @Transactional(isolation=READ_COMMITTED) on createOrder — allowed since OrderService owns boundary!). Interesting: each top-level service method sets its own isolation. That works with Spring.

RC + original-spec flow: gap locks gone (RC doesn't gap-lock for unique-index equality misses... mostly — dup checks still gap? In RC: "gap locking is only used for foreign-key constraint checking and duplicate-key checking"). The INSERT dup-check gap locks: for INSERT into a gap, insert intention locks are used regardless. Concurrent INSERTs of the SAME key: insert intention locks for the same position conflict?? Insert intention locks don't conflict with each other (they're a wait for gap-lock holders). Without gap locks (RC), T1 inserts; T2..T10's INSERTs: unique check finds T1's uncommitted record → wait on S lock of that record. Hold nothing. No cycle. T1 commits → all get dup error, S locks briefly... if released at statement end in RC → retry UPDATEs serialize on X record lock cleanly → 3 successes!! If S locks are held to commit → upgrade deadlock → test fails.

This is very testable. Let me just empirically test approach: READ COMMITTED + spec-original algorithm.

Also simpler possibility: keep RR but ensure row-exists check + pure INSERT-first... we saw failed INSERT in main tx = S lock held → deadlock. 

Honestly, the mini-tx approach is GUARANTEED correct (I can reason it fully), at the cost of pool headroom + small design deviation. The RC approach may work but relies on uncertain S-lock-release semantics, and changes isolation semantics app-wide-ish (only per-tx though). The retry approach is standard but changes ownership structure.

Let me just try the RC experiment — it's a 2-line change. If it passes reliably (run test multiple times), adopt with a comment. Actually hmm, one more wrinkle for RC in T8: order creation in RC — the order status conditional UPDATEs etc. don't need gap locks; RC is fine and common for such apps (many run RC in production, e.g., with binlog row format).

Experiment: add isolation to tryOccupy:

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
```

and revert occupyOneDate to the spec-original (UPDATE first → INSERT → retry once). Run concurrent test a few times.

Actually wait — before that, also consider: maybe keep current exists-check version + RC? The exists check is a no-lock read; harmless. But original-spec is simpler and matches doc. With RC, UPDATE-first on missing row: no gap lock, no locks at all. Good.

Let me try.

</details>

死锁报告很清楚了：两个事务各自持有该行记录的 **S 锁**（来自失败 INSERT 的重复键检查，且 S 锁持有到事务结束），随后都请求 X 锁做 UPDATE → S→X 升级死锁。这是 REPEATABLE READ 下"INSERT 撞唯一索引后在同事务重试 UPDATE"的固有问题。

改成 READ COMMITTED 可以消除两类锁问题（不产生间隙锁；唯一索引等值未命中不加锁）。试一下：恢复技术方案 §5.1 的原始算法，并给 tryOccupy 指定 READ COMMITTED 隔离级别：

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_6LoC7TPsL5Ftpgqice5C592Y -->
```json
{
  "new_string": "    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     * READ COMMITTED：REPEATABLE READ 下对不存在的库存行做加锁 UPDATE 会产生间隙锁，\n     * 且并发 INSERT 撞唯一索引留下的 S 锁持有到事务结束，两者都会导致并发占用死锁。\n     */\n    @Transactional(isolation = Isolation.READ_COMMITTED)\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {",
  "old_string": "    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     */\n    @Transactional\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_6LoC7TPsL5Ftpgqice5C592Y -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_3jF98Xb0xB6JpqUrvpoN1Co9 -->
```json
{
  "new_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        if (inventoryMapper.occupyOne(typeId, date) == 1) {\n            return;\n        }\n        // 影响 0 行：该行可能不存在（首次预订该日），尝试建行；撞唯一索引说明并发已建行\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 回到原子条件更新重试一次\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "old_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 无锁一致性读判断行是否存在：行存在时直接走原子条件更新（记录锁），\n        // 避免对不存在的行做加锁 UPDATE 产生间隙锁导致并发 INSERT 死锁\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (exists) {\n            if (inventoryMapper.occupyOne(typeId, date) != 1) {\n                throw new BizException(ErrorCode.SOLD_OUT);\n            }\n            return;\n        }\n        // 首次预订该日：建行（occupied=1），撞唯一索引说明并发已建行，回到原子条件更新重试一次\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 并发已建行，重试条件更新\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_3jF98Xb0xB6JpqUrvpoN1Co9 -->
old_string not found in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java, the file contents may be out of date. Please use the Read Tool to reload the content.


</details>

<details><summary>Thinking</summary>

The old_string doesn't match — maybe I misremember the exact comment text. Read the file.

</details>

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_OjZONTKkLujKoYx843Ss2jzn -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_OjZONTKkLujKoYx843Ss2jzn -->
1	package com.example.hotel.inventory;
2	
3	import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
4	import com.example.hotel.common.api.ErrorCode;
5	import com.example.hotel.common.exception.BizException;
6	import com.example.hotel.inventory.entity.DailyInventory;
7	import com.example.hotel.inventory.mapper.InventoryMapper;
8	import org.springframework.dao.DuplicateKeyException;
9	import org.springframework.stereotype.Service;
10	import org.springframework.transaction.annotation.Transactional;
11	
12	import java.time.LocalDate;
13	import java.util.List;
14	import java.util.Map;
15	import java.util.stream.Collectors;
16	
17	@Service
18	public class InventoryService {
19	
20	    private final InventoryMapper inventoryMapper;
21	
22	    public InventoryService(InventoryMapper inventoryMapper) {
23	        this.inventoryMapper = inventoryMapper;
24	    }
25	
26	    /** 区间 [checkin, checkout) 剩余可订量：逐日剩余的最小值；无库存行的日期按当前房间数计 */
27	    public int queryAvailability(Long typeId, LocalDate checkin, LocalDate checkout) {
28	        int totalRooms = inventoryMapper.countRooms(typeId);
29	        List<DailyInventory> rows = inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
30	                .eq("room_type_id", typeId)
31	                .ge("stay_date", checkin)
32	                .lt("stay_date", checkout));
33	        Map<LocalDate, DailyInventory> byDate = rows.stream()
34	                .collect(Collectors.toMap(DailyInventory::getStayDate, row -> row));
35	        int minRemaining = totalRooms;
36	        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
37	            DailyInventory row = byDate.get(date);
38	            int remaining = row == null ? totalRooms : row.getTotalCount() - row.getOccupiedCount();
39	            minRemaining = Math.min(minRemaining, remaining);
40	        }
41	        return minRemaining;
42	    }
43	
44	    /**
45	     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。
46	     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。
47	     * READ COMMITTED：REPEATABLE READ 下对不存在的库存行做加锁 UPDATE 会产生间隙锁，
48	     * 且并发 INSERT 撞唯一索引留下的 S 锁持有到事务结束，两者都会导致并发占用死锁。
49	     */
50	    @Transactional(isolation = Isolation.READ_COMMITTED)
51	    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
52	        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
53	            occupyOneDate(typeId, date);
54	        }
55	    }
56	
57	    /** 释放区间 [checkin, checkout) 每日 1 间，带 occupied_count > 0 保护，按日期升序 */
58	    @Transactional
59	    public void release(Long typeId, LocalDate checkin, LocalDate checkout) {
60	        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
61	            inventoryMapper.releaseOne(typeId, date);
62	        }
63	    }
64	
65	    private void occupyOneDate(Long typeId, LocalDate date) {
66	        // 无锁一致性读判断行是否存在：行存在时直接走原子条件更新（记录锁），
67	        // 避免对不存在的行做加锁 UPDATE 产生间隙锁导致并发 INSERT 死锁
68	        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
69	                .eq("room_type_id", typeId)
70	                .eq("stay_date", date)) > 0;
71	        if (exists) {
72	            if (inventoryMapper.occupyOne(typeId, date) != 1) {
73	                throw new BizException(ErrorCode.SOLD_OUT);
74	            }
75	            return;
76	        }
77	        // 首次预订该日：建行（occupied=1）；撞唯一索引说明并发已建行，回到原子条件更新重试一次
78	        DailyInventory row = new DailyInventory();
79	        row.setRoomTypeId(typeId);
80	        row.setStayDate(date);
81	        row.setTotalCount(inventoryMapper.countRooms(typeId));
82	        row.setOccupiedCount(1);
83	        try {
84	            inventoryMapper.insert(row);
85	            return;
86	        } catch (DuplicateKeyException ignored) {
87	            // 并发已建行，重试条件更新
88	        }
89	        if (inventoryMapper.occupyOne(typeId, date) != 1) {
90	            throw new BizException(ErrorCode.SOLD_OUT);
91	        }
92	    }
93	}

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_y4ng5t0wN3ZDQTvRxr0EjkHe -->
```json
{
  "new_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        if (inventoryMapper.occupyOne(typeId, date) == 1) {\n            return;\n        }\n        // 影响 0 行：该行可能不存在（首次预订该日），尝试建行；撞唯一索引说明并发已建行\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 回到原子条件更新重试一次\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "old_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 无锁一致性读判断行是否存在：行存在时直接走原子条件更新（记录锁），\n        // 避免对不存在的行做加锁 UPDATE 产生间隙锁导致并发 INSERT 死锁\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (exists) {\n            if (inventoryMapper.occupyOne(typeId, date) != 1) {\n                throw new BizException(ErrorCode.SOLD_OUT);\n            }\n            return;\n        }\n        // 首次预订该日：建行（occupied=1）；撞唯一索引说明并发已建行，回到原子条件更新重试一次\n        DailyInventory row = new DailyInventory();\n        row.setRoomTypeId(typeId);\n        row.setStayDate(date);\n        row.setTotalCount(inventoryMapper.countRooms(typeId));\n        row.setOccupiedCount(1);\n        try {\n            inventoryMapper.insert(row);\n            return;\n        } catch (DuplicateKeyException ignored) {\n            // 并发已建行，重试条件更新\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_y4ng5t0wN3ZDQTvRxr0EjkHe -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

<details><summary>Thinking</summary>

Need to add the Isolation import. Also note: when joining an existing transaction (T8), Spring will throw InvalidIsolationLevelException if the outer tx has different isolation... Actually Spring only applies the isolation when CREATING a new transaction; with REQUIRED joining an existing tx, Spring validates the isolation level matches (validateExistingTransaction=false by default → it does NOT validate, silently participates). Default behavior: participates without complaint. Good — so T8's order tx (default RR) + tryOccupy joining → runs under RR anyway! Hmm, that means in T8 the isolation READ_COMMITTED would be silently IGNORED when joining the order tx. That reintroduces deadlock risk in T8's concurrency tests (TC-G01/G02 via order creation)!

So for T8 I'd need createOrder's @Transactional(isolation=READ_COMMITTED) too — the top-level owner sets it. I'll note that for T8. For now T7 standalone works.

Add import.

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_49aRC5X1CaFPLbWt2YuJgIZn -->
```json
{
  "new_string": "import org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Isolation;\nimport org.springframework.transaction.annotation.Transactional;",
  "old_string": "import org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Transactional;",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_49aRC5X1CaFPLbWt2YuJgIZn -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

#### Tool Call: Bash (`cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | gre…`)
<!-- call_id: tool_8HmccDepzmiK8ryuh1b2Y4QO -->
```json
{
  "command": "cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | grep -E \"Tests run:|FAILURE\" | head -3; echo \"--- run $i done ---\"; done",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | gre…`)</summary>

<!-- call_id: tool_8HmccDepzmiK8ryuh1b2Y4QO -->
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.350 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.071 s <<< FAILURE!
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
--- run 1 done ---
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.187 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.062 s <<< FAILURE!
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
--- run 2 done ---
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.188 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.066 s <<< FAILURE!
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
--- run 3 done ---


</details>

#### Tool Call: Bash (`cd hotel-server && grep -B2 -A8 "AssertionFailedError" targe…`)
<!-- call_id: tool_6OarCAzzIlLQD5XWgLIU3PUO -->
```json
{
  "command": "cd hotel-server && grep -B2 -A8 \"AssertionFailedError\" target/surefire-reports/com.example.hotel.inventory.InventoryServiceTest.txt | head -20; docker exec hotel-mysql mysql -uroot -proot123 -e \"SHOW ENGINE INNODB STATUS\\G\" 2>/dev/null | sed -n '/LATEST DETECTED DEADLOCK/,/WE ROLL BACK/p' | grep -E \"query id|lock mode|WAITING|HOLDS|RECORD LOCKS.*index\" | head -12"
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && grep -B2 -A8 "AssertionFailedError" targe…`)</summary>

<!-- call_id: tool_6OarCAzzIlLQD5XWgLIU3PUO -->
Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.188 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryServiceTest
com.example.hotel.inventory.InventoryServiceTest.tryOccupy_concurrent_neverOversells -- Time elapsed: 0.066 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 
未知异常: [org.springframework.dao.DeadlockLoserDataAccessException: 
### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
### The error may exist in com/example/hotel/inventory/mapper/InventoryMapper.java (best guess)
### The error may involve com.example.hotel.inventory.mapper.InventoryMapper.occupyOne-Inline
### The error occurred while setting parameters
### SQL: UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count
### Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
; Deadlock found when trying to get lock; try restarting transaction, org.springframework.dao.DeadlockLoserDataAccessException: 
MySQL thread id 83, OS thread handle 281472096059136, query id 1599 192.168.65.1 root updating
*** (1) HOLDS THE LOCK(S):
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 2044 lock mode S
*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 2044 lock_mode X locks rec but not gap waiting
MySQL thread id 85, OS thread handle 281472430034688, query id 1602 192.168.65.1 root updating
*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 2047 lock mode S
*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 7 page no 5 n bits 72 index uk_inventory_type_date of table `hotel`.`daily_inventory` trx id 2047 lock_mode X locks rec but not gap waiting


</details>

<details><summary>Thinking</summary>

Same S→X upgrade deadlock even in READ COMMITTED — so the dup-check S locks ARE held to transaction end in RC too. The failed-INSERT-then-retry-UPDATE pattern is inherently deadlock-prone when 2+ transactions collide on row creation. Confirmed empirically.

So the viable deadlock-free design: main transaction never executes a colliding INSERT. Row creation must happen in a separate tiny transaction that commits immediately (releasing any dup-check S locks), OR rows must otherwise pre-exist.

Plan: `InventoryRowCreator` component with `@Transactional(propagation = REQUIRES_NEW)` ensureRow (INSERT IGNORE). occupyOneDate: conditional UPDATE → 0 → ensureRow (new tx, commits) → conditional UPDATE → 0 → SOLD_OUT.

Pool exhaustion concern: outer tx holds a connection; REQUIRES_NEW borrows a second. Hikari default maximum-pool-size = 10. Test uses 10 threads → all 10 connections held by outer txs → ensureRow waits → 30s timeout → failure. So must either raise pool size or limit... Set `spring.datasource.hikari.maximum-pool-size` in application.yml to something like 30. Reasonable for a demo app. Also concurrency: with pool 30 and 10 threads it works.

But hmm, wait. Let me think about whether there's an approach without REQUIRES_NEW and without pool concerns...

Alternative: pre-create the row OUTSIDE the caller transaction... same thing.

Alternative: could we avoid the S-lock issue by having only ONE tx do INSERT and others wait cleanly? The waiters' INSERTs don't hold locks while waiting (good), but after the creator commits, waiters acquire S locks for the dup check and get errors. The S lock acquisition is inherent to dup-key error in InnoDB. UNLESS the waiter never INSERTs a colliding key: i.e., waiter, after waiting, re-checks existence with a locking read? No — its INSERT IS the statement that waits; the wait happens inside INSERT.

What about `INSERT IGNORE`? Same dup mechanism → S locks? For INSERT IGNORE the dup row produces a warning not error; locks: I believe it still sets S lock on duplicate record. Probably same issue.

What about checking existence with SELECT ... FOR UPDATE first (in RC: no gap locks; missing row → no locks), then INSERT if missing? Race: two txs both see missing, both INSERT → one waits → after creator commits, second's INSERT gets dup error + S lock... same. But then the second can catch and just UPDATE — S lock held → upgrade deadlock with a third tx in same state. Same problem, smaller window.

The ONLY way to keep the main tx free of dup-check S locks: never INSERT a key that might collide within the main tx. So: creation in separate committed tx (REQUIRES_NEW), or pre-created rows (seed/migration — design change, lazy creation was specified), or named locks (GET_LOCK before INSERT — serializes creators; GET_LOCK in main tx, released at... GET_LOCK locks are released on RELEASE_LOCK or connection close, NOT on commit! With connection pooling that's dangerous — lock would survive commit and leak across usages unless explicitly released. Could release explicitly in finally. GET_LOCK approach: acquire named lock per (type,date) → SELECT existence (no lock) → INSERT if absent → conditional UPDATE → release named lock. The named lock serializes the whole check-insert-update per date → no dup collisions (only one inserter; others see committed row after waiting on named lock... wait, named lock waits don't see uncommitted... T1 holds named lock, inserts row, updates, releases (but T1's tx not committed!). T2 acquires named lock, SELECT — doesn't see T1's uncommitted row → INSERT → blocks on T1's record lock (uncommitted same key) → T1 commits → T2 dup error + S lock → but T2 is the ONLY named-lock holder → no other tx will request X on this row... until T2 releases and T3 enters — T3 SELECT sees committed row (T2 committed? not necessarily!). Ugh, T2 still in tx holding S, then UPDATE → X request; T3 blocked on named lock, not holding anything → no deadlock. Then T2 commits; T3 proceeds: SELECT sees row, UPDATE → waits for... nothing. OK actually GET_LOCK also works but has release discipline issues.

Decision: REQUIRES_NEW mini-tx for row creation + bump Hikari pool size. It's clean, reasoning is solid, matches spec's algorithm shape.

Implementation:

```java
@Component
class InventoryRowCreator {
    private final InventoryMapper inventoryMapper;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureRow(Long typeId, LocalDate date, int totalRooms) {
        inventoryMapper.insertIgnore(typeId, date, totalRooms);
    }
}
```

Add to InventoryMapper:
```java
@Insert("INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)")
int insertIgnore(...);
```

occupyOneDate:
```java
private void occupyOneDate(Long typeId, LocalDate date) {
    if (inventoryMapper.occupyOne(typeId, date) == 1) return;
    // 该行可能不存在：在独立小事务中建行（立即提交释放锁，避免本事务因撞唯一索引持 S 锁引发升级死锁）
    rowCreator.ensureRow(typeId, date, inventoryMapper.countRooms(typeId));
    if (inventoryMapper.occupyOne(typeId, date) != 1) throw new BizException(ErrorCode.SOLD_OUT);
}
```

Wait — hmm, one more check on REQUIRES_NEW + same-datasource deadlock: inner tx does INSERT IGNORE. If OUTER tx already holds a lock on that same row... outer did UPDATE first (returned 0 — either row missing: no locks in RC? in RR: gap locks!!). CAREFUL: outer tx's first UPDATE on missing row in RR takes gap locks; then inner tx INSERT IGNORE needs insert intention in that gap → blocked by OUTER's own gap locks → self-deadlock (inner waits for outer, outer waits for inner; inner lock-wait-timeout after 50s... or deadlock detector? Cross-connection self-deadlock is detected as lock wait timeout, not deadlock). So with RR outer, the gap-lock problem resurfaces between outer and its own inner tx!

Therefore tryOccupy MUST run in READ COMMITTED (no gap locks on the missed UPDATE) for the mini-tx approach to work. Keep @Transactional(isolation = READ_COMMITTED) on tryOccupy. In RC: outer UPDATE on missing row → no locks. Inner INSERT IGNORE → free. Inner commits. Outer re-UPDATE → record lock. ✓

For T8: order creation tx must also be READ_COMMITTED (set on OrderService.createOrder) so that tryOccupy joins an RC tx... wait — Spring: joining tx ignores the inner isolation setting (validateExistingTransaction=false default), so tryOccupy would run under the outer's isolation. If outer is RR → gap locks → self-deadlock with inner ensureRow. So T8 must declare RC at its top-level boundary. I'll note this deviation in the task list / will handle in T8.

Also release(): only UPDATEs on existing rows; RR fine but keep simple @Transactional — actually to be safe with the whole scheme, release only touches existing rows, no gap locks. Keep default.

Also: countRooms inside ensureRow — pass as arg. Fine.

Pool: bump to 30 in application.yml. Hmm, is changing application.yml acceptable? It's config, fine.

Wait, actually let me reconsider pool math: 10 test threads → 10 outer conns + up to 10 inner conns = 20 needed. Pool 30 OK. Tomcat prod: 200 threads but booking concurrency realistically low; and inner conn borrow is brief. Pool 30 could deadlock under 30+ concurrent bookings (each holding outer conn waiting for inner conn). Hikari would time out after 30s → exception. For demo scale acceptable. Note it.

Alternatively, eliminate REQUIRES_NEW by doing ensureRow BEFORE starting the tx: tryOccupy (non-transactional public): for each date → ensureRowEach (each own tx, @Transactional(REQUIRES_NEW) effectively via proxy on public method: each ensureRow call = own short tx); then call self.doTryOccupy @Transactional(RC) which only does conditional UPDATEs (rows all exist now!). But T8 joins: OrderService.createOrder tx calls tryOccupy — tryOccupy joins outer tx, and ensureRow inside would ALSO join the outer tx (REQUIRED) → no good, need REQUIRES_NEW again. Ugh.

Hmm wait, actually for T8: OrderService could call inventoryService.ensureRowsForRange (non-tx, each date own tx via REQUIRES_NEW proxy) BEFORE opening its own transaction? OrderService.createOrder structure: public non-transactional method → ensureRows (separate txs) → self.createOrderInTransaction (own RC tx: validations? validations should come first, before occupying... BR-01 validate → ensure rows → tx(occupy + insert order)). That works! But it splits OrderService into two methods — acceptable pattern.

But then InventoryService.tryOccupy as the seam: for T7 standalone test, tryOccupy must itself ensure rows. Design tryOccupy as:

```java
public void tryOccupy(typeId, checkin, checkout) {   // 非事务：先建行（各自独立小事务），再事务内纯条件更新
    ensureRows(typeId, checkin, checkout);            // REQUIRES_NEW per date — but if joining outer tx (T8), REQUIRES_NEW still suspends & borrows 2nd conn. Same pool issue but no self-deadlock since outer tx hasn't taken any locks yet IF ensureRows happens before any UPDATE...
```

Order matters: in T8, outer tx calls tryOccupy → ensureRows FIRST (before any UPDATE in outer tx). Inner REQUIRES_NEW txs: outer tx holds NO locks yet (no statements executed? BR-01 validation does SELECTs — consistent reads, no locks). So no self-conflict! Then doTryOccupy joins outer tx — but isolation: outer must be RC. With outer RC and all rows pre-existing, the UPDATEs are pure record-lock. Deadlock-free. And tryOccupy standalone (T7 test): no outer tx; ensureRows runs each date in own tx (no outer conn held! pool fine); then doTryOccupy starts own RC tx — pure UPDATEs.

Self-deadlock analysis for T8 ordering: outer tx executes ensureRows (inner conns) BEFORE any locking statement in outer → inner never conflicts with outer. After that, outer only does record-lock UPDATEs on existing rows in ascending date order → no cycles with other txs doing the same. ✓✓ And in T7 standalone: no outer tx at all during ensureRows. ✓

What about ensureRow inner txs racing each other (different threads)? Concurrent INSERT IGNORE same key: waiters wait on creator's uncommitted insert → creator commits immediately (own tiny tx) → waiters proceed (ignored). No locks held by waiters → no cycle. ✓

So final design:

```java
@Service
public class InventoryService {
    // 自注入代理，保证 REQUIRES_NEW / 事务方法经代理生效
    private final InventoryService self;  // via ObjectProvider or @Lazy

    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
        // 先在独立小事务中逐日建行（立即提交，本事务绝不执行可能撞唯一索引的 INSERT，
        // 避免 REPEATABLE READ 下间隙锁与撞键 S 锁导致的并发死锁），随后只做原子条件更新
        for (date...) self.ensureRow(typeId, date);
        self.occupyAll(typeId, checkin, checkout);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void ensureRow(Long typeId, LocalDate date) {
        inventoryMapper.insertIgnore(typeId, date, countRooms(typeId));
    }

    @Transactional(isolation = READ_COMMITTED)
    public void occupyAll(...) { for dates: if occupyOne != 1 throw SOLD_OUT; }
}
```

Hmm, self-injection: use `@Lazy InventoryService self` or ObjectProvider. A bit smelly but common. Alternative: split into two beans (InventoryService + InventoryRowCreator) — cleaner. Let me do two beans:

- `InventoryService` (the seam): tryOccupy / release / queryAvailability.
- `InventoryRowCreator` (@Component, internal): ensureRow REQUIRES_NEW INSERT IGNORE.

tryOccupy: 
```java
public void tryOccupy(...) {
    for (date) rowCreator.ensureRow(typeId, date);   // 独立小事务建行
    occupyAll(...);  // 自身 @Transactional(RC) — but self-invocation! tryOccupy is not transactional, occupyAll called internally → proxy bypassed → no tx!
}
```

Self-invocation problem: tryOccupy (non-tx) calling this.occupyAll (@Transactional) bypasses proxy → runs without tx → each UPDATE autocommits → no整单回滚 capability, and T8-joining breaks (occupy wouldn't join T8's tx!). CRITICAL: occupy must join the caller's tx. So tryOccupy itself must be the @Transactional method, and ensureRows must NOT join it (REQUIRES_NEW). But then in T8 (outer tx exists), tryOccupy joins outer (REQUIRED + RC ignored), ensureRows suspends outer → outer hasn't taken locks yet? In T8, tryOccupy is called after BR-01 validations (consistent reads, no locks) → outer holds no locks → inner ensureRow safe. ✓ But wait — what if T8's outer tx is RR (default)? tryOccupy joins RR tx → the occupy UPDATEs run in RR — but rows all exist (ensureRows done) → UPDATE on existing row via unique index = record lock, no gap locks → fine even in RR! Since rows pre-exist, RR vs RC doesn't matter for the UPDATEs!! 

So: with ensure-first design, isolation doesn't even matter for correctness of the UPDATEs. 

Final structure:

```java
@Service
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryRowCreator rowCreator;

    /** 占用前先在独立小事务中逐日建行，再在调用方事务内做原子条件更新 */
    @Transactional
    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
        for (date : dates) {
            rowCreator.ensureRow(typeId, date);   // REQUIRES_NEW，立即提交
            if (inventoryMapper.occupyOne(typeId, date) != 1) throw SOLD_OUT;
        }
    }
}
```

Hmm wait — REQUIRES_NEW suspends the tryOccupy tx and borrows a second connection per date. Pool exhaustion: standalone test 10 threads → each tryOccupy tx holds 1 conn; ensureRow needs another → need pool ≥ 20. Set pool 30. OK.

But efficiency: ensureRow EVERY date for EVERY occupy — even when row exists (99% case). That's an extra round-trip + tx per date. INSERT IGNORE when row exists: suspends tx, borrows conn, dup-checks (S lock in mini-tx, released at immediate commit), commits. Wasteful. Optimize: only ensureRow when the first UPDATE returns 0:

```java
@Transactional
public void tryOccupy(...) {
    for (date : dates) occupyOneDate(typeId, date);
}

private void occupyOneDate(typeId, date) {
    if (inventoryMapper.occupyOne(typeId, date) == 1) return;
    // 行不存在（READ COMMITTED 下未命中不加锁）：独立小事务建行后重试一次
    rowCreator.ensureRow(typeId, date);
    if (inventoryMapper.occupyOne(typeId, date) != 1) throw new BizException(ErrorCode.SOLD_OUT);
}
```

Now the missed-UPDATE gap lock issue in RR returns (outer tx takes gap locks when UPDATE misses; then inner INSERT waits on outer's gap locks → self lock-wait). So tryOccupy must be RC when standalone. When joining T8's tx — T8 must own an RC tx. So keep @Transactional(isolation = READ_COMMITTED) on tryOccupy, and document that T8's createOrder must also declare RC (since joining inherits outer's isolation).

Alternatively avoid the missed-UPDATE entirely: SELECT existence first (consistent read, no locks ever, any isolation):
- SELECT count (no locks) → exists → UPDATE (record lock) → 0 → SOLD_OUT.
- not exists → ensureRow (REQUIRES_NEW) → UPDATE → 0 → SOLD_OUT?? can UPDATE be 0 after ensureRow? ensureRow guarantees committed row exists → UPDATE 0 only if full → SOLD_OUT. ✓

This is isolation-agnostic (no gap locks anywhere ever: SELECTs are consistent reads; UPDATEs only run on rows known to exist... well "known" via snapshot — could be stale: row created after snapshot not visible → ensureRow INSERT IGNORE → fine → UPDATE → record lock on existing → OK; row never deleted in prod. Tests delete rows! cleanup deletes → stale "exists" → UPDATE on deleted row → misses → gap lock in RR!! Only in test cleanup scenario + same tx — cleanup happens between tests, different txs. Snapshot staleness only within a tx. Within one tryOccupy tx, rows aren't deleted. OK.)

But honestly, the isolation-agnostic version with SELECT-first is robust regardless of how T8 configures its tx. And it matches "行在则条件更新、不在则建行重试" semantics. Slight extra SELECT per date — negligible.

Decision — final:

InventoryService:
```java
@Transactional
public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
    for (dates) occupyOneDate(typeId, date);
}

private void occupyOneDate(Long typeId, LocalDate date) {
    boolean exists = selectCount(...) > 0;   // 一致性读，不加锁
    if (exists) {
        if (occupyOne != 1) throw SOLD_OUT;
        return;
    }
    rowCreator.ensureRow(typeId, date);      // REQUIRES_NEW 建行并立即提交
    if (occupyOne != 1) throw SOLD_OUT;
}
```

Wait — but in RR standalone test: tryOccupy tx, SELECT (snapshot), UPDATE existing → record lock ✓. Concurrent creators: ensureRow mini-txs → no cycle ✓. Deadlock modes all gone. RC not needed at all. And T8 joining works under any isolation. 

Race re-check (10 threads, day1):
- All SELECT → no row (consistent). All call ensureRow: T1 inserts+commits; T2..T10 INSERT IGNORE wait on T1's uncommitted... T1 commits instantly → all proceed (0 affected, ignored). Mini-txs each commit. No locks held by waiters (fresh tx) → no deadlock ✓.
- All UPDATE day1 → serialize on record lock: 3 succeed, 7 get 0 → SOLD_OUT → rollback (nothing to roll back except their... nothing). ✓
- 3 winners → day2: SELECT — snapshot in RR! Winner W1's snapshot was taken at its first SELECT (day1, before day2 row created by W2?) — hmm: W1 does SELECT day2 → snapshot established at W1's day1 SELECT → doesn't see day2 row even if W2 created it → W1 ensureRow → INSERT IGNORE → if W2's day2 row committed → ignored (brief S lock in mini-tx, released) → UPDATE day2 → current read sees row → +1 ✓. If W2's insert uncommitted → W1's INSERT IGNORE waits → W2 commits → ignored → UPDATE ✓. No deadlock (mini-tx holds nothing else). ✓

All good. SELECT-first + REQUIRES_NEW ensureRow + conditional UPDATE. Isolation-agnostic, deadlock-free, spec-shaped.

Pool size: bump to 30 in application.yml? Test threads=10 → up to 10 outer + 10 inner = 20 conns. Default pool 10 → timeout failure. MUST bump. Set spring.datasource.hikari.maximum-pool-size: 30.

Hmm — actually wait, does the mini-tx REALLY need REQUIRES_NEW? If ensureRow joins the outer tx (REQUIRED), the INSERT happens within outer tx → colliding INSERT → dup error S lock held to outer commit → upgrade deadlock returns. Yes, REQUIRES_NEW is essential.

REQUIRES_NEW self-injection doesn't work (proxy); separate @Component InventoryRowCreator needed. It uses InventoryMapper (same module ✓).

Edge: what if tryOccupy called with NO outer tx — @Transactional starts tx (conn 1); ensureRow REQUIRES_NEW borrows conn 2 briefly. Fine.

What about release()? Only UPDATEs on existing rows (orders occupied them, so rows exist). Ascending. Record locks. No issues. Keep @Transactional. In T8, joins cancel tx. ✓

Also update the class comment explaining the concurrency design. Also fix InventoryMapper: add insertIgnore @Insert.

Update application.yml: add hikari pool size 30.

Test file: keep as is (with the unexpected-exceptions collector — good diagnostic, keep). Also the earlier read of QueryWrapper import still used. Let me now rewrite InventoryService and add InventoryRowCreator + mapper method.

</details>

READ COMMITTED 也没能解决——死锁报告依然是同样的 S→X 升级死锁（并发 INSERT 撞唯一索引后留下的 S 锁在 RC 下同样持有到事务结束）。结论：**主事务内只要执行可能撞唯一索引的 INSERT，就无法避免这个死锁**。

正确解法：把"建行"拆到独立的极小事务（`REQUIRES_NEW`，立即提交释放锁），主事务只做纯原子条件更新（行已存在 → 只产生记录锁，无间隙锁、无撞键 S 锁）：

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_jfc43ZWNGeecEt7wCTMPmnQQ -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.springframework.stereotype.Component;\nimport org.springframework.transaction.annotation.Propagation;\nimport org.springframework.transaction.annotation.Transactional;\n\nimport java.time.LocalDate;\n\n/**\n * 逐日库存行的创建器。建行必须在独立小事务中完成并立即提交：\n * 若在调用方事务内 INSERT 撞唯一索引，InnoDB 会为重复键检查持有该记录的 S 锁直到事务结束，\n * 随后并发事务再请求 X 锁（条件更新）会形成 S→X 升级死锁。\n */\n@Component\npublic class InventoryRowCreator {\n\n    private final InventoryMapper inventoryMapper;\n\n    public InventoryRowCreator(InventoryMapper inventoryMapper) {\n        this.inventoryMapper = inventoryMapper;\n    }\n\n    /** 建行（已存在则忽略），total_count 取当前房间数；独立事务立即提交，空行（occupied=0）无害 */\n    @Transactional(propagation = Propagation.REQUIRES_NEW)\n    public void ensureRow(Long typeId, java.time.LocalDate stayDate) {\n        inventoryMapper.insertIgnore(typeId, stayDate, inventoryMapper.countRooms(typeId));\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java"
}
```

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_Zy6q9m9QPYy93grKj8n5yBTt -->
```json
{
  "new_string": "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport org.apache.ibatis.annotations.Insert;\nimport org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Select;\nimport org.apache.ibatis.annotations.Update;\n\nimport java.time.LocalDate;\n\npublic interface InventoryMapper extends BaseMapper<DailyInventory> {",
  "old_string": "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Select;\nimport org.apache.ibatis.annotations.Update;\n\nimport java.time.LocalDate;\n\npublic interface InventoryMapper extends BaseMapper<DailyInventory> {",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_jfc43ZWNGeecEt7wCTMPmnQQ -->
Wrote 1199 bytes to hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java

</details>

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_Zy6q9m9QPYy93grKj8n5yBTt -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_iURNx0mXitM12qR6XlA0A5Ir -->
```json
{
  "new_string": "    @Select(\"SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}\")\n    int countRooms(@Param(\"typeId\") Long typeId);\n\n    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)\")\n    int insertIgnore(@Param(\"typeId\") Long typeId, @Param(\"stayDate\") LocalDate stayDate,\n                     @Param(\"totalRooms\") int totalRooms);\n}",
  "old_string": "    @Select(\"SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}\")\n    int countRooms(@Param(\"typeId\") Long typeId);\n}",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_iURNx0mXitM12qR6XlA0A5Ir -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java

</details>

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_oQJJOn7TTbr09ZPpKZojADKJ -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;\nimport com.example.hotel.common.api.ErrorCode;\nimport com.example.hotel.common.exception.BizException;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Transactional;\n\nimport java.time.LocalDate;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.stream.Collectors;\n\n@Service\npublic class InventoryService {\n\n    private final InventoryMapper inventoryMapper;\n    private final InventoryRowCreator rowCreator;\n\n    public InventoryService(InventoryMapper inventoryMapper, InventoryRowCreator rowCreator) {\n        this.inventoryMapper = inventoryMapper;\n        this.rowCreator = rowCreator;\n    }\n\n    /** 区间 [checkin, checkout) 剩余可订量：逐日剩余的最小值；无库存行的日期按当前房间数计 */\n    public int queryAvailability(Long typeId, LocalDate checkin, LocalDate checkout) {\n        int totalRooms = inventoryMapper.countRooms(typeId);\n        List<DailyInventory> rows = inventoryMapper.selectList(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .ge(\"stay_date\", checkin)\n                .lt(\"stay_date\", checkout));\n        Map<LocalDate, DailyInventory> byDate = rows.stream()\n                .collect(Collectors.toMap(DailyInventory::getStayDate, row -> row));\n        int minRemaining = totalRooms;\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            DailyInventory row = byDate.get(date);\n            int remaining = row == null ? totalRooms : row.getTotalCount() - row.getOccupiedCount();\n            minRemaining = Math.min(minRemaining, remaining);\n        }\n        return minRemaining;\n    }\n\n    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     * 并发安全：本事务只做\"对已存在行的原子条件更新\"（仅产生记录锁）；\n     * 建行由 InventoryRowCreator 在独立小事务中完成，避免撞唯一索引的 S 锁与间隙锁引发死锁。\n     */\n    @Transactional\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            occupyOneDate(typeId, date);\n        }\n    }\n\n    /** 释放区间 [checkin, checkout) 每日 1 间，带 occupied_count > 0 保护，按日期升序 */\n    @Transactional\n    public void release(Long typeId, LocalDate checkin, LocalDate checkout) {\n        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {\n            inventoryMapper.releaseOne(typeId, date);\n        }\n    }\n\n    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 一致性读判断行是否存在（不加锁，避免对不存在行的加锁 UPDATE 产生间隙锁）\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (!exists) {\n            // 首次预订该日：独立小事务建行（撞唯一索引自动忽略），再回到条件更新\n            rowCreator.ensureRow(typeId, date);\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_oQJJOn7TTbr09ZPpKZojADKJ -->
Wrote 3675 bytes to hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

<details><summary>Thinking</summary>

Wait — REQUIRES_NEW borrows a second connection while outer tx holds one. 10 test threads × 2 = 20 connections needed; Hikari default max is 10. Bump pool in application.yml. Also fix InventoryRowCreator's stray `java.time.LocalDate` param type (I imported LocalDate but used fully-qualified in signature). Let me clean that up.

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_QctAb8fpl4ExUtvIfIduoQtQ -->
```json
{
  "new_string": "    /** 建行（已存在则忽略），total_count 取当前房间数；独立事务立即提交，空行（occupied=0）无害 */\n    @Transactional(propagation = Propagation.REQUIRES_NEW)\n    public void ensureRow(Long typeId, LocalDate stayDate) {",
  "old_string": "    /** 建行（已存在则忽略），total_count 取当前房间数；独立事务立即提交，空行（occupied=0）无害 */\n    @Transactional(propagation = Propagation.REQUIRES_NEW)\n    public void ensureRow(Long typeId, java.time.LocalDate stayDate) {",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_QctAb8fpl4ExUtvIfIduoQtQ -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java

</details>

<details><summary>Thinking</summary>

REQUIRES_NEW 会让占用事务持有 1 个连接、建行小事务再借 1 个，并发 10 线程需要 20 个连接。Hikari 默认 maximum-pool-size=10，需要调大。修改 application.yml：

</details>

#### Tool Call: Edit (`hotel-server/src/main/resources/application.yml`)
<!-- call_id: tool_9x0yyvvnbNhFLCbhz1Bj9Dla -->
```json
{
  "new_string": "  datasource:\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/hotel?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false\n    username: ${MYSQL_USER:root}\n    password: ${MYSQL_PASSWORD:root123}\n    hikari:\n      # 库存占用事务期间会以 REQUIRES_NEW 独立小事务建行（额外借一个连接），连接数需留一倍余量\n      maximum-pool-size: 30",
  "old_string": "  datasource:\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/hotel?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false\n    username: ${MYSQL_USER:root}\n    password: ${MYSQL_PASSWORD:root123}",
  "path": "hotel-server/src/main/resources/application.yml"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/resources/application.yml`)</summary>

<!-- call_id: tool_9x0yyvvnbNhFLCbhz1Bj9Dla -->
Replaced 1 occurrence in hotel-server/src/main/resources/application.yml

</details>

#### Tool Call: Bash (`cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | gre…`)
<!-- call_id: tool_ES8pv4yUDGSWzxKB23hEqXMi -->
```json
{
  "command": "cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | grep -E \"Tests run:.*Failures|未知异常\" | head -3; echo \"--- run $i ---\"; done",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && for i in 1 2 3; do mvn -q test 2>&1 | gre…`)</summary>

<!-- call_id: tool_ES8pv4yUDGSWzxKB23hEqXMi -->
--- run 1 ---
--- run 2 ---
--- run 3 ---


</details>

<details><summary>Thinking</summary>

grep found nothing — meaning no "Tests run" lines in quiet output? With mvn -q, surefire summary may not print. Exit code? Let me run once without grep and check.

</details>

#### Tool Call: Bash (`cd hotel-server && mvn test 2>&1 | grep -E "Tests run|BUILD"…`)
<!-- call_id: tool_RVVd3B0PKjiK0rRdVoGQRYZO -->
```json
{
  "command": "cd hotel-server && mvn test 2>&1 | grep -E \"Tests run|BUILD\" | tail -5",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn test 2>&1 | grep -E "Tests run|BUILD"…`)</summary>

<!-- call_id: tool_RVVd3B0PKjiK0rRdVoGQRYZO -->
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.319 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS


</details>

<details><summary>Thinking</summary>

All 4 tests pass. Let me run the suite a few more times to be confident about the concurrency test stability (flakiness check), say 5 runs of just the concurrent test.

</details>

全部通过。再多跑几轮并发用例确认稳定性：

#### Tool Call: Bash (`cd hotel-server && for i in 1 2 3 4 5; do mvn test -Dtest=In…`)
<!-- call_id: tool_lLlOaUHReeJim2daSE15ELLl -->
```json
{
  "command": "cd hotel-server && for i in 1 2 3 4 5; do mvn test -Dtest=InventoryServiceTest#tryOccupy_concurrent_neverOversells 2>&1 | grep -E \"Tests run:.*-- in|BUILD\"; done",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && for i in 1 2 3 4 5; do mvn test -Dtest=In…`)</summary>

<!-- call_id: tool_lLlOaUHReeJim2daSE15ELLl -->
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.298 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.293 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.233 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.263 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.203 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS


</details>

<details><summary>Thinking</summary>

Stable across 5 runs. Also clean up: my earlier test had `unexpected` collector — that's fine to keep as a diagnostic. Also remove the now-unused import Isolation in InventoryService? I rewrote the file without Isolation import — check. My final Write didn't include Isolation import. Good.

Also verify no leftover: the test uses `java.util.concurrent.CopyOnWriteArrayList` fully qualified — fine.

Now verify DB has no leftover test rows (cleanup worked):

Also update the task list — T7 status? User hasn't accepted yet. Per workflow, report and wait. But user said "继续 T7" — after T6 commit. For T7 completion, I should report and wait for confirmation before T8 (they earlier said "不要进入下一个任务" for T6; for T7 they said continue T7 only). So stop at T7 completion report.

Should I commit T7? User said "提交 然后继续 T7" — the commit instruction was for T6. T7 commit should wait for user confirmation per AGENTS.md （每个阶段完成后提示用户提交 git). I'll report and提示.

Note the design deviation worth documenting in the task list （关键偏差 section) — but I'll mention it in report; can add to 清单 after acceptance. Actually the 清单 has a "实施过程中的关键偏差（新对话必读）" section — this deadlock finding is exactly that kind of knowledge. But wait for验收 before updating? The section is meant for continuity; adding the note now is useful. I'll wait until user confirms, to avoid churn. Hmm — but if conversation compacted, the deviation knowledge would be lost. I'll add it now — it's factual record, low risk.

Actually let me add the deviation note + T7 status update only after user confirms, consistent with previous pattern (I did the same for T6: updated doc only after user said 提交）. Fine.

Quick check DB cleanliness.

</details>

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "SEL…`)
<!-- call_id: tool_jvK7CZ1Uk02H6S2zUBiOaTUP -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 hotel -e \"SELECT * FROM daily_inventory;\" 2>/dev/null; echo \"rows: $(docker exec hotel-mysql mysql -uroot -proot123 hotel -N -e 'SELECT COUNT(*) FROM daily_inventory' 2>/dev/null)\""
}
```

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "SEL…`)</summary>

<!-- call_id: tool_jvK7CZ1Uk02H6S2zUBiOaTUP -->
rows: 0


</details>

<details><summary>Thinking</summary>

Clean. Report T7 results. Don't commit, don't start T8 (user's earlier pattern: they confirm then say 提交/继续）. Summarize implementation + the key design deviation (deadlock discovery) + test results.

</details>

T7 完成，测试全部通过。

## 实现内容（inventory 模块）

- `inventory/entity/DailyInventory.java`、`inventory/mapper/InventoryMapper.java` — 含原子条件更新 `occupyOne`/`releaseOne`、`countRooms`、`insertIgnore`
- `inventory/InventoryService.java` — 三个接口方法：
  - `queryAvailability(typeId, checkin, checkout)`：逐日剩余取最小值，无库存行按当前房间数计
  - `tryOccupy(...)`：按日期升序逐日原子占用，订满抛 1001"该房型所选日期已订满"，`@Transactional`（REQUIRED 传播，整单回滚由调用方事务保证）
  - `release(...)`：逐日 −1，带 `occupied_count > 0` 保护
- `inventory/InventoryRowCreator.java` — 建行专用组件（见下方关键偏差）
- 新增 `src/test/.../InventoryServiceTest.java` 集成测试 4 个用例

## 关键偏差（重要，与技术方案 §5.1 的实现差异）

按 §5.1 原始算法（同事务内"UPDATE → 0 → INSERT → 撞唯一索引重试 UPDATE"）实测**并发必现死锁**，10 线程仅 1~2 个成功。InnoDB 死锁日志证实两类成因：

1. RR 下对不存在的行做加锁 UPDATE 产生**间隙锁**，并发 INSERT 同间隙互相等待；
2. INSERT 撞唯一索引后 InnoDB 为重复键检查持有该记录 **S 锁直到事务结束**，并发事务再请求 X 锁形成 S→X 升级死锁（READ COMMITTED 下同样存在，已实测）。

**解法**：建行拆到 `InventoryRowCreator` 的独立小事务（`REQUIRES_NEW`，立即提交释放锁），占用事务只做"对已存在行的原子条件更新"（纯记录锁）。防超卖语义不变（仍由 `occupied_count < total_count` 条件更新 + 唯一索引保证），并发 5 轮实测零死锁。为此把 Hikari 连接池调到 30（占用事务期间建行需额外借一个连接）。

## 测试结果（`mvn test`，需 MySQL 已启动）

| 用例 | 对应 | 结果 |
| --- | --- | --- |
| `queryAvailability_noInventoryRow_countsAsCurrentRoomCount` | TC-B01：无库存行按房间数 3 计 | ✅ |
| `queryAvailability_dailyMinimum_andZeroWhenFull` | TC-B01/B02：逐日最小值、订满为 0、订满后再占抛 SOLD_OUT | ✅ |
| `release_returnsRoomToSellable` | 释放后剩余量恢复、占用数不为负 | ✅ |
| `tryOccupy_concurrent_neverOversells` | TC-G01 库存断言：10 线程并发占用同区间，**成功数恰为 3**、每日 `occupied=3 ≤ total`、失败方无残留 | ✅（连跑 5 轮稳定） |

测试后 `daily_inventory` 表无残留数据。任务清单 T7 状态和偏差记录等你验收后再更新；如需提交我再执行 `编码测试: inventory模块（防超卖）` 的提交。

## Turn 3

### User

在系统启动/房型房间变更时，提前把未来一段时间需要用到的库存记录创建好，而不是用户下单时发现库存行不存在再临时创建 这个会不会简化你这个并发控制

### Assistant

<details><summary>Thinking</summary>

The user asks: would pre-creating inventory rows ahead of time (at startup / when rooms change, for a future window) simplify the concurrency control, instead of lazy creation at booking time?

This is a design discussion question. I should answer honestly and technically — not just agree. Let me think carefully.

Current design pain point: lazy INSERT during tryOccupy creates two deadlock modes (gap locks on missed UPDATE in RR; S-lock from duplicate-key check held to commit + S→X upgrade deadlock). Solution was REQUIRES_NEW mini-tx for row creation.

Would pre-creation simplify?

Yes, substantially:
- If rows always exist for the bookable window, tryOccupy becomes pure conditional UPDATEs on existing rows → only record locks, no gap locks, no duplicate-key S locks. The InventoryRowCreator / REQUIRES_NEW machinery disappears. occupyOneDate becomes: `if (occupyOne != 1) throw SOLD_OUT`. The mapper's insertIgnore stays only for the pre-creation job (which is single-threaded/admin context, no concurrency).
- The concurrency reasoning becomes trivial: ascending date order + existing rows = record locks only = deadlock-free (same as current final design, but achieved by data invariant rather than transaction choreography).
- queryAvailability unchanged (rows exist anyway).
- The "无库存行按当前房间数计" fallback becomes a pure fallback, rarely exercised.
- The Hikari pool bump could arguably stay or revert — no more REQUIRES_NEW, so the extra connection borrow disappears; pool bump no longer needed (could revert to default). Actually keeping 30 is harmless, but could simplify back.

Costs / considerations:
1. Need a window decision: how far ahead? E.g., today + 90 days or 365 days. 4 room types × 365 days = 1460 rows — trivial.
2. Time marches on: rows must be extended as "today" advances. Startup-time creation handles restart; but a long-running demo app crossing midnight without restart would miss new days. Options: create a large enough window (e.g., 2 years: 4×730=2920 rows, still trivial) so it never matters in demo; or scheduled job (PRD says no scheduled tasks — the 已知简化 says no 定时任务 for auto-close; adding a scheduled inventory extension might conflict with "不做定时任务" spirit, though it's a different purpose). Realistically: create window today+2年 at startup and at room change → no cron needed for demo lifetime.
3. What about bookings beyond the window? Booking dates are user-chosen; someone could book 3 years out. With pre-creation + no lazy creation, tryOccupy UPDATE misses → 0 rows → we'd need... either still keep lazy creation as fallback (then complexity isn't fully removed) or reject bookings beyond window (new business rule not in PRD — 范围约束 says don't over-design; PRD has no booking-window limit). Hmm. Actually keeping the lazy ensureRow as a rare fallback costs little: the REQUIRES_NEW creator remains but is almost never exercised. But then is complexity really reduced? The code stays, but the hot path is pure UPDATE. The deadlock machinery becomes a cold fallback.
   Alternative: keep queryAvailability's "missing row = current room count" and keep the ensureRow fallback. I'd recommend: pre-create + keep fallback (defense in depth, and avoids adding a booking-window business rule that PRD doesn't have).
4. When to pre-create:
   - Startup: an ApplicationRunner that for each room type inserts missing rows for [today, today + N days] — INSERT IGNORE in one batch, idempotent. Single-threaded at startup (well, runner runs before web traffic? ApplicationRunner runs after context refresh, before the app accepts... actually Spring Boot starts accepting web requests after runners complete — runners run before the web server starts accepting? The web server is started before runners are called? Order: context refreshed → web server started → ApplicationRunners called → application "ready". Tomcat starts accepting connections... The server socket is bound during context refresh (WebServerStartStopLifecycle); runners execute in SpringApplication.run after refresh, before "started" event. There can be a brief window where the port is open but runners haven't finished. For demo, negligible. Or use @EventListener(ApplicationReadyEvent)? Same issue. It's fine — also requests at that instant would just hit the lazy fallback... which we're keeping.
   - Room change (UC-09, T9): room count changes already must sync `total_count` for future inventory rows per tech design §4.2 ("再把 stay_date ≥ 今天 的库存行 total_count 同步为新值"). With pre-creation, this sync becomes: batch upsert for the window (INSERT ... ON DUPLICATE KEY UPDATE total_count=new) — admin operation, low concurrency, safe. Actually this SIMPLIFIES T9 too: instead of "UPDATE existing future rows" you "ensure + update". Also new room type creation → pre-create its rows.
5. Data growth: 4 types × 730 days ≈ 2920 rows — nothing. §8 risk "逐日库存行随时间累积" already accepts accumulation.

Does it conflict with AGENTS.md / tech design? The tech doc §5.1 explicitly describes lazy creation on first booking. Changing to pre-creation is a design deviation requiring user confirmation — and the user is literally proposing it. Per 工作流程， this is 编码阶段； a deviation from the tech design. The user asked "会不会简化" — they're asking for assessment. I should answer: yes, it simplifies the hot path significantly, explain why (root cause was the INSERT, not the UPDATE), note what remains (fallback, window choice, startup hook, T9 sync), and ask if they want me to refactor T7 accordingly.

I should NOT just implement it without confirmation? The user is asking a question ("这个会不会简化你这个并发控制"). Answer the question with analysis, give recommendation, and offer to implement. Since they're the decision-maker and this changes the tech design, I should present the trade-offs and let them confirm before refactoring. Maybe use AskUserQuestion? A plain answer + offer is fine; could end with asking whether to adopt. Given the workflow requires confirmation for design changes, I'll answer and ask.

Let me structure:

1. 直接回答：会，而且简化的是"热路径"——死锁的两个根因都来自"下单事务里 INSERT"，行预存在后 tryOccupy 退化为纯条件更新。
2. 简化点列表：
   - occupyOneDate 变成一句 `if (occupyOne != 1) throw SOLD_OUT`
   - REQUIRES_NEW 小事务、exists 预检查可以移除（或退化为兜底）
   - 连接池不需要为建行留余量
   - T9 的 UC-09 库存同步也更顺：房间数变更 = 对未来窗口批量 upsert total_count，正好落在预创建机制上
3. 需要新增的机制/决策：
   - 窗口长度（建议今天起 2 年，4 房型×730 天≈3000 行，无感）
   - 启动时 ApplicationRunner 幂等补齐（INSERT IGNORE）
   - 新增房型/房间变更时同步创建
   - 窗口外日期怎么办：建议保留现有 ensureRow 懒建兜底（避免引入 PRD 没有的"预订窗口"业务规则）；兜底几乎不触发，热路径已无死锁面
   - 长时间运行跨天：窗口 2 年足够演示期，不需要定时任务（与 PRD"不做定时任务"一致）
4. 结论/建议：值得做。并发正确性仍由原子条件更新+唯一索引保证（面试口径不变），预创建只是把"行存在"变成数据不变式，把并发 choreography 换成运维式初始化。
5. 问是否按此重构 T7。

Keep it concise. Should I ask via AskUserQuestion or just plain text? Plain text with a clear question at the end is fine. Actually the decision is binary: adopt or not. I'll just ask in text.

</details>

会，而且这个简化正好打在死锁的根因上。前面排查的结论是两类死锁都来自"下单事务里的 INSERT"（对不存在行的加锁 UPDATE 产生间隙锁；INSERT 撞唯一索引留下持有到事务结束的 S 锁）。如果"行存在"成为数据不变式，`tryOccupy` 就退化为纯条件更新——只有记录锁，按日期升序执行，无死锁面。

**具体能简化掉的：**

- `occupyOneDate` 收敛成一句 `if (occupyOne(typeId, date) != 1) throw SOLD_OUT`
- `InventoryRowCreator`（REQUIRES_NEW 小事务）和 `exists` 预检查从热路径移除
- 连接池不必再为建行留一倍余量（application.yml 那处调整可回退）
- T9 也受益：UC-09 房间数变更时"同步未来库存行 total_count"（技术方案 §4.2 本来就要做）就变成对未来窗口批量 upsert，和预创建是同一套机制

**需要新增的机制和决策：**

| 事项 | 建议 |
| --- | --- |
| 预创建窗口 | 今天起 2 年。4 房型 × 730 天 ≈ 3000 行，无感；窗口够长则演示期内不需要定时任务滚动延期（与 PRD"不做定时任务"一致） |
| 创建时机 | ① 启动时 `ApplicationRunner` 按 `INSERT IGNORE` 幂等补齐；② T9 新增房型/房间数变更时同步创建 |
| 窗口外日期 | 保留现有懒建逻辑作兜底——下单超出窗口时仍能成交，避免引入 PRD 里没有的"预订窗口期"业务规则。兜底几乎不触发，热路径已无死锁 |

有一点要说清楚：这不是把并发控制"简化掉"，而是把并发控制从"事务编排"换成"初始化保证"。防超卖的正确性仍然只靠 `occupied_count < total_count` 原子条件更新 + 唯一索引（面试口径不变），预创建只是消除了 INSERT 这个不稳定因素。同时这也是对技术方案 §5.1"首次预订该日时建行"的一处设计修订。

要按这个方案重构 T7 吗？改动：`InventoryService.occupyOneDate` 简化、新增启动时预创建的 runner、`mvn test` 回归。

## Turn 4

### User

按照这个建议来修改技术方案、技术决策和已有的代码

### Assistant

<details><summary>Thinking</summary>

The user approves: refactor per the proposal — update 技术方案 (docs/02), 技术决策 (the decisions are in 技术方案 §1.1/§1.2 — "技术决策" likely means the decision tables in that doc), and existing code.

Changes:

1. **docs/02-技术方案.md**:
   - §5.1 防超卖： change lazy row creation to pre-creation + conditional update. Update the tryOccupy steps: rows pre-created at startup/room-change; occupy = pure conditional UPDATE; missing row (beyond window) → fallback lazy creation (independent small tx? or keep the creator). Update §4.2 关键设计说明 (库存与房间数一致性 — pre-creation mechanism). Maybe add a decision row to §1.2 (补充确认 #13?) — "库存行预创建" as a new decision. Also §8 risks mention 逐日库存行累积 — pre-creation adds fixed ~3000 rows; update that row? It says "每房型每日一行" — now pre-created 2 years. Could update.
   - v1.1 → v1.2 with revision record.

2. **Code**:
   - `InventoryService.occupyOneDate`: simplify to `if (occupyOne != 1) { ensureRow fallback? }`.
   
   Wait — decision: keep lazy fallback for beyond-window bookings. The fallback needs the REQUIRES_NEW creator (since even rare, doing INSERT in main tx has deadlock risk — but beyond-window concurrent booking race is super rare; still, keep the safe mechanism). So keep InventoryRowCreator, used by both the pre-creator (startup runner? runner can just use mapper insertIgnore directly in batches) and the fallback.
   
   Hot path: `if (occupyOne(typeId, date) == 1) return; rowCreator.ensureRow(typeId, date); if (occupyOne != 1) throw SOLD_OUT;` — that's almost what we have but without the exists SELECT. Simpler: try UPDATE first; if 0 (missing row or full), ensureRow (idempotent, cheap-ish), retry UPDATE once; 0 → SOLD_OUT. The gap-lock concern with UPDATE on missing row: with pre-creation, missing rows are rare (beyond window). In RR, missed UPDATE takes gap locks; then ensureRow REQUIRES_NEW INSERT waits on outer's own gap locks → self lock-wait timeout! Hmm. That was the earlier analysis. So for the fallback path with RR, the missed UPDATE's gap locks conflict with our own mini-tx INSERT.

   Options: (a) keep the exists SELECT before UPDATE (consistent read, no locks) → if missing, ensureRow first, then UPDATE — avoids missed-UPDATE gap locks entirely. (b) make tryOccupy RC. 

   With pre-creation, the hot path row exists → SELECT says exists → UPDATE record lock. The exists SELECT is one extra consistent read per date — cheap. Keep the current structure (exists → maybe ensureRow → UPDATE once). Actually current code: exists? → ensureRow if missing → single UPDATE → SOLD_OUT if 0. That's already minimal and isolation-agnostic. So InventoryService barely changes! Just the comment. Hmm.

   So code changes:
   - New `InventoryPreCreator` (or put in InventoryService?) — an ApplicationRunner: for each room type, INSERT IGNORE rows for [today, today+2年]. Needs room type list — inventory module can't call room module (dependency rules!). It can query room_type ids via its own mapper SQL (like countRooms reads room table). Add `@Select("SELECT id FROM room_type")` or batch insert directly in SQL:
     ```sql
     INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)
     SELECT rt.id, DATE_ADD(CURDATE(), INTERVAL seq.n DAY), (SELECT COUNT(*) FROM room r WHERE r.room_type_id = rt.id), 0
     FROM room_type rt CROSS JOIN (seq of 0..729)
     ```
     Generating a date sequence in MySQL 8: recursive CTE or a numbers trick. With a recursive CTE:
     ```sql
     INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)
     WITH RECURSIVE dates AS (
       SELECT CURDATE() AS d
       UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < CURDATE() + INTERVAL 729 DAY
     )
     SELECT rt.id, d.d, (SELECT COUNT(*) FROM room WHERE room_type_id = rt.id), 0
     FROM room_type rt CROSS JOIN dates d
     ```
     Wait, INSERT ... WITH ... SELECT — syntax: `INSERT IGNORE INTO ... WITH RECURSIVE ... SELECT ...`? Actually MySQL syntax: `INSERT INTO t WITH RECURSIVE cte AS (...) SELECT ...`? The WITH comes before INSERT in MySQL 8? In MySQL, `WITH ... INSERT` isn't supported; but `INSERT INTO t WITH cte AS (...) SELECT ...` — MySQL supports WITH after INSERT INTO tbl? MySQL docs: WITH can be used at the beginning of SELECT, and "INSERT ... WITH ... SELECT" is supported: `INSERT INTO t WITH cte AS (...) SELECT ...`? Hmm. MySQL 8.0.19+: "WITH ... SELECT" in INSERT: `INSERT INTO tbl_name WITH cte ... SELECT ...`. Yes, MySQL supports `INSERT INTO t WITH RECURSIVE ... SELECT ...`? Actually MySQL documentation states WITH clause is permitted at the beginning of SELECT statements, and for INSERT ... SELECT, the WITH can precede the SELECT: `INSERT INTO t WITH cte AS (...) SELECT * FROM cte;` — I believe that works in MySQL 8.0. Alternatively Java-side loop: for each type, for each of 730 days → 2920 individual INSERTs at startup — takes a few seconds, meh. Better: batch via Java building a multi-row INSERT? 730 days × N types — could do per-type recursive CTE. Or simpler Java approach: single mapper method `preCreate(typeId, startDate, days)` using recursive CTE per type:
     ```sql
     INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)
     WITH RECURSIVE dates(d) AS (
       SELECT #{startDate}
       UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < #{startDate} + INTERVAL #{days} DAY
     )
     SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates
     ```
     Need to verify syntax works on MySQL 8. I can test directly with docker exec.
     
     Also cte_max_recursion_depth default 1000 → 730 iterations OK (rows = 731? recursion depth counts iterations; default 1000 fine).

   - Runner: `InventoryPreCreator implements ApplicationRunner` in inventory package, @Component, method @Transactional? Batch per type: for each room_type id (via new mapper selectRoomTypeIds), call preCreate per type in one tx or autocommit — fine either way; idempotent. Also expose a public method `preCreateWindow()` so T9 room maintenance can call it? T9: room module can't call inventory (only order → inventory). Hmm! UC-09 room maintenance is in room module; syncing future inventory rows total_count is required by §4.2 — how does room module update inventory?? Dependency rules say only order → inventory. But T9's assignable-rooms/check-in are order admin... 房间维护 is room module's admin controller. Hmm, the tech doc says room module hides "房间数变更同步未来库存行总数" (§3.4 room row: "维护时...房间数变更同步未来库存行总数"). So room module itself touches daily_inventory — via its own SQL presumably (like inventory reads room table). That's a T9 problem; for now the startup runner suffices. I'll note in the doc that room maintenance will call the same pre-create/upsert SQL (room module will have its own inventory-sync mapper method in T9). Or... to keep it available, maybe put pre-create as a public method on InventoryService so order-side can use; room module T9 can do its own SQL. For T7 scope: startup runner only.

   - Revert Hikari pool bump? The REQUIRES_NEW fallback still exists for beyond-window bookings — rare, but still borrows a second connection. Pool 30 vs default 10: beyond-window bookings are rare, 10 would suffice realistically, but 30 is harmless and documented. I'd keep 30 with updated comment, or revert to keep changes minimal? The comment says it's for REQUIRES_NEW ensureRow — still true (fallback). Keep as is (harmless headroom). Hmm, actually with fallback being near-never, default 10 is fine. But keeping 30 costs nothing and protects the fallback under concurrency. Keep.

   - Update comments in InventoryService: pre-creation invariant + fallback.

   - Tests: add a test for the pre-creator? E.g., after startup (SpringBootTest context runs runners? ApplicationRunner runs in SpringBootTest — yes, runners execute when context starts in @SpringBootTest). So in tests, rows for [today, today+730) already exist for all 4 types! Then my existing tests use dates 2099-01-10 — outside window (today 2026-10-04 + 730 days ≈ 2028-10) → fallback path still exercised. Good — tests still cover both paths. Could add an assertion test: queryAvailability for a date within window works and row exists; and a test that pre-creator is idempotent (run twice, row count stable). Add `InventoryPreCreatorTest`:
     - rows exist for window: select count for type 1, stay_date = today → 1 row, total = 3.
     - idempotent: run preCreateWindow() again → no duplicate (count unchanged).
     
   - The startup runner in real app: runs before... fine.

3. **AGENTS.md?** The 编码规范 don't change. The 清单's 关键偏差 section should get the new deviation entry — but per workflow, update 清单 at acceptance/commit time. I'll update it now since user asked to modify docs; the 清单 update (T7 status) still waits for验收？ The user said "按照这个建议来修改技术方案、技术决策和已有的代码" — 清单 isn't 技术方案/决策/代码. I'll leave 清单 for the commit step, but add the deviation note maybe. Keep pattern: update 清单 when committing after acceptance. 

4. Also update docs/02 version to v1.2 with 修订记录， add decision #13 in §1.2 table, rewrite §5.1 relevant paragraphs, update §4.2 库存与房间数一致性 bullet, maybe §8 risk row about 逐日库存行累积 (now pre-created ~730 days). Also the API/DB sections don't change. §3.4 inventory row "接口背后隐藏的复杂度" mentions "逐日库存行的创建与并发原子占用" — update to mention 预创建. Let me draft doc edits:

§1.2 add row: `| 13 | 库存行创建时机 | 启动/房型房间变更时预创建未来 2 年库存行（INSERT IGNORE 幂等）；窗口外日期下单时懒建兜底 |`

§4.2 bullet "库存与房间数的一致性": update to mention预创建 + UC-09 同步 = upsert未来窗口行.

§5.1 rewrite steps:
```
预创建（决策 13）：应用启动时与房型/房间变更时，按 INSERT IGNORE 批量创建 [今天, 今天+730 天) 的逐日库存行（total_count 取当前房间数），保证下单热路径上库存行总是存在。

创建订单先由 service 层做业务规则校验（BR-01 ...）不变。

校验通过后在一个事务内，对 [checkin, checkout) 每日按日期升序：
1. 原子条件更新 UPDATE ... occupied < total
2. 影响行数 = 1 → 成功；= 0 → 说明超出预创建窗口（或并发下窗口刚建），经独立小事务懒建该行后重试一次（见下"为什么懒建要在独立小事务"）
3. 仍 0 → 已订满，抛业务异常整单回滚。
```
And explain: 建行之所以不在下单事务内做：REPEATABLE READ 下加锁 UPDATE 未命中产生间隙锁、INSERT 撞唯一索引的 S 锁持有到事务结束，均导致并发死锁（开发中实测复现）；行预存在后占用路径只有记录锁。

§8 risk row 逐日库存行累积： update — 每房型每日一行，预创建 2 年约 4×730 行，演示规模无压力.

§3.4 inventory row: "逐日库存行的创建与并发原子占用" → "逐日库存行的预创建/懒建与并发原子占用、剩余量取逐日最小值（BR-02）、取消/退房释放" — minor.

Also header 修订记录 v1.2.

5. Test the recursive CTE syntax first via docker exec.

Implementation steps:
1. Test CTE SQL in MySQL.
2. Add mapper methods: `selectRoomTypeIds`, `preCreate(typeId, startDate, endDate/days, totalRooms)`.
3. Add `InventoryPreCreator` (ApplicationRunner + public preCreateWindow()).
4. Simplify/adjust InventoryService comments (logic mostly same: exists → ensureRow → update). Hmm — actually should hot path still do the exists SELECT? With pre-creation, rows always exist in-window. Keep as is (works for both). Minimal change. But wait — actually now I could simplify occupyOneDate to not SELECT first: just UPDATE; if 0 → ensureRow → UPDATE → SOLD_OUT. The missed-UPDATE gap-lock self-conflict with ensureRow: outer RR tx missed UPDATE → gap locks held; ensureRow REQUIRES_NEW INSERT into that gap → insert intention blocked by outer's own gap locks → lock wait timeout (50s) → exception. That would make beyond-window bookings fail horribly. So keep exists-first. Keep current code, update comments.

5. Update InventoryRowCreator comment (fallback role + used for pre-create? Pre-creator could reuse insertIgnore mapper method in batch form — separate mapper method preCreateWindow per type. The single-row insertIgnore remains for ensureRow).

6. Add test: InventoryPreCreatorTest (window rows exist, idempotent).
7. mvn test full suite (multiple runs).
8. Update docs/02.
9. Report; await confirmation for commit.

Note: in @SpringBootTest, ApplicationRunner beans are executed? Spring Boot test runs SpringApplication — yes, ApplicationRunners are called during context startup in @SpringBootTest (it bootstraps the full application). Good.

The runner signature: `public void run(ApplicationArguments args)`. Also expose `preCreateWindow()` public for reuse/testing.

Window: today → today+730 days. Define PRE_CREATE_DAYS = 730.

preCreate mapper SQL per type:

```java
@Insert("INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) "
      + "WITH RECURSIVE dates(d) AS ("
      + "  SELECT #{startDate} "
      + "  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < #{endDate}"
      + ") "
      + "SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates")
int preCreate(@Param("typeId") Long typeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("totalRooms") int totalRooms);
```

Window: [today, today + 730 days] → 731 rows per type. endDate = today.plusDays(730) inclusive via `d < endDate`... if startDate=today and condition d < today+730, rows today..today+729 = 730 rows. Fine: [今天, 今天+730天).

Syntax check: `INSERT IGNORE INTO t WITH RECURSIVE ... SELECT ...` — hmm, is WITH allowed after INSERT INTO tbl? MySQL 8.0 reference: "INSERT ... SELECT" and CTE: examples show `INSERT INTO t1 WITH cte AS (...) SELECT ...`? Hmm, actually I recall MySQL requires `INSERT INTO tbl WITH ... SELECT` is NOT valid; the correct form is `INSERT INTO tbl SELECT ... FROM ...` where WITH precedes SELECT: `INSERT INTO tbl WITH cte AS (...) SELECT ...`... conflicting memories. MySQL docs (8.0): "A WITH clause is permitted in these contexts: At the beginning of SELECT ... ; At the beginning of subqueries...; Immediately preceding SELECT for statements that include a SELECT: INSERT ... WITH ... SELECT ..., REPLACE ... WITH ... SELECT". YES — MySQL explicitly supports `INSERT ... WITH ... SELECT`. So `INSERT IGNORE INTO t WITH RECURSIVE cte AS (...) SELECT ...` should be valid. Verify empirically with docker exec before writing Java.

Careful with parameter interpolation in recursive term: `SELECT #{startDate}` — LocalDate binding as DATE fine.

Also selectRoomTypeIds:
```java
@Select("SELECT id FROM room_type")
List<Long> selectRoomTypeIds();
```
(inventory reading room_type id — consistent with countRooms precedent.)

InventoryPreCreator:
```java
@Component
public class InventoryPreCreator implements ApplicationRunner {
    static final int PRE_CREATE_DAYS = 730;
    private final InventoryMapper inventoryMapper;
    
    @Override
    public void run(ApplicationArguments args) {
        preCreateWindow();
    }

    /** 为全部房型预创建 [今天, 今天+PRE_CREATE_DAYS) 的库存行（INSERT IGNORE 幂等） */
    public void preCreateWindow() {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(PRE_CREATE_DAYS);
        for (Long typeId : inventoryMapper.selectRoomTypeIds()) {
            inventoryMapper.preCreate(typeId, today, end, inventoryMapper.countRooms(typeId));
        }
    }
}
```

Should preCreateWindow be @Transactional? Each preCreate is one statement, autocommit fine.

Comment in InventoryService occupyOneDate update: mention预创建使库存行在窗口内恒存在，此处 SELECT/ensureRow 是窗口外日期的懒建兜底。

Doc updates and code, then run tests (full suite several times). Also the existing tests use 2099 dates → fallback path. Add a test asserting window row exists for today (pre-created with total=3) and queryAvailability within window; idempotency: run preCreateWindow() again, count rows for type 1 in window stable.

Careful: @SpringBootTest runs the runner → creates ~2920 rows in the dev DB. That's the same DB the demo uses — fine, intended.

Also note: the tests' cleanup deletes rows only in their 2099 range; window rows persist. TC-G01-style test unaffected.

Let me start: test SQL syntax.

</details>

好，按此方案实施。先验证 MySQL 8 的递归 CTE 批量建行语法可行：

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
IN…`)
<!-- call_id: tool_VBNmvOKj0VIDWlu8i8wqGwZT -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 hotel -e \"\nINSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count)\nWITH RECURSIVE dates(d) AS (\n  SELECT DATE '2026-10-04'\n  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < DATE '2026-10-04' + INTERVAL 729 DAY\n)\nSELECT 99, d, 3, 0 FROM dates;\nSELECT COUNT(*) AS rows_created, MIN(stay_date), MAX(stay_date) FROM daily_inventory WHERE room_type_id = 99;\nDELETE FROM daily_inventory WHERE room_type_id = 99;\nSELECT COUNT(*) AS after_cleanup FROM daily_inventory WHERE room_type_id = 99;\" 2>&1 | grep -v Warning"
}
```

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
IN…`)</summary>

<!-- call_id: tool_VBNmvOKj0VIDWlu8i8wqGwZT -->
rows_created	MIN(stay_date)	MAX(stay_date)
730	2026-10-04	2028-10-02
after_cleanup
0


</details>

<details><summary>Thinking</summary>

CTE batch insert works. Now implement:

1. Mapper: add `selectRoomTypeIds()` and `preCreate(...)`.
2. `InventoryPreCreator` (ApplicationRunner).
3. Update InventoryService comments (logic unchanged — exists-first + ensureRow fallback).
4. Update InventoryRowCreator comment (fallback role).
5. Add InventoryPreCreatorTest.
6. Update docs/02 (v1.2: decision 13, §3.4, §4.2, §5.1, §8).
7. Run tests.

</details>

语法可行（一次插入 730 行）。现在改代码：

#### Tool Call: Read (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_Z24oXoxpJbjU8ZFUTydO68uK -->
```json
{
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

<details><summary>Tool Result: Read (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_Z24oXoxpJbjU8ZFUTydO68uK -->
1	package com.example.hotel.inventory.mapper;
2	
3	import com.baomidou.mybatisplus.core.mapper.BaseMapper;
4	import com.example.hotel.inventory.entity.DailyInventory;
5	import org.apache.ibatis.annotations.Insert;
6	import org.apache.ibatis.annotations.Param;
7	import org.apache.ibatis.annotations.Select;
8	import org.apache.ibatis.annotations.Update;
9	
10	import java.time.LocalDate;
11	
12	public interface InventoryMapper extends BaseMapper<DailyInventory> {
13	
14	    @Update("UPDATE daily_inventory SET occupied_count = occupied_count + 1 "
15	            + "WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count < total_count")
16	    int occupyOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);
17	
18	    @Update("UPDATE daily_inventory SET occupied_count = occupied_count - 1 "
19	            + "WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count > 0")
20	    int releaseOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);
21	
22	    @Select("SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}")
23	    int countRooms(@Param("typeId") Long typeId);
24	
25	    @Insert("INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) "
26	            + "VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)")
27	    int insertIgnore(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate,
28	                     @Param("totalRooms") int totalRooms);
29	}

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_UZla9ZfOB868801XVdxR1HO1 -->
```json
{
  "new_string": "    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)\")\n    int insertIgnore(@Param(\"typeId\") Long typeId, @Param(\"stayDate\") LocalDate stayDate,\n                     @Param(\"totalRooms\") int totalRooms);\n\n    @Select(\"SELECT id FROM room_type\")\n    java.util.List<Long> selectRoomTypeIds();\n\n    /** 批量创建某房型 [startDate, endDate) 的库存行（已存在则忽略），total_count 取当前房间数 */\n    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"WITH RECURSIVE dates(d) AS (\"\n            + \"  SELECT #{startDate} \"\n            + \"  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < #{endDate}\"\n            + \") \"\n            + \"SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates\")\n    int preCreate(@Param(\"typeId\") Long typeId, @Param(\"startDate\") LocalDate startDate,\n                  @Param(\"endDate\") LocalDate endDate, @Param(\"totalRooms\") int totalRooms);\n}",
  "old_string": "    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)\")\n    int insertIgnore(@Param(\"typeId\") Long typeId, @Param(\"stayDate\") LocalDate stayDate,\n                     @Param(\"totalRooms\") int totalRooms);\n}",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_UZla9ZfOB868801XVdxR1HO1 -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java

</details>

#### Tool Call: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_b2O42qkMGmrjYgK23zW6ch9j -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.springframework.boot.ApplicationArguments;\nimport org.springframework.boot.ApplicationRunner;\nimport org.springframework.stereotype.Component;\n\nimport java.time.LocalDate;\n\n/**\n * 库存行预创建（决策 13）：应用启动时为全部房型批量创建未来窗口的逐日库存行。\n * 预创建保证下单热路径上库存行总是存在，占用只做\"对已存在行的原子条件更新\"（纯记录锁），\n * 从而避免下单事务内 INSERT 引发的并发死锁（间隙锁 / 撞唯一索引的 S 锁升级）。\n * 窗口长度覆盖演示期，无需定时任务滚动延期；窗口外日期由下单时懒建兜底（InventoryRowCreator）。\n */\n@Component\npublic class InventoryPreCreator implements ApplicationRunner {\n\n    static final int PRE_CREATE_DAYS = 730;\n\n    private final InventoryMapper inventoryMapper;\n\n    public InventoryPreCreator(InventoryMapper inventoryMapper) {\n        this.inventoryMapper = inventoryMapper;\n    }\n\n    @Override\n    public void run(ApplicationArguments args) {\n        preCreateWindow();\n    }\n\n    /** 为全部房型预创建 [今天, 今天 + PRE_CREATE_DAYS) 的库存行，INSERT IGNORE 幂等可重复执行 */\n    public void preCreateWindow() {\n        LocalDate start = LocalDate.now();\n        LocalDate end = start.plusDays(PRE_CREATE_DAYS);\n        for (Long typeId : inventoryMapper.selectRoomTypeIds()) {\n            inventoryMapper.preCreate(typeId, start, end, inventoryMapper.countRooms(typeId));\n        }\n    }\n}\n",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryPreCreator.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_b2O42qkMGmrjYgK23zW6ch9j -->
Wrote 1609 bytes to hotel-server/src/main/java/com/example/hotel/inventory/InventoryPreCreator.java

</details>

<details><summary>Thinking</summary>

Update InventoryService comments to reflect the pre-creation design (hot path rows always exist; exists-check + ensureRow is beyond-window fallback). Read current file section and edit occupyOneDate comment and tryOccupy javadoc.

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_pYmW8oJE51gITTgMT50my0YF -->
```json
{
  "new_string": "    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     * 预创建窗口内库存行恒存在（决策 13），本事务只做\"对已存在行的原子条件更新\"（纯记录锁）；\n     * 窗口外日期走懒建兜底（独立小事务建行），避免下单事务内 INSERT 引发的并发死锁。\n     */\n    @Transactional\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {",
  "old_string": "    /**\n     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。\n     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。\n     * 并发安全：本事务只做\"对已存在行的原子条件更新\"（仅产生记录锁）；\n     * 建行由 InventoryRowCreator 在独立小事务中完成，避免撞唯一索引的 S 锁与间隙锁引发死锁。\n     */\n    @Transactional\n    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_pYmW8oJE51gITTgMT50my0YF -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_YMkfFEhQG42X9vnZzTCjWZDl -->
```json
{
  "new_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 一致性读判断行是否存在（不加锁，避免对不存在行的加锁 UPDATE 产生间隙锁）；\n        // 预创建窗口内恒为存在，未命中即窗口外日期\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (!exists) {\n            // 超出预创建窗口：懒建兜底，独立小事务建行后再走条件更新\n            rowCreator.ensureRow(typeId, date);\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "old_string": "    private void occupyOneDate(Long typeId, LocalDate date) {\n        // 一致性读判断行是否存在（不加锁，避免对不存在行的加锁 UPDATE 产生间隙锁）\n        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", typeId)\n                .eq(\"stay_date\", date)) > 0;\n        if (!exists) {\n            // 首次预订该日：独立小事务建行（撞唯一索引自动忽略），再回到条件更新\n            rowCreator.ensureRow(typeId, date);\n        }\n        if (inventoryMapper.occupyOne(typeId, date) != 1) {\n            throw new BizException(ErrorCode.SOLD_OUT);\n        }\n    }",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_YMkfFEhQG42X9vnZzTCjWZDl -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java

</details>

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_GdDbymgXp3JHeH1csb4KLQwA -->
```json
{
  "new_string": "/**\n * 懒建兜底：预创建窗口（InventoryPreCreator）之外日期的库存行创建器。\n * 建行必须在独立小事务中完成并立即提交：\n * 若在调用方事务内 INSERT 撞唯一索引，InnoDB 会为重复键检查持有该记录的 S 锁直到事务结束，\n * 随后并发事务再请求 X 锁（条件更新）会形成 S→X 升级死锁。\n */",
  "old_string": "/**\n * 逐日库存行的创建器。建行必须在独立小事务中完成并立即提交：\n * 若在调用方事务内 INSERT 撞唯一索引，InnoDB 会为重复键检查持有该记录的 S 锁直到事务结束，\n * 随后并发事务再请求 X 锁（条件更新）会形成 S→X 升级死锁。\n */",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_GdDbymgXp3JHeH1csb4KLQwA -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java

</details>

<details><summary>Thinking</summary>

Now add a test for the pre-creator. Note: @SpringBootTest runs ApplicationRunner, so window rows exist in test context. Test:
- window rows exist for type 1 at today with total=3, occupied=0
- idempotent: call preCreateWindow() again, row count in window unchanged
- occupy within window works via hot path (tryOccupy for tomorrow) and availability decreases — but careful: tests run against shared dev DB; occupying tomorrow's row for type 1... cleanup afterwards (release + reset occupied). Cleanup: release then it returns to 0. But delete? No — window rows should persist (they're pre-created). Use tryOccupy then release to restore occupied=0. But if assertion fails mid-way, residue. Use try/finally. Also other tests use 2099 dates (fallback path) — good coverage of both paths.

Write InventoryPreCreatorTest.

</details>

新增预创建的测试：

#### Tool Call: Write (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)
<!-- call_id: tool_LhN18DzlZUEl71FXY0nQntgg -->
```json
{
  "content": "package com.example.hotel.inventory;\n\nimport com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;\nimport com.example.hotel.inventory.entity.DailyInventory;\nimport com.example.hotel.inventory.mapper.InventoryMapper;\nimport org.junit.jupiter.api.Test;\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.boot.test.context.SpringBootTest;\n\nimport java.time.LocalDate;\n\nimport static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.jupiter.api.Assertions.assertNotNull;\nimport static org.junit.jupiter.api.Assertions.assertTrue;\n\n/**\n * 库存行预创建（决策 13）：启动时窗口内行已存在、幂等、窗口内占用走热路径。\n * SpringBootTest 启动时会执行 ApplicationRunner，即预创建已生效。\n */\n@SpringBootTest\nclass InventoryPreCreatorTest {\n\n    private static final Long TYPE_ID = 1L; // 大床房 3 间\n    private static final int TOTAL_ROOMS = 3;\n\n    @Autowired\n    private InventoryPreCreator preCreator;\n    @Autowired\n    private InventoryService inventoryService;\n    @Autowired\n    private InventoryMapper inventoryMapper;\n\n    @Test\n    void windowRowsExistAfterStartup() {\n        LocalDate today = LocalDate.now();\n        DailyInventory row = inventoryMapper.selectOne(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", TYPE_ID)\n                .eq(\"stay_date\", today));\n        assertNotNull(row);\n        assertEquals(TOTAL_ROOMS, row.getTotalCount());\n        // 窗口末端（今天 + 730 天 - 1）也已创建\n        Long count = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", TYPE_ID)\n                .ge(\"stay_date\", today)\n                .lt(\"stay_date\", today.plusDays(InventoryPreCreator.PRE_CREATE_DAYS)));\n        assertEquals(InventoryPreCreator.PRE_CREATE_DAYS, count);\n    }\n\n    @Test\n    void preCreate_isIdempotent() {\n        preCreator.preCreateWindow();\n        preCreator.preCreateWindow();\n        Long count = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()\n                .eq(\"room_type_id\", TYPE_ID)\n                .ge(\"stay_date\", LocalDate.now()));\n        assertEquals(InventoryPreCreator.PRE_CREATE_DAYS, count);\n    }\n\n    @Test\n    void occupyWithinWindow_thenRelease() {\n        // 窗口内日期：占用走\"已存在行的条件更新\"热路径，释放后恢复\n        LocalDate checkin = LocalDate.now().plusDays(30);\n        LocalDate checkout = checkin.plusDays(2);\n        int before = inventoryService.queryAvailability(TYPE_ID, checkin, checkout);\n        assertTrue(before <= TOTAL_ROOMS);\n        try {\n            inventoryService.tryOccupy(TYPE_ID, checkin, checkout);\n            assertEquals(before - 1, inventoryService.queryAvailability(TYPE_ID, checkin, checkout));\n        } finally {\n            inventoryService.release(TYPE_ID, checkin, checkout);\n        }\n        assertEquals(before, inventoryService.queryAvailability(TYPE_ID, checkin, checkout));\n    }\n}\n",
  "path": "hotel-server/src/test/java/com/example/hotel/inventory/InventoryPreCreatorTest.java"
}
```

<details><summary>Tool Result: Write (`hotel-server/src/test/java/com/example/hotel/inventory/Inven…`)</summary>

<!-- call_id: tool_LhN18DzlZUEl71FXY0nQntgg -->
Wrote 3028 bytes to hotel-server/src/test/java/com/example/hotel/inventory/InventoryPreCreatorTest.java

</details>

#### Tool Call: Bash (`cd hotel-server && mvn test 2>&1 | grep -E "Tests run|BUILD"…`)
<!-- call_id: tool_2msPfssYFwvLn8y6CjwAcAy9 -->
```json
{
  "command": "cd hotel-server && mvn test 2>&1 | grep -E \"Tests run|BUILD\" | tail -5",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && mvn test 2>&1 | grep -E "Tests run|BUILD"…`)</summary>

<!-- call_id: tool_2msPfssYFwvLn8y6CjwAcAy9 -->
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.387 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryPreCreatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.079 s -- in com.example.hotel.inventory.InventoryServiceTest
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE


</details>

#### Tool Call: Bash (`cd hotel-server && grep -A5 "FAILURE!" target/surefire-repor…`)
<!-- call_id: tool_YSu1DQAEqnKBb8zozQreN2ij -->
```json
{
  "command": "cd hotel-server && grep -A5 \"FAILURE!\" target/surefire-reports/com.example.hotel.inventory.InventoryPreCreatorTest.txt | head -8"
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && grep -A5 "FAILURE!" target/surefire-repor…`)</summary>

<!-- call_id: tool_YSu1DQAEqnKBb8zozQreN2ij -->
Tests run: 3, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.387 s <<< FAILURE! -- in com.example.hotel.inventory.InventoryPreCreatorTest
com.example.hotel.inventory.InventoryPreCreatorTest.preCreate_isIdempotent -- Time elapsed: 0.391 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <730> but was: <731>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertEquals.failNotEqual(AssertEquals.java:197)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:182)


</details>

<details><summary>Thinking</summary>

731 vs 730: the runner ran at startup with `start = today`... but wait, my idempotent test counts rows >= today. Startup created [today, today+730). Then preCreateWindow() again same range → no new rows. Why 731?

Hmm — windowRowsExistAfterStartup counted ge today AND lt today+730 == 730 ✓ (that passed). The idempotent test counts >= today with no upper bound → includes 730 window rows + ... one extra row from another test? Test order: InventoryServiceTest also runs — uses 2099 dates and cleans up in @AfterEach. But test execution order — InventoryServiceTest ran AFTER (0.079s elapsed, listed second). Actually surefire report shows PreCreator failed with 731 — maybe a leftover row from a previous manual test run? Earlier I ran tests before pre-creation existed — those cleaned up. Hmm, what about `occupyWithinWindow_thenRelease` — releases properly. What other row >= today for type 1? 

Wait — InventoryRowCreator fallback rows: none today.

Could the startup runner have run with start=today, and then... 730 rows [today, today+730). Count >= today should be 730. The 731st row: maybe from an earlier failed run of this same test? No, rows only from runner/mapper — preCreate is INSERT IGNORE, idempotent.

Hmm — maybe the runner ran TWICE during context startup? No...

Wait — time zone/clock: LocalDate.now() in app vs CURDATE()... I parameterized dates, no CURDATE. 

Maybe the 731st row comes from `windowRowsExistAfterStartup`? It doesn't insert. `occupyWithinWindow_thenRelease` occupies and releases — no inserts (rows exist).

What about a leftover row at today+730 or beyond from... the FIRST test run of this class earlier (the failed mvn test I just ran counts as run 1; but failure occurred in that first run already).

Hmm, maybe leftover from my earlier CTE syntax test? I used room_type_id=99 and deleted. No.

What about the very first manual test with order HR-T6TEST? Deleted.

OH WAIT. The startup runner runs for the test context — but the runner also ran when... hmm. Actually: is it possible the app context started BEFORE midnight vs test... no, same minute.

Let me think again: `preCreate_isIdempotent` counts rows with stay_date >= LocalDate.now() for type 1. The startup runner created rows [today, today+730): that's today .. today+729 inclusive = 730 rows. Plus — did a previous partial run of THIS test class create a row at today+730? preCreate uses end = start.plusDays(730), condition `d < endDate` → max = today+729. No.

Unless... the first `mvn test` (failed run) had startup runner create 730 rows; but ALSO, earlier today, I started the actual app (`mvn spring-boot:run` for T6) — before pre-creation existed. No rows.

Hmm wait, actually — what about the `InventoryServiceTest` cleanup: it deletes rows in [2099-01-10, 2099-01-12) — those rows are > today! If a PREVIOUS test run (before I added PreCreator) left residue... no, cleanup handled them; and DB check earlier showed 0 rows.

But NOW: InventoryServiceTest's tryOccupy on 2099 dates creates fallback rows (2099-01-10, 11) — cleanup deletes them in @AfterEach. Unless test ordering: JUnit runs classes; surefire shows InventoryServiceTest elapsed 0.079s — wait that's suspiciously fast. "Time elapsed: 0.079 s" for 4 tests including a 10-thread concurrency test that previously took 1.3s? And PreCreatorTest ran FIRST (1.387s). Order: PreCreatorTest ran first, then ServiceTest. The 0.079s is probably per-class after context cached... no, time is class total. Weird. Whatever.

So in PreCreatorTest run, 731 rows >= today for type 1 existed. One extra: where? OH! I bet it's `occupyWithinWindow` from... no.

Hmm — maybe surefire ran the tests in same JVM but the startup runner ALSO runs for every @SpringBootTest class context (same cached context, runner runs once). 

Actually wait — maybe the app itself is running?? No, I stopped it.

Let me check: docker DB query — count rows for type 1 >= today, and find the extra row's date.

</details>

多出一行（731 vs 730）。查一下实际的行分布找出多出来的那条：

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
SE…`)
<!-- call_id: tool_EezR4nS5RFXb6CHTj9oqbp9K -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 hotel -e \"\nSELECT COUNT(*) c, MIN(stay_date), MAX(stay_date) FROM daily_inventory WHERE room_type_id=1;\nSELECT stay_date, COUNT(*) c FROM daily_inventory WHERE room_type_id=1 GROUP BY stay_date HAVING c > 1 LIMIT 5;\nSELECT stay_date FROM daily_inventory WHERE room_type_id=1 AND stay_date >= CURDATE() + INTERVAL 729 DAY ORDER BY stay_date;\" 2>&1 | grep -v Warning"
}
```

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "
SE…`)</summary>

<!-- call_id: tool_EezR4nS5RFXb6CHTj9oqbp9K -->
c	MIN(stay_date)	MAX(stay_date)
731	2026-10-04	2028-10-03
stay_date
2028-10-02
2028-10-03


</details>

<details><summary>Thinking</summary>

MAX = 2028-10-03 = today + 729 days... wait today is 2026-10-04. 2026-10-04 + 730 days = 2028-10-03. So rows go up to 2028-10-03, meaning the window is [today, today+730] — 731 rows? Let me recompute: preCreate with startDate = 2026-10-04, endDate = start.plusDays(730) = 2028-10-03, condition `d < endDate` → max d = 2028-10-02. But DB shows 2028-10-03 exists!

`d < #{endDate}` — endDate bound as LocalDate 2028-10-03. If the parameter is bound as a DATETIME string '2028-10-03' vs comparison semantics... `d < '2028-10-03'` with d = 2028-10-03 → false → stops. So max should be 2028-10-02. But we got 2028-10-03!

Unless endDate parameter was bound including time... if bound as '2028-10-03 00:00:00' and d is a DATE → d < '2028-10-03 00:00:00' → d=2028-10-03 → '2028-10-03 00:00:00' < '2028-10-03 00:00:00' → false. Still stops.

Hmm wait — actually maybe the startup runner's `today` was 2026-10-04 and endDate = 2028-10-03, but the recursion: `SELECT #{startDate} UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < #{endDate}`. The WHERE applies to the anchor rows... The recursive member generates d+1 from rows where d < endDate. So rows: start, start+1, ..., up to the last d where d < endDate, generating d+1 ≤ endDate. If d = 2028-10-02 < 2028-10-03 → generate 2028-10-03! The generated row is NOT filtered by the WHERE (the WHERE only limits which rows recurse further). So the window is [start, end] INCLUSIVE = 731 rows. My CTE semantics were off by one.

In my earlier manual test: endDate = '2026-10-04' + 729 days = 2028-10-02, rows max = 2028-10-02... wait earlier test showed MAX = 2028-10-02 with INTERVAL 729 DAY — same off-by-one: [10-04, 10-04+729] inclusive = 730 rows, max = 2028-10-02. I thought it was exclusive; it was inclusive. Consistent.

So the window is [start, end] inclusive → PRE_CREATE_DAYS=730 gives 731 rows ([today, today+730]). Options: keep inclusive semantics and set end = start.plusDays(PRE_CREATE_DAYS - 1), or fix test to expect 731. Cleaner: make it [start, start+PRE_CREATE_DAYS) exclusive as documented — change SQL to `d + INTERVAL 1 DAY < #{endDate}` in WHERE: generates d+1 only if d+1 < endDate → max = endDate - 1 = exclusive ✓.

Change: `UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d + INTERVAL 1 DAY < #{endDate}` — hmm, anchor: startDate itself always included even if startDate >= endDate — fine (never happens).

Wait but windowRowsExistAfterStartup PASSED with count == 730 using `lt start.plusDays(730)` — because rows beyond that bound existed but were excluded by the query bound. And idempotent test counted >= today with no upper bound → 731. So both consistent with inclusive-end behavior.

I'll fix the SQL to true exclusive bound and rerun. Existing 731st row (2028-10-03) will linger — harmless; but for the idempotent test counting >= today... after fix, window = [today, today+730) = 730 rows, but the stale 2028-10-03 row (from previous runs) still exists → count 731 again! Ugh. The stale row came from runner runs with the buggy SQL. I should delete the out-of-window row manually, or make tests robust. Cleanest: delete rows beyond the window now (one-time cleanup of my own test residue).

Also note: this stale-row issue reveals that narrowing semantics leave residue — fine for dev DB.

Fix SQL, clean stale rows, rerun.

</details>

找到原因：递归 CTE 的 `WHERE d < endDate` 只限制"是否继续递归"，不限制生成的行本身，所以窗口实际是闭区间 `[start, end]` 多出一行。改为排他上界，并清掉之前错误运行留下的越界行：

#### Tool Call: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)
<!-- call_id: tool_f7LLiAAgrDa4LR5HIAD0XoUu -->
```json
{
  "new_string": "    /** 批量创建某房型 [startDate, endDate) 的库存行（已存在则忽略），total_count 取当前房间数 */\n    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"WITH RECURSIVE dates(d) AS (\"\n            + \"  SELECT #{startDate} \"\n            + \"  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d + INTERVAL 1 DAY < #{endDate}\"\n            + \") \"\n            + \"SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates\")",
  "old_string": "    /** 批量创建某房型 [startDate, endDate) 的库存行（已存在则忽略），total_count 取当前房间数 */\n    @Insert(\"INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) \"\n            + \"WITH RECURSIVE dates(d) AS (\"\n            + \"  SELECT #{startDate} \"\n            + \"  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d < #{endDate}\"\n            + \") \"\n            + \"SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates\")",
  "path": "hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java"
}
```

#### Tool Call: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "DEL…`)
<!-- call_id: tool_9aSNuWBVD2mEpk3zzIMVhsj3 -->
```json
{
  "command": "docker exec hotel-mysql mysql -uroot -proot123 hotel -e \"DELETE FROM daily_inventory WHERE stay_date >= CURDATE() + INTERVAL 730 DAY; SELECT COUNT(*) c FROM daily_inventory;\" 2>&1 | grep -v Warning"
}
```

<details><summary>Tool Result: Edit (`hotel-server/src/main/java/com/example/hotel/inventory/mappe…`)</summary>

<!-- call_id: tool_f7LLiAAgrDa4LR5HIAD0XoUu -->
Replaced 1 occurrence in hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java

</details>

<details><summary>Tool Result: Bash (`docker exec hotel-mysql mysql -uroot -proot123 hotel -e "DEL…`)</summary>

<!-- call_id: tool_9aSNuWBVD2mEpk3zzIMVhsj3 -->
c
2920


</details>

#### Tool Call: Bash (`cd hotel-server && for i in 1 2 3; do mvn test 2>&1 | grep -…`)
<!-- call_id: tool_lCMTEAm4n0x3ZSPF8HNMdK58 -->
```json
{
  "command": "cd hotel-server && for i in 1 2 3; do mvn test 2>&1 | grep -E \"Tests run:.*-- in|Tests run:.*Skipped: [01],|BUILD\" | tail -4; echo \"--- run $i ---\"; done",
  "timeout": 300
}
```

<details><summary>Tool Result: Bash (`cd hotel-server && for i in 1 2 3; do mvn test 2>&1 | grep -…`)</summary>

<!-- call_id: tool_lCMTEAm4n0x3ZSPF8HNMdK58 -->
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.353 s -- in com.example.hotel.inventory.InventoryPreCreatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.103 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
--- run 1 ---
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.184 s -- in com.example.hotel.inventory.InventoryPreCreatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.083 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
--- run 2 ---
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.193 s -- in com.example.hotel.inventory.InventoryPreCreatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.087 s -- in com.example.hotel.inventory.InventoryServiceTest
[INFO] BUILD SUCCESS
--- run 3 ---


</details>

<details><summary>Thinking</summary>

Wait — InventoryServiceTest now runs in 0.08-0.1s (was 1.2s)? The concurrency test with 10 threads completed that fast? Plausible: rows now pre-created for... no wait, the test uses 2099 dates — outside window (2028-10), so fallback path still. 10 threads, 2 days... could be fast if no contention waits (rows exist after first run? No, cleanup deletes them). 0.08s for 10 concurrent 2-day occupies... hmm, actually with small row counts and local MySQL it's possible once context is cached. Actually elapsed shown per class; surefire "Time elapsed" for the second class might exclude context startup (cached). 10 threads doing ~6 statements each on localhost InnoDB — 80ms is plausible. Fine — Tests run: 4, 0 failures.

Now update docs/02-技术方案.md: v1.2, decision 13, §3.4 inventory row, §4.2, §5.1, §8.

</details>

全部通过且稳定。现在更新技术方案文档（v1.2）：

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_Zifiv16pFOhtlInIt9dBWzlN -->
```json
{
  "new_string": "| 文档版本 | v1.2 |\n| 文档日期 | 2026-10-04 |\n| 文档状态 | 已定稿 |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |\n| 输入 | 已确认的 8 项技术决策 + 5 项补充确认（见 §1.2、§1.3） |\n| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全）；v1.2：库存行改为启动/变更时预创建（决策 13，下单事务内不再建行） |",
  "old_string": "| 文档版本 | v1.1 |\n| 文档日期 | 2026-10-04 |\n| 文档状态 | 已定稿 |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |\n| 输入 | 已确认的 8 项技术决策 + 4 项补充确认（见 §1.2、§1.3） |\n| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全） |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_Zifiv16pFOhtlInIt9dBWzlN -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_BZFjQo5Y0dODOl8rKpGH1zBe -->
```json
{
  "new_string": "| 11 | 账号体系 | 单 `user` 表 + `role` 字段（住客自助注册、admin 预置，共用认证链路） |\n| 12 | token 存储 | 存数据库表（重启不掉线，与\"服务端存储\"决策一致） |\n| 13 | 库存行创建时机（v1.2 修订） | 启动时与房型/房间变更时，按 `INSERT IGNORE` 幂等预创建未来 730 天的逐日库存行；超出窗口的日期下单时懒建兜底（独立小事务建行，见 §5.1）。替代 v1.1 的\"首次预订该日时在下单事务内建行\" |",
  "old_string": "| 11 | 账号体系 | 单 `user` 表 + `role` 字段（住客自助注册、admin 预置，共用认证链路） |\n| 12 | token 存储 | 存数据库表（重启不掉线，与\"服务端存储\"决策一致） |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_BZFjQo5Y0dODOl8rKpGH1zBe -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_qFBL9seHQ3Cknmhjd6MvJPzv -->
```json
{
  "new_string": "| **inventory** | `queryAvailability(typeId, checkin, checkout)` / `tryOccupy(...)` / `release(...)` | 逐日库存行的预创建与懒建兜底、并发原子占用、剩余量取逐日最小值（BR-02）、取消/退房释放 |",
  "old_string": "| **inventory** | `queryAvailability(typeId, checkin, checkout)` / `tryOccupy(...)` / `release(...)` | 逐日库存行的创建与并发原子占用、剩余量取逐日最小值（BR-02）、取消/退房释放 |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_qFBL9seHQ3Cknmhjd6MvJPzv -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_H4ZFMxAhi44aXs54b0gum0IX -->
```json
{
  "new_string": "- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。库存行在启动时与房型/房间变更时预创建（决策 13）：`INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的行，`total_count` 取当前房间数；UC-09 维护房间（增/减/改挂）时，先校验\"新房间数 ≥ 当前有效订单数\"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值（不存在的行随预创建一并补上）。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步。**",
  "old_string": "- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。占用库存时从 `room` 表取当前房间总数写入新行；UC-09 维护房间（增/减/改挂）时，先校验\"新房间数 ≥ 当前有效订单数\"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步。**",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_H4ZFMxAhi44aXs54b0gum0IX -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

现在改写 §5.1 的占用流程：

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_UtSb17548qfO3tIZjeGnnvzC -->
```json
{
  "new_string": "创建订单先由 service 层做**业务规则校验**（BR-01：入住日期 ≥ 当天、离店日期 > 入住日期、住客姓名非空、手机号格式合法），校验不通过直接拒绝，不进入库存占用。\n\n库存行**预创建**（决策 13）：应用启动时与房型/房间变更时，按 `INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的逐日库存行（`total_count` 取当前房间数）。预创建保证下单热路径上库存行**总是存在**——这是并发安全的前提，原因见本节末尾。\n\n校验通过后在**一个事务**内，对日期区间 `[checkin, checkout)` 的每一天**按日期升序**执行（固定加锁顺序，降低并发事务死锁概率）：\n\n1. 原子条件更新：\n   `UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count`\n2. 影响行数 = 1 → 该日占用成功；= 0 → 行存在但已订满，或超出预创建窗口（行不存在）。先用无锁一致性读区分：行不存在则经**独立小事务**懒建该行（`INSERT IGNORE`，立即提交），随后重试一次条件更新。\n3. 重试后仍为 0 → 当日已订满，**抛业务异常、整单回滚**（已占用的日期行随事务一并回滚），返回\"该房型所选日期已订满\"。\n4. 全部日期占用成功 → 插入订单，提交事务。\n\n并发正确性由两点保证：`occupied_count < total_count` 的条件更新在 MySQL 行锁下是原子的（不超卖）；`(room_type_id, stay_date)` 唯一索引防止库存行重复。\n\n**为什么不能在下单事务内建行**（v1.2 实测结论）：下单事务里 INSERT 库存行有两类并发死锁——① REPEATABLE READ 下对不存在的行做加锁 UPDATE 会产生间隙锁，并发 INSERT 同一间隙互相等待成环；② INSERT 撞唯一索引后 InnoDB 为重复键检查持有该记录的 S 锁直到事务结束，并发事务再请求 X 锁形成 S→X 升级死锁（READ COMMITTED 下同样存在）。预创建把\"行存在\"变成数据不变式后，占用路径只剩对已存在行的条件更新（纯记录锁），配合日期升序的固定加锁顺序，无死锁面；懒建兜底走独立小事务，同样不把 INSERT 引入下单事务。\n\n查询剩余量（无需事务）：区间内每日 `total_count − occupied_count` 的最小值；无库存行的日期按 `total_count = 当前房间数, occupied = 0` 计。",
  "old_string": "创建订单先由 service 层做**业务规则校验**（BR-01：入住日期 ≥ 当天、离店日期 > 入住日期、住客姓名非空、手机号格式合法），校验不通过直接拒绝，不进入库存占用。\n\n校验通过后在**一个事务**内，对日期区间 `[checkin, checkout)` 的每一天**按日期升序**执行（固定加锁顺序，降低并发事务死锁概率）：\n\n1. 原子条件更新：\n   `UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count`\n2. 影响行数 = 1 → 该日占用成功；= 0 → 该行可能不存在（首次预订该日），尝试 `INSERT`（`total_count` 取当前房间数）；INSERT 撞唯一索引说明并发已建行，回到第 1 步重试一次。\n3. 重试后仍为 0 → 当日已订满，**抛业务异常、整单回滚**（已占用的日期行随事务一并回滚），返回\"该房型所选日期已订满\"。\n4. 全部日期占用成功 → 插入订单，提交事务。\n\n并发正确性由两点保证：`occupied_count < total_count` 的条件更新在 MySQL 行锁下是原子的（不超卖）；`(room_type_id, stay_date)` 唯一索引防止并发建行产生重复行。\n\n查询剩余量（无需事务）：区间内每日 `total_count − occupied_count` 的最小值；无库存行的日期按 `total_count = 当前房间数, occupied = 0` 计。",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_UtSb17548qfO3tIZjeGnnvzC -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_ieafgEuu8dAHb48bF4HS6rqj -->
```json
{
  "new_string": "| 逐日库存行随时间累积 | 预创建窗口 730 天（每房型约 730 行，4 房型 ≈ 3000 行），窗口外日期懒建；演示规模（几百订单）无压力 | 不清理；口头说明生产版可归档 |",
  "old_string": "| 逐日库存行随时间累积 | 每房型每日一行，演示规模（几百订单）无压力 | 不清理；口头说明生产版可归档 |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_ieafgEuu8dAHb48bF4HS6rqj -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

Also check §2.1 start.sh flow mentions schema.sql/data.sql — pre-creation happens via ApplicationRunner at startup; worth adding a mention? §2.1 step 6 says schema.sql + data.sql 完成建表与预置数据. Could add "并由 InventoryPreCreator 预创建库存行". Minor — let me add briefly. Also AGENTS.md references? AGENTS.md doesn't mention lazy creation. Fine.

Let me check §2.1 text: "6. java -jar hotel-server.jar 启动应用；schema.sql + data.sql 完成建表与预置数据（房型 3~4 种、每房型若干房间、admin 账号，满足 AC-20），脚本幂等可重复启动（见 §4.2）。" Add: "启动后 InventoryPreCreator 幂等预创建未来 730 天库存行（决策 13）".

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_Ne1PUD2yyAyZ2CpfmHZuWpcg -->
```json
{
  "new_string": "6. `java -jar hotel-server.jar` 启动应用；`schema.sql` + `data.sql` 完成建表与预置数据（房型 3~4 种、每房型若干房间、admin 账号，满足 AC-20），脚本幂等可重复启动（见 §4.2）；启动后由 `InventoryPreCreator` 幂等预创建未来 730 天的库存行（决策 13）。",
  "old_string": "6. `java -jar hotel-server.jar` 启动应用；`schema.sql` + `data.sql` 完成建表与预置数据（房型 3~4 种、每房型若干房间、admin 账号，满足 AC-20），脚本幂等可重复启动（见 §4.2）。",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_Ne1PUD2yyAyZ2CpfmHZuWpcg -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

Doc updated. Now final regression: run full mvn test again once more (already 3 green runs after the fix). Also verify the fallback path test still meaningful: InventoryServiceTest uses 2099 dates (beyond 2028-10 window) → exercises ensureRow fallback ✓. PreCreator tests cover window path ✓.

One more check: the DB currently has 2920 rows (4 types × 730). Also should the `tryOccupy` test dates (2099) remain beyond window forever — window is rolling (today+730), 2099 is safely beyond ✓.

Done. Report summary. Also remind: 任务清单状态更新和提交待确认。Maybe also mention 技术决策表 updated (§1.2 decision 13).

</details>

修改完成，回归测试通过。

## 代码改动

- **新增 `inventory/InventoryPreCreator.java`**：`ApplicationRunner`，启动时为全部房型批量预创建 `[今天, 今天+730天)` 库存行（`INSERT IGNORE` 幂等，4 房型 ≈ 2920 行）；`preCreateWindow()` 公开，供 T9 房型/房间变更时复用
- **`InventoryMapper`**：新增 `selectRoomTypeIds()` 和 `preCreate()`（递归 CTE 一次插入 730 行；修正了 CTE 上界为排他区间）
- **`InventoryService.tryOccupy`**：逻辑不变但语义变了——窗口内库存行恒存在，热路径只剩纯条件更新（记录锁）；`exists` 检查 + `ensureRow` 降级为**窗口外日期的懒建兜底**
- **`InventoryRowCreator`**：注释更新为懒建兜底角色
- **新增 `InventoryPreCreatorTest`**：窗口行存在且完整（730 行/type）、幂等（重复执行不增行）、窗口内占用/释放往返

## 文档改动（`docs/02-技术方案.md` → v1.2）

- §1.2 新增**决策 13**：库存行预创建时机，替代 v1.1 的"下单事务内首次预订建行"
- §5.1 重写占用流程，并补充"为什么不能在下单事务内建行"的实测结论（间隙锁 + 撞键 S 锁升级两类死锁）
- §4.2 一致性说明、§2.1 启动流程、§3.4 inventory 模块职责、§8 风险表同步更新

## 测试结果

`mvn test` 连跑 3 轮全绿，共 7 个用例：

- `InventoryServiceTest` 4 个（TC-B01/B02 查询语义、TC-G01 库存断言：10 线程并发成功数恰为 3、不超卖、无残留）——2099 年日期，覆盖**窗口外懒建兜底**路径
- `InventoryPreCreatorTest` 3 个（窗口行存在、幂等、窗口内占用释放往返）——覆盖**预创建热路径**

任务清单中 T7 状态与偏差记录等你验收后我再更新；确认后可提交（建议提交信息：`编码测试: inventory模块（防超卖，库存行预创建）`）。

## Turn 5

### User

提交推送

### Assistant

<details><summary>Thinking</summary>

User says commit and push. First update the task list (T7 status + deviation note), then commit and push. Check remote exists.

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_3rvQ2pJjbo4Pp5YgzBrkQwQV -->
```json
{
  "new_string": "> 进度快照：2026-10-04。**T1~T7 已完成并验收**（T1~T5 commit `af5b058`），下一个任务从 **T8** 开始。",
  "old_string": "> 进度快照：2026-10-04。**T1~T6 已完成并验收**（T1~T5 commit `af5b058`），下一个任务从 **T7** 开始。",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_3rvQ2pJjbo4Pp5YgzBrkQwQV -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_4NT5cN1KfFXsSXcLy6ucU9HK -->
```json
{
  "new_string": "| T6 room 模块（住客侧只读） | ✅ 完成 | room_type/room 实体与 mapper、两个只读接口、listAssignableRooms；TC-F01①、TC-A01①② 实测通过 |\n| T7 inventory 模块（防超卖） | ✅ 完成 | queryAvailability/tryOccupy/release + 库存行预创建（决策 13）；7 个集成测试通过（含 10 线程并发不超卖） |\n| T8~T12 | ⬜ 待做 | 见下方任务定义 |",
  "old_string": "| T6 room 模块（住客侧只读） | ✅ 完成 | room_type/room 实体与 mapper、两个只读接口、listAssignableRooms；TC-F01①、TC-A01①② 实测通过 |\n| T7~T12 | ⬜ 待做 | 见下方任务定义 |",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_4NT5cN1KfFXsSXcLy6ucU9HK -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_BJNhAUdtKYuBMPdxQDLBxLqb -->
```json
{
  "new_string": "- admin 预置账号：`admin / admin123`（BCrypt 密文在 data.sql）。\n- **库存行预创建（技术方案 v1.2 决策 13）**：启动时 `InventoryPreCreator` 按 `INSERT IGNORE` 预创建未来 730 天库存行；下单事务内绝不建行（实测同事务 INSERT 会引间隙锁/撞键 S 锁两类并发死锁），窗口外日期由 `InventoryRowCreator` 独立小事务懒建兜底。Hikari 连接池已调到 30（REQUIRES_NEW 建行需额外借连接）。\n- 集成测试已引入 `hotel-server/src/test`（`mvn test`，需 MySQL 已启动）；并发用例直接用 2099 年日期避免与预创建窗口（今天+730 天）冲突。",
  "old_string": "- admin 预置账号：`admin / admin123`（BCrypt 密文在 data.sql）。",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_BJNhAUdtKYuBMPdxQDLBxLqb -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_iuMES2Cx0PoseC6eNC6T50UW -->
```json
{
  "new_string": "### T7 inventory 模块（防超卖核心）✅（已验收，库存行预创建见技术方案 v1.2 决策 13）",
  "old_string": "### T7 inventory 模块（防超卖核心）",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_iuMES2Cx0PoseC6eNC6T50UW -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

#### Tool Call: Bash (`git add -A hotel-server ai-process docs && git status --shor…`)
<!-- call_id: tool_OqCzVizqEof9zxMxQHcaFH4y -->
```json
{
  "command": "git add -A hotel-server ai-process docs && git status --short && git commit -m \"编码测试: inventory模块（防超卖，库存行预创建）\" && git log --oneline -2"
}
```

<details><summary>Tool Result: Bash (`git add -A hotel-server ai-process docs && git status --shor…`)</summary>

<!-- call_id: tool_OqCzVizqEof9zxMxQHcaFH4y -->
A  ai-process/conversations/kimi-export-session_-20261004-085925.md
M  "ai-process/\347\274\226\347\240\201\344\273\273\345\212\241\346\270\205\345\215\225.md"
M  "docs/02-\346\212\200\346\234\257\346\226\271\346\241\210.md"
A  hotel-server/src/main/java/com/example/hotel/inventory/InventoryPreCreator.java
A  hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java
A  hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java
A  hotel-server/src/main/java/com/example/hotel/inventory/entity/DailyInventory.java
A  hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java
M  hotel-server/src/main/resources/application.yml
A  hotel-server/src/test/java/com/example/hotel/inventory/InventoryPreCreatorTest.java
A  hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java
[main f4bb466] 编码测试: inventory模块（防超卖，库存行预创建）
 11 files changed, 3858 insertions(+), 12 deletions(-)
 create mode 100644 ai-process/conversations/kimi-export-session_-20261004-085925.md
 create mode 100644 hotel-server/src/main/java/com/example/hotel/inventory/InventoryPreCreator.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/inventory/entity/DailyInventory.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java
 create mode 100644 hotel-server/src/test/java/com/example/hotel/inventory/InventoryPreCreatorTest.java
 create mode 100644 hotel-server/src/test/java/com/example/hotel/inventory/InventoryServiceTest.java
f4bb466 编码测试: inventory模块（防超卖，库存行预创建）
739e4de 编码测试: room模块（住客侧只读接口与可分配房间推导）


</details>

<details><summary>Thinking</summary>

Committed f4bb466. Note: the commit also swept in `ai-process/conversations/kimi-export-session_*.md` (a session export) since I used `git add -A` on those dirs. It's included — acceptable, it's part of ai-process records. Now push. Check remote first.

</details>

#### Tool Call: Bash (`git remote -v && git push 2>&1 | tail -3`)
<!-- call_id: tool_hBCGCupsq4GUFmzMq1jMuJC7 -->
```json
{
  "command": "git remote -v && git push 2>&1 | tail -3",
  "timeout": 120
}
```

<details><summary>Tool Result: Bash (`git remote -v && git push 2>&1 | tail -3`)</summary>

<!-- call_id: tool_hBCGCupsq4GUFmzMq1jMuJC7 -->
origin	git@github.com:donglongjun886/hotel-reservation-system.git (fetch)
origin	git@github.com:donglongjun886/hotel-reservation-system.git (push)
To github.com:donglongjun886/hotel-reservation-system.git
   af5b058..f4bb466  main -> main


</details>

<details><summary>Thinking</summary>

Pushed successfully: af5b058..f4bb466 (this included both 739e4de room and f4bb466 inventory since 739e4de wasn't pushed before). Report briefly.

</details>

已提交并推送完成。

- 本次推送包含两个提交：`739e4de`（T6 room 模块）和 `f4bb466`（T7 inventory 模块）
- `f4bb466` 内容：inventory 模块全部代码、7 个集成测试、技术方案 v1.2（决策 13 库存行预创建）、任务清单进度与偏差记录、Hikari 连接池调整
- 已推送到 `origin/main`（`af5b058..f4bb466`）

任务清单已更新：**T1~T7 已验收，下一个任务是 T8（order 模块住客侧：订单号生成、创建订单、我的订单/详情、取消）**。需要继续时说一声。
