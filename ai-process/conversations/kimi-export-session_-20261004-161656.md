---
session_id: session_6dc59ed8-1951-4b44-8be9-67c07ab4c87d
exported_at: 2026-10-04T16:16:56.680Z
work_dir: /Users/donglongjun/IdeaProjects/hotel-reservation-system
message_count: 118
token_count: 58217
---

# Kimi Session Export

## Overview

- **Topic**: 请对已有的代码做一次全面的code review(使用 code review skill),对照 docs/01 需求文档、docs/02 技术方案和 doc…
- **Conversation**: 10 turns | 55 tool calls

---

## Turn 1

### User

请对已有的代码做一次全面的code review(使用 code review skill),对照 docs/01 需求文档、docs/02 技术方案和 docs/03 测试用例检查：1需求和验收标准是否都已实现，业务规则有无偏差； 2是否符合技术方案和 AGENTS.md 中的编码规范；3并发、事务、异常处理等关键点是否有隐患；4测试用例是否覆盖主线和异常场景，是否都能通过。只输出评审意见，按严重程度分类，指出具体文件和位置，不要直接修改代码。完成后等我确认。

## Turn 2

### User

排除前端还未完成 这里面有我需要决策的问题吗

## Turn 3

### User

1金额口径选A 2下单幂等要做 3修文档 4A

## Turn 4

### User

前端不是还没有写吗 为什么已经需要改前端了

## Turn 5

### User

那继续

## Turn 6

### User

提交推送

## Turn 7

### User

评审中剩余的修复项 这个有需要我决策的吗

## Turn 8

### User

订单列表分页要做 自动化测试要补 文档改下，其他按照你的建议，执行吧

## Turn 9

### User

现在什么进度

### Assistant

<details><summary>Thinking</summary>

Continue: update todo list, then docs updates. Start with docs/01 修订记录表 + two 不可订→已订满. Read relevant sections.

</details>

#### Tool Call: TodoList
<!-- call_id: tool_uL8RL4iSZlaropid7Os6jgiY -->
```json
{
  "todos": [
    {
      "title": "补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配",
      "status": "done"
    },
    {
      "title": "文档：PRD 已订满、分页/接口契约、测试用例",
      "status": "in_progress"
    },
    {
      "title": "mvn test + 前端 build + E2E 验证",
      "status": "pending"
    }
  ]
}
```

#### Tool Call: Read (`docs/01-需求文档.md`)
<!-- call_id: tool_BJNykWZ1y4hEgbVFFDJnuVDa -->
```json
{
  "path": "docs/01-需求文档.md",
  "line_offset": 1,
  "n_lines": 20
}
```

#### Tool Call: Read (`ai-process/编码任务清单.md`)
<!-- call_id: tool_YQ05cexBRLsaK2roydRXJ5TM -->
```json
{
  "path": "ai-process/编码任务清单.md",
  "line_offset": -60
}
```

<details><summary>Tool Result: TodoList</summary>

<!-- call_id: tool_uL8RL4iSZlaropid7Os6jgiY -->
Todo list updated.
Current todo list:
  [done] 补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配
  [in_progress] 文档：PRD 已订满、分页/接口契约、测试用例
  [pending] mvn test + 前端 build + E2E 验证

Ensure that you continue to use the todo list to track progress. Mark tasks done immediately after finishing them, and keep exactly one task in_progress when work is underway.

</details>

<details><summary>Tool Result: Read (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_BJNykWZ1y4hEgbVFFDJnuVDa -->
1	# 酒店预订系统 需求文档（PRD）
2	
3	| 项目 | 内容 |
4	| --- | --- |
5	| 文档版本 | v1.1 |
6	| 文档日期 | 2026-10-04 |
7	| 文档状态 | 已确认（v1.1 经需求评审修订） |
8	| 项目性质 | 面试演示项目，要求代码可运行、可演示 |
9	
10	---
11	
12	## 1. 项目目标与范围
13	
14	### 1.1 项目目标
15	
16	做一个带前后端的酒店业务系统，覆盖完整业务主链路：
17	
18	**浏览房型 → 创建预订 → 查询订单 → 前台办理入住 → 退房完成**
19	
20	系统需保证核心业务规则正确（防超卖、状态流转、入住校验），可支撑面试现场全流程演示。

</details>

<details><summary>Tool Result: Read (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_YQ05cexBRLsaK2roydRXJ5TM -->
72	
73	### T7 inventory 模块（防超卖核心）✅（已验收，库存行预创建见技术方案 v1.2 决策 13）
74	- **目标**：`InventoryService` 三方法——`queryAvailability`（逐日最小剩余量，无库存行按当前房间数计）、`tryOccupy`（按日期升序原子条件更新，INSERT 撞唯一索引重试一次，失败整单回滚由调用方事务保证）、`release`（逐日 −1 带 >0 保护）。并发集成测试对该接口多线程调用。
75	- **涉及模块**：inventory。
76	- **对应测试用例**：TC-G01 库存断言部分、TC-B01/B02 查询语义。
77	- **验收条件**：N（≥5）线程并发 tryOccupy 同区间，成功数恰为房间数、occupied 永不超 total；queryAvailability 无库存行日期边界正确。
78	
79	### T8 order 模块（住客侧）✅（已验收）
80	- **目标**：`OrderNoGenerator`（每日序号表、同事务、允许跳号）；创建订单（BR-01 先校验后占用、金额下单时固化 BigDecimal、一个事务内逐日占用+插单）；我的订单/详情（按 token user_id 过滤，忽略前端 userId）；取消（带源状态+user_id 条件更新，成功后 release 库存）。
81	- **涉及模块**：order（依赖 inventory、room）。
82	- **对应测试用例**：TC-B03~B13、TC-C01~C05、TC-A06②、TC-H03、TC-G07；并发 TC-G02、G03。
83	- **验收条件**：非法请求拒绝且不占库存（TC-B13）；中途订满整单回滚无残留（TC-B12）；金额分单位且调价不回溯（TC-B09/B10）；20 线程订单号全局唯一（TC-G07）；越权查/取消被拒（TC-A06②/C05）。
84	- **已实现**：`OrderNoGenerator` + `OrderSeqRowCreator`（序号行懒建改独立小事务，见关键偏差）、`OrderService.create/listMine/detailMine/cancelMine`、`OrderController` 四个住客侧接口；金额 DTO 层分单位换算。
85	- **实测通过**：`OrderServiceTest` 12 个 + `OrderConcurrencyTest` 3 个（20 线程订单号唯一、并发抢最后一间仅一笔成功、取消与预订交叉终态自洽），全量 `mvn test` 22 个用例通过。
86	
87	### T9 order 前台侧 + room 维护接口 ✅（已验收）
88	- **目标**：admin 订单查询（订单号/guest_phone/空查全部）、详情、assignable-rooms；办理入住（BR-06 四项校验、房间行锁+锁内复查+状态条件更新）；办理退房（条件更新+逐日释放）；房型/房间维护（UC-09："新房间数 ≥ 有效订单数"校验、未来库存行 total 同步、改挂双方校验）。
89	- **涉及模块**：order、room、inventory。
90	- **对应测试用例**：TC-D01~D09、TC-E01~E05、TC-F03~F08；并发 TC-G04、G05、G06。
91	- **验收条件**：入住校验文案与原型一致；并发分房仅一笔成功（TC-G04）；提前退房剩余晚数可再售且金额不变（TC-E02/E03）；减房低于有效订单数被拒（TC-F07）；改挂双方库存同步（TC-F08）。
92	- **已实现**：`OrderAdminController`（查询/详情/assignable-rooms/check-in/check-out）、`RoomAdminController`（房型与房间 GET/POST/PUT）、`RoomService` UC-09 维护（有效订单校验 + 改挂双方同步）、`InventoryService.syncTotalForFutureDates`（补建行 + 刷 total）；`OrderInfo` 增加 roomNo/idCard；新增依赖方向 room → inventory（见关键偏差）。
93	- **实测通过**：`OrderAdminServiceTest` 15 个 + `RoomAdminServiceTest` 6 个 + `AdminConcurrencyTest` 3 个（并发分房仅一笔成功、并发入住同一订单仅一笔生效、取消+入住交叉终态自洽，连跑 4 轮稳定）；全量 `mvn test` 46 个用例通过无回归。
94	
95	---
96	
97	## 第三阶段：前端页面
98	
99	### T10 住客端页面（P-C1~C8）✅（已验收）
100	- **目标**：房型列表（日期区间剩余量、不可订置灰）、房型详情、登录、注册、预订确认、预订结果（成功/失败态）、我的订单、订单详情（取消+二次确认）。接口全走 `src/api/`。
101	- **涉及模块**：hotel-web views/guest、api/；hotel-server room（补 availability 接口）。
102	- **对应测试用例**：TC-A01~A06、TC-B01~B11、TC-C01~C03 的 UI 部分。
103	- **验收条件**：游客浏览+预订引导登录（TC-A01）；订满跳失败页提示"该房型所选日期已订满"（TC-B11）；金额页面展示元；已入住订单无取消入口（TC-C03①）。
104	- **已实现**：8 个页面 + 共享 `GuestNav` 组件；`src/api/auth.js/room.js/order.js`（含订单状态枚举中文映射；金额最初在 api 层转元，评审修订后改为组件持"分"整数、仅渲染处 `formatYuan` 格式化）；`src/utils/date.js`、`src/utils/money.js`。后端补 `GET /api/room-types/availability`（`RoomTypeAvailability` DTO、`RoomService.queryAvailability`），新增 `RoomAvailabilityTest` 2 个用例；`ErrorCode.PHONE_REGISTERED` 文案对齐原型（见关键偏差）。
105	- **实测通过**：`mvn test` 48 个用例全过；`npm run build` 通过；curl 冒烟：availability 无 token 200/code 0 且剩余量正确、缺参与日期倒挂文案正确、注册→登录→下单→我的订单→详情→取消→重复取消 1006、取消后库存释放恢复、无 token 下单 401。
106	
107	### T11 前台管理端页面（P-A1~A5）
108	- **目标**：admin 登录、订单查询列表、订单详情（入住办理：身份证+房间下拉仅可分配房间；退房+二次确认；按状态显隐操作区）、房型维护、房间维护。
109	- **涉及模块**：hotel-web views/admin、api/admin.js。
110	- **对应测试用例**：TC-D01~D09、TC-E01~E05、TC-F03~F08 的 UI 部分。
111	- **验收条件**：非已确认订单无入住操作区（TC-D06①）；下拉不含在住房间（TC-D08）；表单校验文案与原型一致（TC-F05/F06）。
112	
113	---
114	
115	## 第四阶段：联调收尾
116	
117	### T12 端到端联调、并发用例与交付验证
118	- **目标**：start.sh 全链路实测；并发测试补齐（优先后端集成测试，断言 DB 终态）；README（前置条件、admin 明文密码、启动方式）；主链路 UI 冒烟。
119	- **涉及模块**：全工程。
120	- **对应测试用例**：TC-F01、TC-F02、TC-G01~G07 全量、TC-H01、TC-H05。
121	- **验收条件**：全新环境 `./start.sh` 一键起系统，TC-H05 全流程无阻断；TC-G01~G07 全部通过；docs/03 覆盖对照 AC-01~21 无遗漏。
122	
123	---
124	
125	## 依赖关系
126	
127	```
128	T1 → T2 → T3 → T4（骨架）
129	T3 → T5(auth) → T6(room 只读) → T7(inventory) → T8(order 住客侧) → T9(order 前台侧 + room 维护)
130	T4 + T5~T8 → T10（住客端页面）；T4 + T9 → T11（管理端页面）；T10 + T11 → T12（联调收尾）
131	```

</details>

<details><summary>Thinking</summary>

Interesting — the summary said T11/T12 pending, but task list says T10 done. Wait, summary earlier said frontend done? Earlier in the conversation "前端不是还没有写吗 为什么已经需要改前端了" — but task list says T10 guest pages done, T11 admin pages pending. Hmm, actually the user message earlier said "排除前端还未完成" — that was at review time. But then T10 shows done. Anyway, this is second-round revision; T10 done, T11 pending. MyOrdersView was already modified for pagination (done per summary).

Now do docs edits. Multiple files can be edited in parallel. Let me start with docs/01.

</details>

#### Tool Call: Edit (`docs/01-需求文档.md`)
<!-- call_id: tool_srwoL9vxbFr6JN5JqNKbPnn3 -->
```json
{
  "path": "docs/01-需求文档.md",
  "old_string": "| 文档版本 | v1.1 |\n| 文档日期 | 2026-10-04 |\n| 文档状态 | 已确认（v1.1 经需求评审修订） |",
  "new_string": "| 文档版本 | v1.2 |\n| 文档日期 | 2026-10-05 |\n| 文档状态 | 已确认（v1.2 统一\"已订满\"用词） |"
}
```

#### Tool Call: Edit (`docs/01-需求文档.md`)
<!-- call_id: tool_x47kRyoE7Q6h2rZnYiQSkWQk -->
```json
{
  "path": "docs/01-需求文档.md",
  "old_string": "若该日期区间内任意一天已订满，则该房型\"不可订\"，不可选择。",
  "new_string": "若该日期区间内任意一天已订满，则该房型\"已订满\"，不可选择。"
}
```

#### Tool Call: Edit (`docs/01-需求文档.md`)
<!-- call_id: tool_EXalCwUjOY9UcLsbEnEwIPnc -->
```json
{
  "path": "docs/01-需求文档.md",
  "old_string": "系统展示当前可订的房型列表，若所选日期内已无房则标记\"不可订\"。",
  "new_string": "系统展示当前可订的房型列表，若所选日期内已无房则标记\"已订满\"。"
}
```

<details><summary>Tool Result: Edit (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_srwoL9vxbFr6JN5JqNKbPnn3 -->
Replaced 1 occurrence in docs/01-需求文档.md

</details>

<details><summary>Tool Result: Edit (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_x47kRyoE7Q6h2rZnYiQSkWQk -->
old_string not found in docs/01-需求文档.md, the file contents may be out of date. Please use the Read Tool to reload the content.


</details>

<details><summary>Tool Result: Edit (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_EXalCwUjOY9UcLsbEnEwIPnc -->
old_string not found in docs/01-需求文档.md, the file contents may be out of date. Please use the Read Tool to reload the content.


</details>

<details><summary>Thinking</summary>

The 不可订 strings differ from my summary's memory. Read around lines 79 and 246.

</details>

#### Tool Call: Read (`docs/01-需求文档.md`)
<!-- call_id: tool_1ud0ou5Qnuo02O1Gaq5bVpGR -->
```json
{
  "line_offset": 72,
  "n_lines": 14,
  "path": "docs/01-需求文档.md"
}
```

#### Tool Call: Read (`docs/01-需求文档.md`)
<!-- call_id: tool_WEPuQPotXeL7ZSUkdUzPWCYO -->
```json
{
  "line_offset": 238,
  "n_lines": 16,
  "path": "docs/01-需求文档.md"
}
```

<details><summary>Tool Result: Read (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_1ud0ou5Qnuo02O1Gaq5bVpGR -->
72	- **主流程**：住客填写手机号、密码完成注册 → 使用手机号+密码登录 → 进入住客首页
73	- **异常**：手机号已注册则提示；密码错误则提示
74	
75	### UC-02 浏览房型与查询可订状态
76	
77	- **参与者**：游客 / 住客（无需登录）
78	- **主流程**：进入房型列表页 → 查看房型名称、单价、介绍 → 选择入住日期与离店日期 → 系统展示各房型在该日期区间的可订状态与剩余数量
79	- **异常**：日期区间无剩余数量的房型展示为"不可订"
80	
81	### UC-03 创建预订
82	
83	- **参与者**：住客（已登录）
84	- **前置条件**：所选房型在所选日期区间有可订数量
85	- **主流程**：住客选择房型与日期区间 → 填写住客姓名、手机号 → 确认订单金额（单价 × 晚数）→ 提交 → 系统生成订单（订单号含 HR+日期），订单状态为"已确认"

</details>

<details><summary>Tool Result: Read (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_WEPuQPotXeL7ZSUkdUzPWCYO -->
238	
239	- AC-01：未登录用户可浏览房型列表；点击"预订"时被引导至登录页
240	- AC-02：使用未注册手机号可注册成功，注册后可登录；重复手机号注册被提示
241	- AC-03：住客"我的订单"中只能看到本人订单，看不到他人订单
242	- AC-04：住客访问前台功能被拒绝；预置 admin 可登录前台后台
243	
244	**浏览与预订**
245	
246	- AC-05：选择入住/离店日期后，各房型展示剩余可订数量；无剩余时展示"不可订"且无法提交
247	- AC-06：离店日期早于或等于入住日期时无法提交并提示
248	- AC-07：成功创建订单后：生成含 HR+日期的唯一订单号，状态为"已确认"，金额 = 单价 × 晚数，订单含姓名与手机号
249	- AC-08（防超卖）：某房型某日期区间仅剩 1 间时，两名住客同时提交预订，一笔成功、一笔失败并提示"已订满"，成功订单数不超过物理房间数
250	
251	**取消**
252	
253	- AC-09："已确认"订单取消成功：状态变"已取消"，该日期区间剩余可订数量 +1

</details>

#### Tool Call: Edit (`docs/01-需求文档.md`)
<!-- call_id: tool_zfvM41mdVaV4OA4f6F6VxCup -->
```json
{
  "new_string": "- **异常**：日期区间无剩余数量的房型展示为\"已订满\"",
  "old_string": "- **异常**：日期区间无剩余数量的房型展示为\"不可订\"",
  "path": "docs/01-需求文档.md"
}
```

#### Tool Call: Edit (`docs/01-需求文档.md`)
<!-- call_id: tool_cA2Jmytcxrg3fomDgMNpBZK6 -->
```json
{
  "new_string": "- AC-05：选择入住/离店日期后，各房型展示剩余可订数量；无剩余时展示\"已订满\"且无法提交",
  "old_string": "- AC-05：选择入住/离店日期后，各房型展示剩余可订数量；无剩余时展示\"不可订\"且无法提交",
  "path": "docs/01-需求文档.md"
}
```

<details><summary>Tool Result: Edit (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_zfvM41mdVaV4OA4f6F6VxCup -->
Replaced 1 occurrence in docs/01-需求文档.md

</details>

<details><summary>Tool Result: Edit (`docs/01-需求文档.md`)</summary>

<!-- call_id: tool_cA2Jmytcxrg3fomDgMNpBZK6 -->
Replaced 1 occurrence in docs/01-需求文档.md

</details>

<details><summary>Thinking</summary>

docs/01 done. Next docs/05 edits. Read relevant areas first.

</details>

docs/01 完成。接着改 docs/05 原型文档。

#### Tool Call: Read (`docs/05-前端原型需求.md`)
<!-- call_id: tool_VuYuBFw4sITwLXi0EKAVlgX1 -->
```json
{
  "line_offset": 128,
  "n_lines": 75,
  "path": "docs/05-前端原型需求.md"
}
```

<details><summary>Tool Result: Read (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_VuYuBFw4sITwLXi0EKAVlgX1 -->
128	
129	#### P-C6 预订结果页
130	
131	- **布局**：居中结果卡片
132	- **成功态**：成功图标占位、"预订成功"标题、订单号（突出展示，提示"入住时请出示订单号"）、订单摘要（房型/日期/金额）；按钮："查看我的订单"、"返回首页"
133	- **失败态**：失败图标占位、"预订失败"标题、原因文案（如"该房型所选日期已订满"）；按钮："重新选择房型"
134	
135	#### P-C7 我的订单列表页
136	
137	- **布局**：顶部导航同 P-C1；主体为订单卡片纵向列表（最新在前）
138	- **展示字段**（每条）：订单号、房型名称、入住/离店日期、金额、订单状态标签（已确认/已入住/已完成/已取消）
139	- **操作按钮**：每条订单"查看详情"
140	- **空数据**："您还没有订单，快去预订吧" + "去预订"按钮（跳 P-C1）
141	- **报错/异常**：未登录访问 → 跳转 P-C3 登录页
142	
143	#### P-C8 订单详情页（住客）
144	
145	- **布局**：上部状态标签区，中部订单信息区，底部操作区
146	- **展示字段**：订单号、状态、房型、入住/离店日期、晚数、住客姓名、手机号、金额、下单时间；已入住后追加：房间号
147	- **操作按钮**：状态=已确认时显示"取消订单"（点击后弹二次确认"确认取消该订单？"）；其余状态无操作按钮；"返回列表"
148	- **报错/异常**：取消失败（如状态已变化）→ 提示"订单状态已变更，请刷新查看"
149	
150	### 3.2 前台管理端
151	
152	#### P-A1 前台登录页
153	
154	- **布局**：居中卡片表单，标题"酒店管理后台"
155	- **展示字段**：账号输入框、密码输入框
156	- **操作按钮**："登录"
157	- **报错/异常**：账号或密码错误 → "账号或密码错误"
158	
159	#### P-A2 订单查询页
160	
161	- **布局**：左侧导航（订单查询 / 房型管理 / 房间管理）；右上显示"admin + 退出"；主体上部为查询区（输入框：订单号或手机号，"查询"按钮），下部为订单表格
162	- **展示字段**（表格列）：订单号、住客姓名、手机号、房型、入住日期、离店日期、金额、状态、下单时间
163	- **操作按钮**：每行"详情"（跳 P-A3）
164	- **空数据**：无订单或查询无结果 → 表格区域显示"暂无订单"
165	- **报错/异常**：查询条件为空点查询 → 默认展示全部订单（不做报错）
166	
167	#### P-A3 订单详情页（前台）
168	
169	- **布局**：上部订单信息区；中部"办理入住"操作区（仅状态=已确认时出现）；下部"办理退房"操作区（仅状态=已入住时出现）
170	- **展示字段**：订单号、状态、房型、入住/离店日期、住客姓名、手机号、金额、下单时间；已入住后追加：身份证号、房间号
171	- **办理入住操作区**：入住条件校验结果提示（通过/不通过及原因）、身份证号输入框、房间下拉选择（仅列该房型当前空闲房间）、"确认入住"按钮
172	- **办理退房操作区**："确认退房"按钮（点击弹二次确认）
173	- **报错/异常**（入住校验，逐条提示原因）：
174	  - 状态非已确认 → "该订单当前状态不可办理入住"
175	  - 未到入住日 → "未到入住日期（入住日：X）"
176	  - 已超过入住窗口 → "已超过可入住时间（离店日：X），请引导客人取消重订"
177	  - 身份证格式不合法 → "身份证号格式不正确"
178	  - 无空闲房间 → "该房型当前无空闲房间"
179	- **报错/异常**（退房）：状态非已入住时操作区不展示；退房失败 → "订单状态已变更，请刷新"
180	
181	#### P-A4 房型管理页
182	
183	- **布局**：左侧导航同 P-A2；主体上部"新增房型"按钮，下部房型表格
184	- **展示字段**（表格列）：房型名称、单价、介绍（截断显示）、房间数
185	- **操作按钮**：每行"编辑"；新增/编辑弹窗表单：房型名称、单价、介绍，"保存"/"取消"
186	- **空数据**："暂无房型，请点击新增"
187	- **报错/异常**：名称/单价为空或单价非正数 → 表单内提示"请填写完整的房型信息"
188	
189	#### P-A5 房间管理页
190	
191	- **布局**：左侧导航同 P-A2；主体上部"新增房间"按钮，下部房间表格（可按房型筛选）
192	- **展示字段**（表格列）：房间号、所属房型、当前状态（空闲/入住中）
193	- **操作按钮**：每行"编辑"；新增/编辑弹窗表单：房间号、所属房型下拉，"保存"/"取消"
194	- **空数据**："暂无房间，请点击新增"
195	- **报错/异常**：房间号为空或重复 → "房间号不能为空且不可重复"
196	
197	---
198	
199	## 4. 与 PRD 的对应关系
200	
201	| 原型内容 | 对应 PRD |
202	| --- | --- |

</details>

#### Tool Call: Edit (`docs/05-前端原型需求.md`)
<!-- call_id: tool_tKKbVoUZIhgzHpIgscXMloN2 -->
```json
{
  "new_string": "- **布局**：顶部导航同 P-C1；主体为订单卡片纵向列表（最新在前）\n- **分页**：每页 10 条，底部翻页组件（页码 + 总数）",
  "old_string": "- **布局**：顶部导航同 P-C1；主体为订单卡片纵向列表（最新在前）",
  "path": "docs/05-前端原型需求.md"
}
```

<details><summary>Tool Result: Edit (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_tKKbVoUZIhgzHpIgscXMloN2 -->
Replaced 1 occurrence in docs/05-前端原型需求.md

</details>

#### Tool Call: Edit (`docs/05-前端原型需求.md`)
<!-- call_id: tool_bruFwP3M6DMCQv61Pcor3k3k -->
```json
{
  "new_string": "- **展示字段**（表格列）：订单号、住客姓名、手机号、房型、入住日期、离店日期、金额、状态、下单时间\n- **分页**：每页 10 条，表格下方翻页（页码 + 总数）",
  "old_string": "- **展示字段**（表格列）：订单号、住客姓名、手机号、房型、入住日期、离店日期、金额、状态、下单时间",
  "path": "docs/05-前端原型需求.md"
}
```

<details><summary>Tool Result: Edit (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_bruFwP3M6DMCQv61Pcor3k3k -->
Replaced 1 occurrence in docs/05-前端原型需求.md

</details>

#### Tool Call: Edit (`docs/05-前端原型需求.md`)
<!-- call_id: tool_PzUyxkfYvzkgF2h9bMBASX5T -->
```json
{
  "new_string": "- **报错/异常**：房间号为空或重复 → \"房间号不能为空且不可重复\"；在住房间改挂其他房型 → \"该房间正在入住中，不可调整\"",
  "old_string": "- **报错/异常**：房间号为空或重复 → \"房间号不能为空且不可重复\"",
  "path": "docs/05-前端原型需求.md"
}
```

<details><summary>Tool Result: Edit (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_PzUyxkfYvzkgF2h9bMBASX5T -->
Replaced 1 occurrence in docs/05-前端原型需求.md

</details>

<details><summary>Thinking</summary>

docs/05 done. Next docs/03 测试用例. Read the relevant areas: §1 测试层级， §2 auth cases (TC-A10 end), TC-D01/D10 area, TC-F08 area, TC-G07 area, §10 对照表. Let me read sections.

</details>

docs/05 完成。现在改 docs/03 测试用例。

#### Tool Call: Read (`docs/03-测试用例.md`)
<!-- call_id: tool_XzulUNOqmFfyuKjehrB9XaBw -->
```json
{
  "line_offset": 1,
  "n_lines": 70,
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Read (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_XzulUNOqmFfyuKjehrB9XaBw -->
1	# 酒店预订系统 测试用例
2	
3	| 项目 | 内容 |
4	| --- | --- |
5	| 文档版本 | v1.0 |
6	| 文档日期 | 2026-10-04 |
7	| 文档状态 | 待评审 |
8	| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[02-技术方案.md](02-技术方案.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |
9	
10	---
11	
12	## 1. 测试策略
13	
14	**覆盖原则**：以 PRD §7 验收标准（AC-01~21）为主线逐条展开，补充业务规则（BR-01~08）、用例（UC-01~09）中的异常分支，以及技术方案 §5 要求的并发场景。每条用例标注对应需求点，文末附覆盖对照表。
15	
16	**测试层级**：
17	
18	| 层级 | 方式 | 适用范围 |
19	| --- | --- | --- |
20	| UI 手工测试 | 浏览器操作（`http://localhost:8080`，一键启动后） | 主线流程、页面交互、提示文案核对 |
21	| API 测试 | curl / Postman 直接调 `/api/**` | 越权、参数校验、异常分支、绕过前端入口的操作（如 AC-10 "通过其他途径取消"） |
22	| 并发测试 | 后端集成测试 / 多线程脚本（对 `InventoryService` 接口或 `/api/orders` 接口并发请求） | 防超卖、并发房间分配、并发状态流转 |
23	
24	**测试环境**：按 `start.sh` 一键启动（MySQL 8 容器 + 单 jar）。涉及"日期窗口"的用例以执行当天的实际日期动态计算（文中记为 `D` = 当天，`D+1` = 次日，以此类推）。
25	
26	**预置数据**（`data.sql`，对应 AC-20）：
27	
28	- admin 账号：`admin` / 预置密码（见 README）
29	- 预置房型 3~4 种（文中以"大床房 A"代称某一具体房型，价格记为 `P` 元/晚）
30	- 每个房型预置若干房间（文中假设大床房 A 有 3 间：A101/A102/A103，实际以 `data.sql` 为准）
31	
32	**测试账号**：除预置 admin 外，用例中自行注册住客账号，文中记为住客甲（13800000001）、住客乙（13800000002），密码统一 `Test1234`（如与预置冲突则顺延手机号）。
33	
34	**身份证测试数据**：合法示例 `110101199001011234`（18 位、末位可为数字或 X）；非法示例按各用例给出。
35	
36	---
37	
38	## 2. 账户与权限（AC-01~AC-04）
39	
40	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
41	| --- | --- | --- | --- | --- | --- |
42	| TC-A01 | 游客浏览房型并引导登录 | 未登录（无 token / 已清 localStorage） | ① 打开首页房型列表；② 查看房型名称、单价、介绍；③ 点击某房型"预订" | ①② 正常展示，无需登录；③ 被引导至登录页 | AC-01、UC-02 |
43	| TC-A02 | 注册成功并登录 | 手机号 13800000001 未注册 | ① 进入注册页，输入手机号、密码 `Test1234`，提交；② 用该手机号+密码登录 | ① 注册成功；② 登录成功，进入住客首页，后续请求携带 token | AC-02、UC-01 |
44	| TC-A03 | 重复手机号注册被拒 | 13800000001 已注册 | 用同一手机号再次注册 | 提示手机号已注册（错误码 1002），不创建新账号 | AC-02、UC-01 异常 |
45	| TC-A04 | 密码错误登录被拒 | 住客甲已注册 | 用正确手机号 + 错误密码登录 | 提示"手机号或密码错误"，不发放 token | UC-01 异常 |
46	| TC-A05 | 注册参数格式校验 | — | ① 手机号填 `123`（非手机号格式）提交；② 手机号或密码留空提交 | ① 提示手机号格式不正确；② 对应输入框提示"请输入手机号/密码"，均不发起有效注册 | BR-01 手机号格式、原型 P-C3/C4 |
47	| TC-A06 | 我的订单数据隔离 | 住客甲、乙各有至少 1 笔订单 | ① 住客甲登录，进入"我的订单"；② 直接调 API：`GET /api/orders/{住客乙的订单号}`（带甲的 token） | ① 仅展示甲的订单；② 接口拒绝访问（不返回乙的订单数据） | AC-03、技术方案 §5.4（按 token 中 user_id 过滤） |
48	| TC-A07 | 住客访问前台接口被拒 | 住客甲已登录 | 带甲的 token 调 `GET /api/admin/orders` | 返回 403，无订单数据泄露 | AC-04、技术方案 §5.4 |
49	| TC-A08 | 未登录访问需登录接口 | 未登录 | ① 调 `POST /api/orders`；② 调 `GET /api/orders/mine` | 均返回 401 | 技术方案 §5.4、权限矩阵 |
50	| TC-A09 | admin 登录前台后台 | 预置 admin 账号 | ① 用 admin + 预置密码登录；② 进入前台订单查询页 | ① 登录成功，角色为 ADMIN；② 可正常进入 `/admin` 各页面 | AC-04、UC-06 |
51	| TC-A10 | 登出后 token 失效 | 住客甲已登录 | ① 登出；② 用原 token 调 `GET /api/auth/me` | ① 登出成功；② 返回 401 | UC-01、技术方案 §5.4 |
52	
53	## 3. 浏览与预订（AC-05~AC-08、BR-01~BR-03）
54	
55	> 预订提交的并发控制用例（防超卖、并发抢最后一间）见 §8 TC-G01~G03，本节 TC-B12 覆盖单线程下的库存回滚。
56	
57	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
58	| --- | --- | --- | --- | --- | --- |
59	| TC-B01 | 按日期查询剩余可订数量 | 大床房 A 共 3 间，所选区间无订单 | 在房型列表选择入住 `D`、离店 `D+2`，查询 | 各房型展示剩余可订数量，大床房 A 显示 3 | AC-05、UC-02 |
60	| TC-B02 | 剩余为 0 展示不可订 | 大床房 A 在 `D~D+2` 已被订满 3 间（可通过先创建 3 笔订单构造） | 用相同日期区间查询可订状态 | 大床房 A 展示"不可订"，预订按钮不可点击/无法提交 | AC-05、UC-02 异常 |
61	| TC-B03 | 日期区间非法 | 住客甲已登录 | ① 离店日期 = 入住日期；② 离店日期早于入住日期 | 均无法提交，提示"离店日期必须晚于入住日期" | AC-06、BR-01 |
62	| TC-B04 | 入住日期早于当天 | 住客甲已登录 | 入住日期选择 `D-1` 提交预订 | 拒绝并提示，不生成订单 | BR-01 |
63	| TC-B05 | 创建预订成功（主线） | 住客甲已登录，大床房 A 在 `D~D+2` 可订，单价 P 元 | 选择大床房 A、入住 `D`、离店 `D+2`，填姓名"张三"、手机号 13900000003，提交 | ① 生成订单，订单号形如 `HR{下单日yyyyMMdd}-NNNN` 且全局唯一；② 状态"已确认"；③ 金额 = P × 2（接口返回"分"单位整数，页面展示元）；④ 订单含姓名与手机号；⑤ 该区间剩余可订数量 −1 | AC-07、BR-01/02/03/08、UC-03 |
64	| TC-B06 | 代订（住客信息与账号不一致） | 住客甲已登录（注册手机号 13800000001） | 创建订单时姓名/手机号填他人信息（如"李四"/13900000004） | 下单成功；前台按 13900000004 可查到该订单，按甲的注册手机号查不到 | BR-01 允许代订、UC-06 |
65	| TC-B07 | 预订必填项与手机号格式 | 住客甲已登录 | ① 姓名留空提交；② 手机号填 `12345` 提交 | 均拒绝并提示，不生成订单、不占库存 | BR-01 |
66	| TC-B08 | 晚数计算（连续多晚） | 住客甲已登录 | 预订入住 `D`、离店 `D+3` | 订单晚数 = 3，金额 = P × 3；占用库存的日期为 D、D+1、D+2 三天（离店日当天不占） | BR-01、BR-02 逐日口径 |
67	| TC-B09 | 房型调价不影响已生成订单 | 住客甲已创建订单 O1（金额 = 原单价 × 晚数） | ① 前台修改大床房 A 单价；② 查看 O1 详情；③ 用新价格再创建同区间订单 O2 | ② O1 金额不变；③ O2 按新单价计算 | BR-03 金额固化 |
68	| TC-B10 | 金额接口格式（分单位） | 大床房 A 单价含小数（如 288.50 元） | 创建 1 晚订单，检查 `POST /api/orders` 响应中的金额字段 | 接口金额为整数 `28850`（分），页面展示 `288.50`；无浮点误差 | 技术方案 §3.5 金额口径 |
69	| TC-B11 | 预订失败结果页 | 大床房 A 所选区间已订满，住客甲已登录 | 甲在确认页提交预订 | ① 跳转预订失败页（P-C6 失败态），提示"该房型所选日期已订满"（错误码 1001），提供"重新选择房型"入口；② 不生成订单；③ 该区间每日 `occupied_count` 与提交前完全一致（无任何库存扣减残留） | UC-03 异常、技术方案 §3.5、§5.1 |
70	| TC-B12 | 多晚订单中途订满，整单回滚 | 大床房 A：D、D+1 可订，D+2 已订满（即 D~D+3 区间整体不可订，但前两晚单独看可订）；住客甲已登录；记录提交前 D、D+1 的 `occupied_count` | 甲提交入住 D、离店 D+3 的预订（占用顺序按日期升序，D、D+1 会先占用成功，D+2 失败） | ① 预订失败，提示"该房型所选日期已订满"；② 不生成订单；③ D、D+1 的 `occupied_count` 恢复为提交前的值——已占用成功的日期行随事务一并回滚，不留库存扣减残留 | BR-02、技术方案 §5.1 第 3 步（整单回滚） |

</details>

#### Tool Call: Read (`docs/03-测试用例.md`)
<!-- call_id: tool_VAWP44kFTfH41mqAGRz3SFD3 -->
```json
{
  "line_offset": 70,
  "n_lines": 116,
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Read (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_VAWP44kFTfH41mqAGRz3SFD3 -->
70	| TC-B12 | 多晚订单中途订满，整单回滚 | 大床房 A：D、D+1 可订，D+2 已订满（即 D~D+3 区间整体不可订，但前两晚单独看可订）；住客甲已登录；记录提交前 D、D+1 的 `occupied_count` | 甲提交入住 D、离店 D+3 的预订（占用顺序按日期升序，D、D+1 会先占用成功，D+2 失败） | ① 预订失败，提示"该房型所选日期已订满"；② 不生成订单；③ D、D+1 的 `occupied_count` 恢复为提交前的值——已占用成功的日期行随事务一并回滚，不留库存扣减残留 | BR-02、技术方案 §5.1 第 3 步（整单回滚） |
71	| TC-B13 | 业务校验失败不占库存 | 住客甲已登录；记录大床房 A 当日 `occupied_count` | 分别提交非法请求：① 入住日期早于当天；② 离店 ≤ 入住；③ 手机号格式非法 | 均拒绝、`occupied_count` 不变——业务规则校验在库存占用之前，失败请求不进入库存环节 | BR-01、技术方案 §5.1（先校验后占用） |
72	| TC-B14 | 重复提交幂等（双击/网络重试） | 住客甲已登录，大床房 A 所选区间可订；确认页进入时生成幂等请求号 requestNo | ① 用同一 requestNo 连续提交两次完全相同的预订（模拟双击/超时重试）；② 不携带 requestNo 提交 | ① 两次返回同一订单号；数据库仅 1 笔订单，该区间每日 `occupied_count` 仅 +1（唯一索引 `uk_order_request_no` 兜底并发重复）；② 拒绝（参数校验），不生成订单、不占库存 | 技术方案 §5.1 幂等、BR-02 |
73	
74	## 4. 取消订单（AC-09、AC-10、BR-04）
75	
76	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
77	| --- | --- | --- | --- | --- | --- |
78	| TC-C01 | 已确认订单取消成功 | 住客甲有"已确认"订单 O1（区间 D~D+2，取消前该区间剩余量记为 N） | ① 订单详情点击取消；② 二次确认；③ 再查该区间可订数量 | ② 状态变"已取消"；③ 剩余可订数量 = N+1（库存释放） | AC-09、BR-04、UC-05 |
79	| TC-C02 | 入住日当天未入住仍可取消 | 订单 O1 入住日 = D，状态"已确认" | 在 D 当天取消 O1 | 取消成功（宽松规则） | BR-04 |
80	| TC-C03 | 已入住订单不可取消 | 订单 O2 状态"已入住" | ① 页面查看是否有取消入口；② 绕过页面直接调 `POST /api/orders/{O2}/cancel`（带本人 token） | ① 无取消入口；② 接口拒绝，状态不变，库存/房间不释放 | AC-10、UC-05 异常 |
81	| TC-C04 | 已完成/已取消订单不可取消 | 订单 O3"已完成"、O4"已取消" | 分别直接调取消接口 | 均拒绝（如提示"订单状态已变更，请刷新查看"），状态不变 | AC-10、AC-19 |
82	| TC-C05 | 越权取消他人订单 | 订单 O1 属住客甲 | 住客乙登录后直接调 `POST /api/orders/{O1}/cancel` | 拒绝，O1 状态不变 | AC-03、技术方案 §5.3（条件更新含 user_id） |
83	
84	## 5. 前台查询与办理入住（AC-11~AC-16、BR-05、BR-06）
85	
86	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
87	| --- | --- | --- | --- | --- | --- |
88	| TC-D01 | 按订单号/手机号查询订单 | admin 已登录；存在订单 O1（住客手机号 13900000003） | ① 按 O1 订单号查询；② 按 13900000003 查询；③ 不输入条件直接查询 | ①② 均能查到 O1 并可进入详情；③ 展示全部订单 | AC-11、UC-06 |
89	| TC-D02 | 办理入住成功（主线） | 订单 O1 状态"已确认"，入住日 = D，离店日 = D+2；大床房 A 有空闲房间 | ① 打开 O1 详情；② 登记合法身份证号，从下拉选择房间 A101；③ 确认入住 | ① 状态变"已入住"；② 订单上出现房间号 A101 与身份证号；③ A101 不再出现在任何订单的可分配房间列表中 | AC-12、AC-16、BR-05/06、UC-07 |
90	| TC-D03 | 入住窗口内晚到办理 | 订单 O1 入住日 = D，离店日 = D+3，状态"已确认"；当前日期 = D+1 | 办理入住 | 办理成功；订单区间与金额不变（不退差价） | BR-06 窗口、BR-03 晚到不退差价 |
91	| TC-D04 | 未到入住日办理被拒 | 订单 O1 入住日 = D+2，状态"已确认" | 在 D 当天办理入住 | 拒绝，提示"未到入住日期（入住日：X）" | AC-13、BR-06 |
92	| TC-D05 | 超过入住窗口办理被拒 | 订单 O1 入住日 = D-3，离店日 = D，状态"已确认"（超期未入住不做自动关闭） | 在 D 当天（= 离店日）办理入住 | 拒绝，提示"已超过可入住时间（离店日：X），请引导客人取消重订"；订单状态保持"已确认"，住客仍可取消 | AC-13、BR-06 及补充 |
93	| TC-D06 | 非已确认状态办理入住被拒 | 分别准备"已入住""已完成""已取消"订单各 1 笔 | ① 页面查看；② 直接调 `POST /api/admin/orders/{orderNo}/check-in` | ① 页面无"办理入住"操作区；② 接口拒绝，提示"该订单当前状态不可办理入住" | AC-14、AC-19 |
94	| TC-D07 | 身份证号格式校验 | 订单 O1 满足其他全部入住条件 | 分别提交：① 17 位数字；② 19 位数字；③ 含字母（非末位 X）；④ 留空 | 均拒绝，提示"身份证号格式不正确"；不入库、不分配房间 | AC-15、BR-06 |
95	| TC-D08 | 可分配房间列表不含在住房间 | 大床房 A 共 3 间，A101 已分配给在住订单 O1 | 对另一笔同房型"已确认"订单 O2 调 `GET .../assignable-rooms` 或查看房间下拉 | 列表仅含 A102、A103，不含 A101 | AC-16、BR-05 |
96	| TC-D09 | 无空闲房间办理入住被拒 | 大床房 A 全部 3 间均处于在住状态；存在该房型"已确认"订单 O4（如提前退房释放库存后再预订构造） | 对 O4 办理入住 | 拒绝，提示"该房型当前无空闲房间"，订单状态不变 | UC-07 异常、BR-05 |
97	| TC-D10 | 入住条件预检（打开详情即提示） | 分别准备：① 入住日 = D 的"已确认"订单 O1；② 入住日 = D+2 的"已确认"订单 O2；③ "已入住"订单 O3 | 打开各订单详情页（或调 `GET /api/admin/orders/{orderNo}/checkin-precheck`） | ① 预检通过，可展示办理操作区；② 不通过，原因"未到入住日期（入住日：X）"；③ 不通过，原因"该订单当前状态不可办理入住"；预检只读，不改变订单状态，原因文案与办理入住报错一致 | 原型 P-A3、BR-06 |
98	
99	## 6. 退房与终态（AC-17~AC-19、BR-07）
100	
101	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
102	| --- | --- | --- | --- | --- | --- |
103	| TC-E01 | 退房成功（主线） | 订单 O1 状态"已入住"，占用房间 A101 | ① 打开 O1 详情点击退房；② 二次确认；③ 查看同房型其他订单的可分配房间列表 | ② 状态变"已完成"；③ A101 重新出现在可分配房间列表中 | AC-17、BR-07、UC-08 |
104	| TC-E02 | 提前退房剩余晚数可再售 | 订单 O1 区间 D~D+3，状态"已入住"；在 D+1 退房；退房前 D+1~D+3 区间大床房 A 已订满 | ① 办理退房；② 立即用区间 D+1~D+2 查询可订数量并创建新订单 | ② 剩余可订数量 +1，新订单可创建成功（"已完成"订单不再占用库存） | BR-07、技术方案 §5.1 逐日释放 |
105	| TC-E03 | 提前退房不退差价 | 同 TC-E02 | 退房后查看 O1 详情 | 金额保持原价（单价 × 3 晚）不变 | BR-07 |
106	| TC-E04 | 非已入住状态退房被拒 | 分别准备"已确认""已完成""已取消"订单 | ① 页面查看；② 直接调 `POST /api/admin/orders/{orderNo}/check-out` | ① 页面无退房操作区；② 接口拒绝（如提示"订单状态已变更，请刷新"），状态不变 | AC-18、AC-19 |
107	| TC-E05 | 终态不可再流转 | 订单 O3"已完成"、O4"已取消" | 对 O3、O4 分别尝试：取消、办理入住、办理退房（直接调 API） | 全部拒绝，状态不变——终态无任何出口 | AC-19、PRD §6.2 |
108	
109	## 7. 基础数据与房型/房间维护（AC-20、AC-21、UC-09）
110	
111	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
112	| --- | --- | --- | --- | --- | --- |
113	| TC-F01 | 预置数据开箱可用 | 全新环境，`start.sh` 一键启动 | 启动后：① 未登录打开房型列表；② admin 登录后台 | ① 有 3~4 种房型、每个房型有若干房间，主链路可直接演示；② admin 可登录，无需手工录入数据 | AC-20 |
114	| TC-F02 | 初始化脚本幂等 | 系统已启动过一次 | 重启应用（`schema.sql`/`data.sql` 再次执行） | 启动不报错；预置房型/房间/admin 不重复；已有业务数据（订单等）不受影响 | 技术方案 §4.2 |
115	| TC-F03 | 新增房型同步住客侧 | admin 已登录 | ① 后台新增房型"套房 S"（单价 588.00）并为该房型新增 2 个房间；② 住客侧刷新房型列表并查询可订数量 | ② 列表出现"套房 S"，剩余可订数量为 2 | AC-21、UC-09 |
116	| TC-F04 | 修改房型信息同步 | 存在房型大床房 A | ① 后台修改名称/单价/介绍；② 住客侧查看 | 住客侧展示更新后的信息（已生成订单金额不受影响，同 TC-B09） | AC-21 |
117	| TC-F05 | 房型表单校验 | admin 已登录 | 新增/编辑房型时：① 名称或单价留空；② 单价填 0 或负数 | 提示"请填写完整的房型信息"，不保存 | 原型 P-A4 |
118	| TC-F06 | 房间号唯一性 | admin 已登录 | 新增/编辑房间：① 房间号留空；② 房间号与现有房间重复 | 提示"房间号不能为空且不可重复"，不保存 | 原型 P-A5 |
119	| TC-F07 | 减少房间低于有效订单数被拒 | 大床房 A 共 3 间，当前有效订单（已确认+已入住）= 3 | 尝试把房间数减到 2（删除/改挂 1 间到其他房型） | 拒绝，提示"该房型存在有效订单，请先处理相关订单"；房间数与库存不变 | UC-09 约束 |
120	| TC-F08 | 房间改挂双方校验与库存同步 | 大床房 A 有效订单 2 间、共 3 间；套房 S 存在 | 将 A103 改挂到套房 S：① 操作成功场景；② 再查 A、S 两房型未来日期的剩余可订数量 | ① 成功（A 剩余 2 ≥ 有效订单 2）；② A 的 total 变 2、S 的 total +1，剩余量随之变化；若改挂会使源房型房间数 < 有效订单数则同样按 TC-F07 拒绝 | UC-09、技术方案 §4.2 |
121	
122	## 8. 并发场景（AC-08、技术方案 §5）
123	
124	> 执行方式：优先以后端集成测试对 `InventoryService.tryOccupy` / `OrderService` 接口做多线程调用；也可在同一数据库上用多线程脚本并发打 `/api/orders`、`/api/admin/**` 接口。断言以**数据库终态**为准（库存行、订单数、状态），而非仅看接口返回值。
125	
126	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
127	| --- | --- | --- | --- | --- | --- |
128	| TC-G01 | 并发预订防超卖（核心） | 大床房 A 共 3 间，清空区间 D~D+2 的全部订单与库存行（剩余量 = 3） | 用 N（≥ 5）个不同住客账号同时提交同房型同区间预订 | ① 恰好 3 笔成功，其余全部失败并提示"该房型所选日期已订满"；② `daily_inventory` 中每日 `occupied_count` ≤ 3（不超卖）；③ 数据库中该区间有效订单数 = 3；④ 失败方无残留库存占用（事务整体回滚） | AC-08、BR-02、技术方案 §5.1 |
129	| TC-G02 | 并发预订仅剩 1 间 | 大床房 A 区间 D~D+2 仅剩 1 间可订 | 住客甲、乙同时提交该区间预订 | 一笔成功、一笔失败提示已订满；成功订单数不超过物理房间数 | AC-08、UC-03 异常 |
130	| TC-G03 | 并发预订 + 取消交叉 | 大床房 A 某区间仅剩 1 间；住客甲持有一笔该区间"已确认"订单 O1 | 同时发起：① 甲取消 O1；② 乙、丙各提交 1 笔该区间预订 | 终态自洽：每日 `occupied_count` 恒等于"已确认+已入住"订单覆盖数（不多不少）；乙丙的成功数不超过取消生效后释放出的可订量（取消先生效则两笔均可成功，否则至多 1 笔）；无超卖、无负库存、无残留占用 | BR-02/04、技术方案 §5.1 |
131	| TC-G04 | 并发分配同一房间 | 订单 O1、O2 均为大床房 A"已确认"且满足入住条件；A101 空闲 | 两个 admin 会话同时对 O1、O2 办理入住且都选 A101 | 一笔成功；另一笔失败并提示"房间刚被分配，请刷新重选"（或等价文案）；数据库中 A101 的在住订单恒为 1（生成列唯一索引兜底，不允许出现两笔） | AC-16、BR-05、技术方案 §5.3 |
132	| TC-G05 | 并发入住同一订单 | 订单 O1"已确认"且满足入住条件 | 两个 admin 会话同时对 O1 办理入住（选不同房间） | 仅一笔生效（状态条件更新影响行数为 1），另一笔提示状态已变更；O1 只分配 1 个房间 | 技术方案 §5.3 条件更新 |
133	| TC-G06 | 并发取消 + 入住同一订单 | 订单 O1"已确认"、入住日 = D | 同时发起：① 住客取消 O1；② admin 对 O1 办理入住 | 仅一个操作成功（带源状态条件更新保证），另一个提示"订单状态已变更，请刷新查看"；终态要么"已取消"要么"已入住"，不存在中间错乱 | 技术方案 §5.3、PRD §6.2 |
134	| TC-G07 | 订单号并发唯一 | 无 | 高并发（≥ 20 线程）同时创建订单 | 全部订单号全局唯一、均符合 `HR+日期+序号` 格式；允许跳号 | BR-08、技术方案 §5.2 |
135	
136	## 9. 接口与数据规范（技术方案 §3.5、§5.4）
137	
138	| 编号 | 用例标题 | 前置条件 | 测试步骤 | 预期结果 | 需求点 |
139	| --- | --- | --- | --- | --- | --- |
140	| TC-H01 | 统一响应体格式 | — | 抽查成功接口（如房型列表）与业务失败接口（如订满） | 成功返回 `{code:0, message:"ok", data:...}`；业务失败返回非 0 code 且 message 为原型文案（如"该房型所选日期已订满"）；HTTP 状态码仅用 200/401/403/500 | 技术方案 §3.5 |
141	| TC-H02 | 密码不明文存储 | 注册住客甲 | 直接查 `user` 表 | `password_hash` 为 BCrypt 密文，无明文密码 | PRD §8.2 |
142	| TC-H03 | 前端不传 userId 也正确归属 | 住客甲已登录 | 抓包构造 `POST /api/orders` / `GET /api/orders/mine`，请求体/参数中塞入他人 userId | 后端忽略前端传入的 userId，一律按 token 中 user_id 处理 | 技术方案 §5.4、AC-03 |
143	| TC-H04 | token 过期处理 | 住客甲 token 已过期（可改 `auth_token.expires_at` 构造） | 用过期 token 调需登录接口 | 返回 401，前端引导重新登录 | 技术方案 §5.4、§8 |
144	| TC-H05 | 主链路端到端冒烟 | 全新环境一键启动 | 注册 → 浏览 → 预订 → 我的订单 → admin 查到订单 → 办理入住 → 退房，全流程 UI 操作 | 全链路无阻断；每步结果反馈明确，关键提示（订满/不可取消/入住条件不满足）文案与原型一致 | PRD §8.1 |
145	
146	---
147	
148	## 10. 验收标准覆盖对照
149	
150	| 验收标准 | 对应用例 |
151	| --- | --- |
152	| AC-01 | TC-A01 |
153	| AC-02 | TC-A02、TC-A03、TC-A04 |
154	| AC-03 | TC-A06、TC-C05、TC-H03 |
155	| AC-04 | TC-A07、TC-A09 |
156	| AC-05 | TC-B01、TC-B02 |
157	| AC-06 | TC-B03（另 TC-B04 覆盖 BR-01 日期下界） |
158	| AC-07 | TC-B05（另 TC-B08、TC-B10 补充晚数与金额口径） |
159	| AC-08 | TC-G01、TC-G02（另 TC-G03 覆盖与取消交叉） |
160	| AC-09 | TC-C01、TC-C02 |
161	| AC-10 | TC-C03、TC-C04 |
162	| AC-11 | TC-D01 |
163	| AC-12 | TC-D02 |
164	| AC-13 | TC-D04、TC-D05 |
165	| AC-14 | TC-D06 |
166	| AC-15 | TC-D07 |
167	| AC-16 | TC-D02、TC-D08、TC-G04 |
168	| AC-17 | TC-E01 |
169	| AC-18 | TC-E04 |
170	| AC-19 | TC-C04、TC-D06、TC-E04、TC-E05 |
171	| AC-20 | TC-F01、TC-F02 |
172	| AC-21 | TC-F03、TC-F04（另 TC-F05~F08 覆盖 UC-09 维护约束） |
173	
174	**补充覆盖（非 AC 直接要求）**：BR-03 金额固化 TC-B09、晚到不退差价 TC-D03；BR-07 提前退房可再售/不退差价 TC-E02、TC-E03；BR-08 订单号并发唯一 TC-G07；代订 TC-B06；下单幂等 TC-B14；入住预检 TC-D10；库存回滚与先校验后占用 TC-B12、TC-B13；并发状态流转 TC-G05、TC-G06；统一响应/密码/token TC-H01~H04；端到端冒烟 TC-H05。
175	
176	---
177	
178	## 11. 已知简化与豁免说明
179	
180	以下行为按 PRD/技术方案为**已知简化，不作为缺陷**（面试被问及按文档口径回答）：
181	
182	- 超期未入住的"已确认"订单不自动关闭，住客可自行取消（BR-06 补充）。
183	- 超期未退房的"已入住"订单由前台人工退房兜底，无定时任务（技术方案 §8）。
184	- 订单号允许跳号（唯一性优先，§5.2）。
185	- 订单改期、在线支付、一单多间、浮动房价、会员/通知等不在本期范围（PRD §1.3），相关入口不存在即视为通过。

</details>

<details><summary>Thinking</summary>

Now make docs/03 edits:

1. Header: version v1.0→v1.1, date 2026-10-05, status 已确认/已修订，关联文档版本（01 v1.2, 02 → v1.4, 05 v1.1）. I need to know docs/02's version after my edits — I'll set v1.4 per summary. docs/05 version: unknown, maybe add v1.2? Keep it simple: update 关联 to 01(v1.2)、02(v1.4)、05(v1.2). Actually I haven't bumped docs/05 version header. Let me check docs/05 header quickly and bump it too.

2. TC-B02 title "剩余为 0 展示不可订" and expectation "不可订" — should also change to 已订满 per PRD wording change! Good catch. Change title and expectation.

3. TC-A11 new row after TC-A10.

4. TC-D01 ③ add 分页.

5. TC-D11 after TC-D10.

6. TC-F09 after TC-F08.

7. TC-G08 after TC-G07.

8. §1 测试层级 API 行： add note about MockMvc automation.

9. §10: AC-11 add TC-D11; AC-21 括注 F05~F09; 补充覆盖 additions.

Let me do these edits sequentially (same file, multiple Edit calls in one response not allowed for same file... actually rule says multiple Edit calls may run in one response only when they do not target the same file. So sequential responses for docs/03. That's many turns. Alternative: rewrite via several Edit calls across responses. Fine.

Edit 1: header.

</details>

#### Tool Call: Read (`docs/05-前端原型需求.md`)
<!-- call_id: tool_PuJxfSAnAg4tMCeCeXaRiv63 -->
```json
{
  "line_offset": 1,
  "n_lines": 12,
  "path": "docs/05-前端原型需求.md"
}
```

<details><summary>Tool Result: Read (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_PuJxfSAnAg4tMCeCeXaRiv63 -->
1	# 酒店预订系统 前端原型需求
2	
3	| 项目 | 内容 |
4	| --- | --- |
5	| 文档版本 | v1.1 |
6	| 文档日期 | 2026-10-04 |
7	| 关联文档 | [01-需求文档.md](01-需求文档.md)（功能与规则以此为准） |
8	| 说明 | 仅低保真原型：页面结构、字段、交互与状态；不涉及视觉样式与技术选型 |
9	
10	---
11	
12	## 1. 页面清单

</details>

<details><summary>Thinking</summary>

Continue: bump docs/05 header version.

</details>

#### Tool Call: Edit (`docs/05-前端原型需求.md`)
<!-- call_id: tool_xhSdJwEZ5NUNUSFt6kcOuj46 -->
```json
{
  "new_string": "| 文档版本 | v1.2 |\n| 文档日期 | 2026-10-05 |",
  "old_string": "| 文档版本 | v1.1 |\n| 文档日期 | 2026-10-04 |",
  "path": "docs/05-前端原型需求.md"
}
```

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_wQz5O5YKFjwvzNZisMmPkRsM -->
```json
{
  "new_string": "| 文档版本 | v1.1 |\n| 文档日期 | 2026-10-05 |\n| 文档状态 | 已修订（v1.1 补充第二轮评审修订用例） |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.2）、[02-技术方案.md](02-技术方案.md)（v1.4）、[05-前端原型需求.md](05-前端原型需求.md)（v1.2） |",
  "old_string": "| 文档版本 | v1.0 |\n| 文档日期 | 2026-10-04 |\n| 文档状态 | 待评审 |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[02-技术方案.md](02-技术方案.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/05-前端原型需求.md`)</summary>

<!-- call_id: tool_xhSdJwEZ5NUNUSFt6kcOuj46 -->
Replaced 1 occurrence in docs/05-前端原型需求.md

</details>

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_wQz5O5YKFjwvzNZisMmPkRsM -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

<details><summary>Thinking</summary>

Now docs/03 body edits, one at a time (same file). Next: §1 测试层级 API row note + TC-B02 wording + TC-A11. Do them sequentially.

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_LRoMIOWZn8PDXFnQ6PW2bohC -->
```json
{
  "new_string": "| API 测试 | curl / Postman 直接调 `/api/**` | 越权、参数校验、异常分支、绕过前端入口的操作（如 AC-10 \"通过其他途径取消\"）。TC-A03/A04/A07/A08/A10、TC-H01/H02/H04 已由 MockMvc 集成测试（`AuthApiTest`）自动化 |",
  "old_string": "| API 测试 | curl / Postman 直接调 `/api/**` | 越权、参数校验、异常分支、绕过前端入口的操作（如 AC-10 \"通过其他途径取消\"） |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_LRoMIOWZn8PDXFnQ6PW2bohC -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_IUxFOh6G2FBOXR54gkOJqkvX -->
```json
{
  "new_string": "| TC-A10 | 登出后 token 失效 | 住客甲已登录 | ① 登出；② 用原 token 调 `GET /api/auth/me` | ① 登出成功；② 返回 401 | UC-01、技术方案 §5.4 |\n| TC-A11 | admin 不可创建预订 | 预置 admin 已登录 | 带 admin 的 token 调 `POST /api/orders`（参数合法） | 返回 403，提示\"前台账号不可创建预订\"；不生成订单、不占库存 | PRD §2.2 权限矩阵、技术方案 §5.4 |",
  "old_string": "| TC-A10 | 登出后 token 失效 | 住客甲已登录 | ① 登出；② 用原 token 调 `GET /api/auth/me` | ① 登出成功；② 返回 401 | UC-01、技术方案 §5.4 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_IUxFOh6G2FBOXR54gkOJqkvX -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_7c2yGYv0wfCRFy9An4Si6kgK -->
```json
{
  "new_string": "| TC-B02 | 剩余为 0 展示已订满 | 大床房 A 在 `D~D+2` 已被订满 3 间（可通过先创建 3 笔订单构造） | 用相同日期区间查询可订状态 | 大床房 A 展示\"已订满\"，预订按钮不可点击/无法提交 | AC-05、UC-02 异常 |",
  "old_string": "| TC-B02 | 剩余为 0 展示不可订 | 大床房 A 在 `D~D+2` 已被订满 3 间（可通过先创建 3 笔订单构造） | 用相同日期区间查询可订状态 | 大床房 A 展示\"不可订\"，预订按钮不可点击/无法提交 | AC-05、UC-02 异常 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_7c2yGYv0wfCRFy9An4Si6kgK -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_JYWFJGUXxvps72IHyphTekgI -->
```json
{
  "new_string": "| ①② 均能查到 O1 并可进入详情；③ 展示全部订单（分页返回，每页 10 条） | AC-11、UC-06 |",
  "old_string": "| ①② 均能查到 O1 并可进入详情；③ 展示全部订单 | AC-11、UC-06 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_JYWFJGUXxvps72IHyphTekgI -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_O2OYkg9gW6sTytrB7H9YgPFx -->
```json
{
  "new_string": "预检只读，不改变订单状态，原因文案与办理入住报错一致 | 原型 P-A3、BR-06 |\n| TC-D11 | 订单列表分页 | 住客甲有 12 笔订单；admin 已登录 | ① 甲调 `GET /api/orders/mine?page=1&pageSize=10` 与 `page=2`；② admin 调 `GET /api/admin/orders?page=1&pageSize=10`；③ 调 `pageSize=51` 或 `page=0` | ① 第 1 页返回 10 条、第 2 页返回 2 条，`total`=12，按下单时间最新在前；② 分页结构同上（`{list,total,page,pageSize}`）；③ 拒绝，提示\"分页参数不正确\"（错误码 1004） | AC-11、技术方案 §6.1/6.2 |",
  "old_string": "预检只读，不改变订单状态，原因文案与办理入住报错一致 | 原型 P-A3、BR-06 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_O2OYkg9gW6sTytrB7H9YgPFx -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_6o33K5F9sJpj9M9iqqECLxBJ -->
```json
{
  "new_string": "若改挂会使源房型房间数 < 有效订单数则同样按 TC-F07 拒绝 | UC-09、技术方案 §4.2 |\n| TC-F09 | 在住房间禁止改挂 | 房间 A101 已分配给在住订单 O1 | 在房间管理中将 A101 改挂到其他房型 | 拒绝，提示\"该房间正在入住中，不可调整\"；A101 所属房型与双方库存不变 | UC-09、原型 P-A5 |",
  "old_string": "若改挂会使源房型房间数 < 有效订单数则同样按 TC-F07 拒绝 | UC-09、技术方案 §4.2 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_6o33K5F9sJpj9M9iqqECLxBJ -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_IHmQho83ZpTmoRGYY0TGsJUN -->
```json
{
  "new_string": "| TC-G07 | 订单号并发唯一 | 无 | 高并发（≥ 20 线程）同时创建订单 | 全部订单号全局唯一、均符合 `HR+日期+序号` 格式；允许跳号 | BR-08、技术方案 §5.2 |\n| TC-G08 | 并发减容维护 + 下单 | 大床房 A 共 3 间，区间 D~D+2 已有 2 笔\"已确认\"订单（占用 2/3） | 同时发起：① admin 将空闲房间 A103 改挂到其他房型（A 减容为 2）；② 住客提交该区间第 3 笔预订 | 恰一方成功：若改挂先完成则下单失败（已订满），若下单先占用则改挂被\"有效订单数\"校验拒绝；终态每日 `occupied_count` ≤ `total_count`，无超卖 | UC-09、BR-02、技术方案 §4.2/§5.1 |",
  "old_string": "| TC-G07 | 订单号并发唯一 | 无 | 高并发（≥ 20 线程）同时创建订单 | 全部订单号全局唯一、均符合 `HR+日期+序号` 格式；允许跳号 | BR-08、技术方案 §5.2 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_IHmQho83ZpTmoRGYY0TGsJUN -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

<details><summary>Thinking</summary>

Now §10 对照表 updates: AC-04 add TC-A11? TC-A11 maps to 权限矩阵 — add to AC-04 row. AC-11 add TC-D11. AC-21 括注 F05~F09. 补充覆盖行 additions.

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_BPni6XNw2YGoI3Mfqpdeb38T -->
```json
{
  "new_string": "| AC-04 | TC-A07、TC-A09、TC-A11 |",
  "old_string": "| AC-04 | TC-A07、TC-A09 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_BPni6XNw2YGoI3Mfqpdeb38T -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_ZOXaPmnS6HfHyYkKTsNHjSDJ -->
```json
{
  "new_string": "| AC-11 | TC-D01、TC-D11 |",
  "old_string": "| AC-11 | TC-D01 |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_ZOXaPmnS6HfHyYkKTsNHjSDJ -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

<details><summary>Thinking</summary>

Continue docs/03 edits: AC-21 row and 补充覆盖 line.

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_KIp1ErmV3sHZhxv0pK5dbN0k -->
```json
{
  "new_string": "| AC-21 | TC-F03、TC-F04（另 TC-F05~F09 覆盖 UC-09 维护约束） |",
  "old_string": "| AC-21 | TC-F03、TC-F04（另 TC-F05~F08 覆盖 UC-09 维护约束） |",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_KIp1ErmV3sHZhxv0pK5dbN0k -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

#### Tool Call: Edit (`docs/03-测试用例.md`)
<!-- call_id: tool_FYhtDkUmr1znNnczsMkcPWL4 -->
```json
{
  "new_string": "下单幂等 TC-B14；入住预检 TC-D10；订单列表分页 TC-D11；在住房间禁改挂 TC-F09；减容维护与下单并发 TC-G08；权限矩阵（admin 禁下单）TC-A11；库存回滚与先校验后占用 TC-B12、TC-B13；",
  "old_string": "下单幂等 TC-B14；入住预检 TC-D10；库存回滚与先校验后占用 TC-B12、TC-B13；",
  "path": "docs/03-测试用例.md"
}
```

<details><summary>Tool Result: Edit (`docs/03-测试用例.md`)</summary>

<!-- call_id: tool_FYhtDkUmr1znNnczsMkcPWL4 -->
Replaced 1 occurrence in docs/03-测试用例.md

</details>

<details><summary>Thinking</summary>

docs/03 done. Now docs/02 技术方案. Need to read relevant sections: §3.5, §4.2 (~247), §5.3 (~294), §5.4, §6.1/6.2, 修订记录. Read around those areas.

</details>

docs/03 完成。现在改 docs/02 技术方案。

#### Tool Call: Read (`docs/02-技术方案.md`)
<!-- call_id: tool_g9Wf2UzewcyMK20mgmSaPYsy -->
```json
{
  "line_offset": 230,
  "n_lines": 110,
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Read (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_g9Wf2UzewcyMK20mgmSaPYsy -->
230	
231	字符集 utf8mb4，InnoDB。表名避开关键字（订单表用 `hotel_order`）。
232	
233	### 4.1 表清单
234	
235	| 表 | 用途 | 关键字段与约束 |
236	| --- | --- | --- |
237	| `user` | 住客与 admin 账号（决策 11） | `login_name` 唯一（住客=手机号，admin="admin"）、`password_hash`（BCrypt）、`role`（GUEST/ADMIN）、`created_at` |
238	| `auth_token` | 登录 token（决策 6、12） | `token`（UUID）主键、`user_id`、`expires_at`、`created_at` |
239	| `room_type` | 房型 | `name`、`price` DECIMAL(10,2)、`description`、`created_at`；不提供删除（PRD 无下线需求） |
240	| `room` | 物理房间 | `room_no` 唯一、`room_type_id`、`created_at`；**无状态字段**，"空闲/入住中"由在住订单推导 |
241	| `hotel_order` | 订单 | `order_no` 唯一、`request_no` 唯一（客户端幂等请求号，防重复提交）、`user_id`（下单账号）、`guest_name` + `guest_phone`（住客信息，支持代订 BR-01）、`room_type_id`、`room_id`（入住前为 NULL）、`id_card`（入住前为 NULL）、`checkin_date`、`checkout_date`、`nights`、`amount` DECIMAL(10,2)（下单时固化 BR-03）、`status` 枚举字符串、`created_at`（下单时间，P-C8/P-A2 展示用）、`updated_at`；生成列 `active_room_id = IF(status='CHECKED_IN', room_id, NULL)` 加唯一索引，DB 级保证同一房间最多一笔已入住订单（BR-05，MySQL 唯一索引允许多个 NULL） |
242	| `daily_inventory` | 逐日库存（决策 5） | `room_type_id` + `stay_date` 唯一索引、`total_count`、`occupied_count` |
243	| `order_seq` | 订单号每日序号（决策 8） | `seq_date` 主键、`current_value` |
244	
245	### 4.2 关键设计说明
246	
247	- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。库存行在启动时与房型/房间变更时预创建（决策 13）：`INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的行，`total_count` 取当前房间数；UC-09 维护房间（增/减/改挂）时，先校验"新房间数 ≥ 当前有效订单数"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值（不存在的行随预创建一并补上）。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步。**
248	- **房间状态推导**：某房型当前空闲房间 = 该房型全部房间 − 被状态为"已入住"订单占用的房间。满足 BR-05"同一房间同一时间只分配给一笔已入住订单"，由"分配时房间行锁 + 生成列唯一索引"双重保证（见 §4.1、§5.3），也让 UC-09 的房间维护不受"房间状态"字段拖累。
249	- **订单状态**：Java 枚举 `CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED`，DB 存字符串，便于 SQL 直读与演示讲解。
250	- **初始化脚本幂等**：`schema.sql` 一律 `CREATE TABLE IF NOT EXISTS`，`data.sql` 用 `INSERT IGNORE`（或等价写法），应用重复启动不报错、预置数据不重复插入。
251	
252	---
253	
254	## 5. 核心机制设计
255	
256	### 5.1 防超卖（BR-02 / AC-08）
257	
258	创建订单先由 service 层做**业务规则校验**（BR-01：入住日期 ≥ 当天、离店日期 > 入住日期、住客姓名非空、手机号格式合法），校验不通过直接拒绝，不进入库存占用。
259	
260	创建预订**幂等**（防双击/网络重试重复下单）：前端每次进入预订确认页生成一个幂等请求号 `requestNo`（UUID）随单提交；service 先按 `request_no + user_id` 查重，命中则直接返回首次创建的订单（不重复生成订单号、不重复占库存）；并发下的重复插入由唯一索引 `uk_order_request_no` 兜底，撞索引的事务标记回滚（撤销已占库存与序号）后改查首次订单返回。
261	
262	库存行**预创建**（决策 13）：应用启动时与房型/房间变更时，按 `INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的逐日库存行（`total_count` 取当前房间数）。预创建保证下单热路径上库存行**总是存在**——这是并发安全的前提，原因见本节末尾。
263	
264	校验通过后在**一个事务**内，对日期区间 `[checkin, checkout)` 的每一天**按日期升序**执行（固定加锁顺序，降低并发事务死锁概率）：
265	
266	1. 原子条件更新：
267	   `UPDATE daily_inventory SET occupied_count = occupied_count + 1 WHERE room_type_id = ? AND stay_date = ? AND occupied_count < total_count`
268	2. 影响行数 = 1 → 该日占用成功；= 0 → 行存在但已订满，或超出预创建窗口（行不存在）。先用无锁一致性读区分：行不存在则经**独立小事务**懒建该行（`INSERT IGNORE`，立即提交），随后重试一次条件更新。
269	3. 重试后仍为 0 → 当日已订满，**抛业务异常、整单回滚**（已占用的日期行随事务一并回滚），返回"该房型所选日期已订满"。
270	4. 全部日期占用成功 → 插入订单，提交事务。
271	
272	并发正确性由两点保证：`occupied_count < total_count` 的条件更新在 MySQL 行锁下是原子的（不超卖）；`(room_type_id, stay_date)` 唯一索引防止库存行重复。
273	
274	**为什么不能在下单事务内建行**（v1.2 实测结论）：下单事务里 INSERT 库存行有两类并发死锁——① REPEATABLE READ 下对不存在的行做加锁 UPDATE 会产生间隙锁，并发 INSERT 同一间隙互相等待成环；② INSERT 撞唯一索引后 InnoDB 为重复键检查持有该记录的 S 锁直到事务结束，并发事务再请求 X 锁形成 S→X 升级死锁（READ COMMITTED 下同样存在）。预创建把"行存在"变成数据不变式后，占用路径只剩对已存在行的条件更新（纯记录锁），配合日期升序的固定加锁顺序，无死锁面；懒建兜底走独立小事务，同样不把 INSERT 引入下单事务。
275	
276	查询剩余量（无需事务）：区间内每日 `total_count − occupied_count` 的最小值；无库存行的日期按 `total_count = 当前房间数, occupied = 0` 计。
277	
278	取消（已确认 → 已取消）与提前退房（已入住 → 已完成）时，对订单区间每日执行 `occupied_count − 1`（带 `occupied_count > 0` 保护，同样按日期升序）。退房时剩余晚数立即可再售，正好由"逐日释放"天然满足（BR-07）。
279	
280	### 5.2 订单号生成（BR-08 / 决策 8）
281	
282	`OrderNoGenerator`（order 模块内部实现，不对外）：
283	
284	1. 事务内对 `order_seq` 当日行执行 `UPDATE ... SET current_value = current_value + 1 WHERE seq_date = 今天`，影响 0 行则 INSERT 初始行（撞主键则重试 UPDATE）；
285	2. 格式化 `HR + yyyyMMdd + '-' + 序号补零4位`，如 `HR20261003-0001`；
286	3. 与订单插入同一事务，序号不回滚复用（允许跳号，唯一性优先）。
287	
288	### 5.3 状态流转与条件更新
289	
290	所有状态变更用**带源状态条件的 UPDATE** 实现，杜绝"先查后改"的并发缝隙：
291	
292	- 取消：`UPDATE hotel_order SET status='CANCELLED' WHERE order_no=? AND status='CONFIRMED' AND user_id=?`，影响 0 行 → "订单状态已变更，请刷新查看"。
293	- 办理入住（一个事务内，按序执行）：
294	  1. BR-06 四项校验：订单状态 = 已确认、入住窗口 `入住日 ≤ 今天 < 离店日`、身份证 18 位格式、该房型有空闲房间；
295	  2. 对选中的房间行加行锁 `SELECT * FROM room WHERE id=? FOR UPDATE`——这是防止"同一房间被两笔订单并发分配"的关键：并发事务在此串行化；
296	  3. 锁内复查该房间无在住订单（`SELECT COUNT(*) FROM hotel_order WHERE room_id=? AND status='CHECKED_IN'`，> 0 则抛业务异常回滚，提示"房间刚被分配，请刷新重选"）；
297	  4. `UPDATE hotel_order SET status='CHECKED_IN', room_id=?, id_card=? WHERE id=? AND status='CONFIRMED'`，影响 0 行 → 订单状态已变更，回滚。
298	
299	  兜底：`hotel_order` 上的生成列唯一索引（§4.1）把"同一房间最多一笔已入住订单"固化为数据库约束，代码路径即使失守也写不进去。
300	- 办理退房：`UPDATE ... SET status='COMPLETED' WHERE order_no=? AND status='CHECKED_IN'`，随后释放库存（§5.1）。
301	
302	终态（已完成/已取消）无任何出口流转，与 PRD §6.2 一致。
303	
304	### 5.4 认证与鉴权
305	
306	- 注册：校验手机号格式与唯一性，BCrypt 加密入库。
307	- 登录：按 `login_name` 查用户、BCrypt 比对，成功则生成 UUID token 写入 `auth_token`（含过期时间，如 7 天），返回 token 与角色。
308	- `AuthInterceptor`：从 `X-Token` 解析用户并放入请求上下文；未登录访问需登录接口 → 401；住客访问 `/api/admin/**` → 403（AC-04）。
309	- 住客"我的订单"一律以 token 中的 `user_id` 过滤，不接受前端传 userId（AC-03）。
310	
311	---
312	
313	## 6. API 接口清单
314	
315	### 6.1 住客侧（`/api`，部分公开）
316	
317	| 方法 | 路径 | 说明 | 登录 |
318	| --- | --- | --- | --- |
319	| POST | `/api/auth/register` | 住客注册 | 否 |
320	| POST | `/api/auth/login` | 登录（住客/前台共用入口，按 role 区分） | 否 |
321	| POST | `/api/auth/logout` | 登出 | 是 |
322	| GET | `/api/auth/me` | 当前登录用户信息 | 是 |
323	| GET | `/api/room-types` | 房型列表（P-C1） | 否 |
324	| GET | `/api/room-types/{id}` | 房型详情（P-C2） | 否 |
325	| GET | `/api/room-types/availability?checkin=&checkout=` | 各房型日期区间剩余量 | 否 |
326	| POST | `/api/orders` | 创建预订（P-C5，入参含幂等请求号 requestNo） | 住客 |
327	| GET | `/api/orders/mine` | 我的订单列表（P-C7） | 住客 |
328	| GET | `/api/orders/{orderNo}` | 我的订单详情（P-C8） | 住客（仅本人） |
329	| POST | `/api/orders/{orderNo}/cancel` | 取消订单 | 住客（仅本人） |
330	
331	### 6.2 前台管理侧（`/api/admin`，仅 ADMIN 角色）
332	
333	| 方法 | 路径 | 说明 |
334	| --- | --- | --- |
335	| GET | `/api/admin/orders?keyword=` | 订单查询：按订单号或住客手机号（P-A2）；keyword 为空返回全部 |
336	| GET | `/api/admin/orders/{orderNo}` | 订单详情（P-A3） |
337	| GET | `/api/admin/orders/{orderNo}/assignable-rooms` | 该订单房型当前可分配房间列表 |
338	| GET | `/api/admin/orders/{orderNo}/checkin-precheck` | 入住条件预检（P-A3，返回 pass 与原因列表，只读） |
339	| POST | `/api/admin/orders/{orderNo}/check-in` | 办理入住（入参：身份证号、房间号） |

</details>

<details><summary>Thinking</summary>

Now edit docs/02. Changes:

1. §4.2 库存一致性段落：补建行前置独立小事务、减容守卫+复查、在住禁改挂。
2. §5.3 step 3 锁内复查改为独立小事务复查。
3. §5.4 补 admin 禁下单。
4. §3.5 时区（need to read §3.5 first — it's earlier in the file; find date line). Let me grep for it.
5. §6.1/6.2 订单列表分页。
6. 修订记录 v1.4 (need to read 修订记录 section, likely near top or bottom).

Let me read §3.5 and 修订记录 areas.

</details>

#### Tool Call: Grep (`docs/02-技术方案.md`)
<!-- call_id: tool_1JRFpm8acyYzCnnGdsAYpUmx -->
```json
{
  "-n": true,
  "output_mode": "content",
  "path": "docs/02-技术方案.md",
  "pattern": "修订记录|### 3.5|时区| LocalDate"
}
```

<details><summary>Tool Result: Grep (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_1JRFpm8acyYzCnnGdsAYpUmx -->
docs/02-技术方案.md:10:| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全）；v1.2：库存行改为启动/变更时预创建（决策 13，下单事务内不再建行）；v1.3：新增下单幂等（requestNo + `uk_order_request_no`）与入住预检接口；前端金额口径明确为"持分、仅渲染处格式化" |
docs/02-技术方案.md:213:### 3.5 前后端交互方式

</details>

#### Tool Call: Read (`docs/02-技术方案.md`)
<!-- call_id: tool_mrKeJwNsr9Q30EqzKxp9nXLd -->
```json
{
  "line_offset": 213,
  "n_lines": 18,
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Read (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_mrKeJwNsr9Q30EqzKxp9nXLd -->
213	### 3.5 前后端交互方式
214	
215	- **协议**：REST + JSON，全部业务接口挂在 `/api` 前缀下；前端构建产物由后端静态托管，同源部署无 CORS 问题（CORS 仅在 dev 模式放开）。
216	- **认证传递**：登录成功后后端返回 token，前端存入 `localStorage`，之后每次请求由 axios 拦截器放入请求头 `X-Token: <uuid>`；后端 `AuthInterceptor` 解析 token、注入当前用户，并按路径规则做角色鉴权（`/api/admin/**` 仅 admin 可用）。
217	- **统一响应体**：所有接口返回 `Result<T>`：
218	
219	  ```json
220	  { "code": 0, "message": "ok", "data": { } }
221	  ```
222	
223	  `code = 0` 成功；非 0 为业务错误码（错误码枚举集中在 `common/api`，如 `1001` 已订满、`1002` 手机号已注册、`1003` 不满足入住条件）；完整错误码表在编码阶段补全，前端一律以 `message` 为展示准绳，不对 code 做分支逻辑（预订失败跳结果页等少数流程除外）。HTTP 状态码仅用 200/401/403/500 表达传输层语义。
224	- **错误呈现**：`request.js` 统一拦截非 0 code，用 Element Plus `ElMessage` 弹出后端 `message`——后端业务异常的 message 直接采用原型文档中的提示文案（如"该房型所选日期已订满"），保证演示讲解与原型一致；页面可自行捕获做定制展示（如预订失败跳 P-C6）。
225	- **日期与金额的接口格式**：日期用 `yyyy-MM-dd` 字符串（LocalDate）。金额在前后端接口间一律以**"分"为单位的整数**传输（如 288.50 元 ↔ `28850`），避免 JS 浮点数精度问题；后端内部计算仍用 BigDecimal（单位元，DB 存 DECIMAL(10,2)），元 ↔ 分的换算收敛在 DTO 序列化层一处完成；前端全程以"分"整数持有数据，禁止在前端做换算与浮点运算，仅在模板渲染处调用统一格式化函数（`formatYuan`，分 ÷ 100 保留两位小数）展示。
226	
227	---
228	
229	## 4. 数据库设计
230	

</details>

<details><summary>Thinking</summary>

Now do docs/02 edits sequentially.

Edit 1: 修订记录 header add v1.4.

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_SgFidQ1ooLA03Jmbo0ZScJZr -->
```json
{
  "new_string": "| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全）；v1.2：库存行改为启动/变更时预创建（决策 13，下单事务内不再建行）；v1.3：新增下单幂等（requestNo + `uk_order_request_no`）与入住预检接口；前端金额口径明确为\"持分、仅渲染处格式化\"；v1.4：第二轮评审修订——UC-09 维护路径死锁修复（建行前置独立小事务）与减容守卫复查、在住房间禁改挂、入住复查改独立只读小事务命中生成列索引、admin 禁创建预订、订单列表分页、JVM 默认时区固定 Asia/Shanghai、异常兜底 4xx 兼容 |",
  "old_string": "| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全）；v1.2：库存行改为启动/变更时预创建（决策 13，下单事务内不再建行）；v1.3：新增下单幂等（requestNo + `uk_order_request_no`）与入住预检接口；前端金额口径明确为\"持分、仅渲染处格式化\" |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_SgFidQ1ooLA03Jmbo0ZScJZr -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

Also bump 文档版本 in header? Check lines 3-9 of docs/02. The 修订记录 line was at line 10. There's likely a 文档版本 row. Let me just also update it. Read top.

</details>

#### Tool Call: Read (`docs/02-技术方案.md`)
<!-- call_id: tool_YU7Ox0n2aCQ6vlm7YY2VH79j -->
```json
{
  "line_offset": 1,
  "n_lines": 11,
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Read (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_YU7Ox0n2aCQ6vlm7YY2VH79j -->
1	# 酒店预订系统 技术方案
2	
3	| 项目 | 内容 |
4	| --- | --- |
5	| 文档版本 | v1.2 |
6	| 文档日期 | 2026-10-04 |
7	| 文档状态 | 已定稿 |
8	| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |
9	| 输入 | 已确认的 8 项技术决策 + 5 项补充确认（见 §1.2、§1.3） |
10	| 修订记录 | v1.1：评审修订（房间并发分配机制、金额接口格式、启动链路、遗漏补全）；v1.2：库存行改为启动/变更时预创建（决策 13，下单事务内不再建行）；v1.3：新增下单幂等（requestNo + `uk_order_request_no`）与入住预检接口；前端金额口径明确为"持分、仅渲染处格式化"；v1.4：第二轮评审修订——UC-09 维护路径死锁修复（建行前置独立小事务）与减容守卫复查、在住房间禁改挂、入住复查改独立只读小事务命中生成列索引、admin 禁创建预订、订单列表分页、JVM 默认时区固定 Asia/Shanghai、异常兜底 4xx 兼容 |
11	

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_FtXndOSqHDJXAX7P0x8g1VAj -->
```json
{
  "new_string": "| 文档版本 | v1.4 |\n| 文档日期 | 2026-10-05 |\n| 文档状态 | 已定稿 |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.2）、[05-前端原型需求.md](05-前端原型需求.md)（v1.2） |",
  "old_string": "| 文档版本 | v1.2 |\n| 文档日期 | 2026-10-04 |\n| 文档状态 | 已定稿 |\n| 关联文档 | [01-需求文档.md](01-需求文档.md)（v1.1）、[05-前端原型需求.md](05-前端原型需求.md)（v1.1） |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_FtXndOSqHDJXAX7P0x8g1VAj -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_j4JV6GOj0dAiqkhCuvrDTeGn -->
```json
{
  "new_string": "- **日期与金额的接口格式**：日期用 `yyyy-MM-dd` 字符串（LocalDate）；JVM 默认时区在启动时固定为 `Asia/Shanghai`，与 DB 连接时区一致，\"今天\"的判定不受部署环境时区影响。金额在前后端接口间一律以**\"分\"为单位的整数**传输",
  "old_string": "- **日期与金额的接口格式**：日期用 `yyyy-MM-dd` 字符串（LocalDate）。金额在前后端接口间一律以**\"分\"为单位的整数**传输",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_j4JV6GOj0dAiqkhCuvrDTeGn -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

Now §4.2 库存一致性段落. Replace the bullet with expanded version.

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_7g36Zlxt3e5IWJVNeOEhz76e -->
```json
{
  "new_string": "- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。库存行在启动时与房型/房间变更时预创建（决策 13）：`INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的行，`total_count` 取当前房间数；UC-09 维护房间（增/减/改挂）时，先校验\"新房间数 ≥ 当前有效订单数\"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步；正在入住中的房间禁止改挂**（在住订单的房间归属不可漂移）。\n  - 维护路径的并发安全（v1.4 实测修订）：① 批量补建行必须先经**独立小事务**完成，再对库存行加锁/更新——否则维护事务对库存范围加锁时会 bleed 到相邻房型首行，把独立事务的 `INSERT IGNORE` 堵死直至锁等待超时；② 减容同步用带守卫的条件更新（`SET total_count = 新值 WHERE occupied_count <= 新值`），随后以加锁当前读复查\"total 未同步的行\"（被守卫跳过的行保留旧 total，普通 count 受外层事务早期 RR 读视图影响会漏行，必须 `FOR UPDATE`），有残留即判定存在并发占用、整体回滚，杜绝维护与下单并发导致的超卖。",
  "old_string": "- **库存与房间数的一致性**：`daily_inventory.total_count` 是该房型在 `stay_date` 当日的房间数快照。库存行在启动时与房型/房间变更时预创建（决策 13）：`INSERT IGNORE` 批量创建 `[今天, 今天+730 天)` 的行，`total_count` 取当前房间数；UC-09 维护房间（增/减/改挂）时，先校验\"新房间数 ≥ 当前有效订单数\"，再把 `stay_date ≥ 今天` 的库存行 `total_count` 同步为新值（不存在的行随预创建一并补上）。历史日期行不改。**将房间改挂其他房型时，源房型与目标房型都要做有效订单校验与库存行同步。**",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_7g36Zlxt3e5IWJVNeOEhz76e -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

Continue docs/02: §5.3 入住复查 step 3.

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_vU1kwmI1vWwGXp9ozeI8iOU8 -->
```json
{
  "new_string": "  3. 复查该房间无在住订单：由**独立只读小事务**（REQUIRES_NEW）按生成列 `active_room_id = 房间id` 查询（命中唯一索引的普通一致性读，> 0 则抛业务异常回滚，提示\"房间刚被分配，请刷新重选\"）。用独立事务是因为办理事务在拿房间行锁前已建立一致性读视图，同事务复查读到的是旧快照；不能用 `FOR UPDATE` 复查——对唯一索引空值加锁会产生间隙锁，两个并发事务互锁成死锁（v1.4 实测）；",
  "old_string": "  3. 锁内复查该房间无在住订单（`SELECT COUNT(*) FROM hotel_order WHERE room_id=? AND status='CHECKED_IN'`，> 0 则抛业务异常回滚，提示\"房间刚被分配，请刷新重选\"）；",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_vU1kwmI1vWwGXp9ozeI8iOU8 -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_jxLJC9Dd8QEKomQ4PPhyXcs2 -->
```json
{
  "new_string": "- 住客\"我的订单\"一律以 token 中的 `user_id` 过滤，不接受前端传 userId（AC-03）。\n- 创建预订仅住客角色可用（PRD §2.2 权限矩阵）：admin 调 `POST /api/orders` 返回 403\"前台账号不可创建预订\"。",
  "old_string": "- 住客\"我的订单\"一律以 token 中的 `user_id` 过滤，不接受前端传 userId（AC-03）。",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_jxLJC9Dd8QEKomQ4PPhyXcs2 -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_F4NvMtcn1vTVuYBfD85UPZ9u -->
```json
{
  "new_string": "| GET | `/api/orders/mine?page=&pageSize=` | 我的订单列表（P-C7）：分页，默认 page=1、pageSize=10（≤50），返回 `PageResult{list,total,page,pageSize}` | 住客 |",
  "old_string": "| GET | `/api/orders/mine` | 我的订单列表（P-C7） | 住客 |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_F4NvMtcn1vTVuYBfD85UPZ9u -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

#### Tool Call: Edit (`docs/02-技术方案.md`)
<!-- call_id: tool_kmfx3DSQjn3ioyT4wv7nZ1i5 -->
```json
{
  "new_string": "| GET | `/api/admin/orders?keyword=&page=&pageSize=` | 订单查询：按订单号或住客手机号（P-A2）；keyword 为空返回全部；分页同住客侧，返回 `PageResult` |",
  "old_string": "| GET | `/api/admin/orders?keyword=` | 订单查询：按订单号或住客手机号（P-A2）；keyword 为空返回全部 |",
  "path": "docs/02-技术方案.md"
}
```

<details><summary>Tool Result: Edit (`docs/02-技术方案.md`)</summary>

<!-- call_id: tool_kmfx3DSQjn3ioyT4wv7nZ1i5 -->
Replaced 1 occurrence in docs/02-技术方案.md

</details>

<details><summary>Thinking</summary>

docs/02 done. Now append revision entry to ai-process/编码任务清单.md. Format follows T10/T11 entries. Append at end? The file ends with 依赖关系 section. Better to append a new section at end: "## 第二轮评审修订（2026-10-05）". Read end already seen (lines 95-131). Append after line 131.

</details>

docs/02 完成。最后在任务清单追加本轮修订记录。

#### Tool Call: Edit (`ai-process/编码任务清单.md`)
<!-- call_id: tool_6A0a9mSOke1hN6YUiignCUSY -->
```json
{
  "new_string": "```\nT1 → T2 → T3 → T4（骨架）\nT3 → T5(auth) → T6(room 只读) → T7(inventory) → T8(order 住客侧) → T9(order 前台侧 + room 维护)\nT4 + T5~T8 → T10（住客端页面）；T4 + T9 → T11（管理端页面）；T10 + T11 → T12（联调收尾）\n```\n\n---\n\n## 第二轮评审修订（2026-10-05）\n\n对照 docs/01~03 全面 code review 后的修复轮，已全部完成：\n\n- **UC-09 维护路径**：批量补建行改前置独立小事务（修复维护×下单 50s 锁等待）；减容改\"occupied ≤ 新 total 守卫 UPDATE + total 未同步行 FOR UPDATE 复查\"；在住房间禁止改挂（\"该房间正在入住中，不可调整\"）；房间号重复捕获 DuplicateKeyException。\n- **入住复查**：改独立只读小事务（`CheckedInRechecker`），按生成列 `active_room_id` 命中唯一索引普通读，修复同事务脏快照与 FOR UPDATE 间隙锁死锁。\n- **权限/口径**：admin 禁创建预订（403\"前台账号不可创建预订\"）；JVM 默认时区固定 Asia/Shanghai；住客订单详情不再返回身份证号；异常兜底补 HttpMessageNotReadable/参数类型不匹配/DuplicateKey → 1004。\n- **订单列表分页**：住客 `GET /api/orders/mine` 与前台 `GET /api/admin/orders` 支持 page/pageSize（默认 1/10，≤50），返回 `PageResult{list,total,page,pageSize}`；前端我的订单页加翻页。\n- **测试**：新增 `AuthApiTest`（MockMvc 8 例：注册/登录/鉴权/token/统一响应体）、admin 禁下单、列表分页、住客详情不含 idCard、在住禁改挂（TC-F09）、减容维护×并发下单（TC-G08）等用例；surefire 显式禁并行。全量 `mvn test` 65 个用例通过。\n- **文档**：PRD v1.2（\"不可订\"→\"已订满\"）、技术方案 v1.4、测试用例 v1.1（TC-A11/D11/F09/G08）、原型 v1.2（分页与在住禁改挂文案）。",
  "old_string": "```\nT1 → T2 → T3 → T4（骨架）\nT3 → T5(auth) → T6(room 只读) → T7(inventory) → T8(order 住客侧) → T9(order 前台侧 + room 维护)\nT4 + T5~T8 → T10（住客端页面）；T4 + T9 → T11（管理端页面）；T10 + T11 → T12（联调收尾）\n```",
  "path": "ai-process/编码任务清单.md"
}
```

<details><summary>Tool Result: Edit (`ai-process/编码任务清单.md`)</summary>

<!-- call_id: tool_6A0a9mSOke1hN6YUiignCUSY -->
Replaced 1 occurrence in ai-process/编码任务清单.md

</details>

<details><summary>Thinking</summary>

Docs done. Now final verification: frontend build + concurrency tests rerun + E2E. Update todo.

Run npm run build (background? it's ~30s, run foreground with timeout 300). And mvn concurrency test. Do build first, and concurrently run mvn test for concurrency classes in background? Both are independent. Run npm build foreground and mvn in background simultaneously.

</details>

文档全部更新完毕。进入最终验证。

#### Tool Call: TodoList
<!-- call_id: tool_bVlk5hXgMJ3zr2I7jQTQunUJ -->
```json
{
  "todos": [
    {
      "status": "done",
      "title": "补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配"
    },
    {
      "status": "done",
      "title": "文档：PRD 已订满、分页/接口契约、测试用例"
    },
    {
      "status": "in_progress",
      "title": "mvn test + 前端 build + E2E 验证"
    }
  ]
}
```

#### Tool Call: Bash (`npm run build 2>&1 | tail -8`)
<!-- call_id: tool_PhSWkHerlV4l6w0jnm9IrRoF -->
```json
{
  "command": "npm run build 2>&1 | tail -8",
  "cwd": "hotel-web",
  "timeout": 240
}
```

#### Tool Call: Bash (`docker compose up -d && mvn test -Dtest='OrderConcurrencyTes…`)
<!-- call_id: tool_z62D0jHx72W17qY6V7y2OYCs -->
```json
{
  "command": "docker compose up -d && mvn test -Dtest='OrderConcurrencyTest,AdminConcurrencyTest' 2>&1 | tail -15",
  "cwd": "hotel-server",
  "run_in_background": true,
  "description": "并发测试复跑一轮",
  "timeout": 600
}
```

<details><summary>Tool Result: TodoList</summary>

<!-- call_id: tool_bVlk5hXgMJ3zr2I7jQTQunUJ -->
Todo list updated.
Current todo list:
  [done] 补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配
  [done] 文档：PRD 已订满、分页/接口契约、测试用例
  [in_progress] mvn test + 前端 build + E2E 验证

Ensure that you continue to use the todo list to track progress. Mark tasks done immediately after finishing them, and keep exactly one task in_progress when work is underway.

</details>

<details><summary>Tool Result: Bash (`npm run build 2>&1 | tail -8`)</summary>

<!-- call_id: tool_PhSWkHerlV4l6w0jnm9IrRoF -->
dist/assets/request-CvyG0myY.js                  52.10 kB │ gzip:  19.83 kB
dist/assets/index-DEIlHSmQ.js                 1,053.77 kB │ gzip: 345.28 kB

(!) Some chunks are larger than 500 kB after minification. Consider:
- Using dynamic import() to code-split the application
- Use build.rollupOptions.output.manualChunks to improve chunking: https://rollupjs.org/configuration-options/#output-manualchunks
- Adjust chunk size limit for this warning via build.chunkSizeWarningLimit.
✓ built in 1.67s


</details>

<details><summary>Tool Result: Bash (`docker compose up -d && mvn test -Dtest='OrderConcurrencyTes…`)</summary>

<!-- call_id: tool_z62D0jHx72W17qY6V7y2OYCs -->
task_id: bash-krhy8pnx
pid: 85127
description: 并发测试复跑一轮
status: running
automatic_notification: true
next_step: The completion arrives automatically in a later turn — do NOT wait, poll, or call TaskOutput on it; continue with your current work.
next_step: Use TaskStop only if the task must be cancelled.
human_shell_hint: The task is visible in the background-task panel.

</details>

<details><summary>Thinking</summary>

Frontend build passed. Concurrency tests running in background — I need the result before E2E (E2E starts the backend via spring-boot:run, which doesn't conflict with mvn test? Both hit the same MySQL — the E2E curl tests would add data to dev DB while tests run; tests create their own data but could interfere (e.g., availability counts). Better to wait for concurrency test to finish before starting E2E backend. I genuinely need its result, so WaitFor.

</details>

#### Tool Call: WaitFor (`bash-krhy8pnx`)
<!-- call_id: tool_UL3gNVU1UL7itS5SuQmQFbEl -->
```json
{
  "task_id": "bash-krhy8pnx",
  "timeout": 600
}
```

<details><summary>Tool Result: WaitFor (`bash-krhy8pnx`)</summary>

<!-- call_id: tool_UL3gNVU1UL7itS5SuQmQFbEl -->
wait_status: completed
task_id: bash-krhy8pnx
waited_ms: 1
timeout_ms: 600000

[finished]
Wall time: 7.249 seconds
task_id: bash-krhy8pnx
description: 并发测试复跑一轮
status: completed
detached: true
timeout_ms: 600000
kind: process
command: docker compose up -d && mvn test -Dtest='OrderConcurrencyTest,AdminConcurrencyTest' 2>&1 | tail -15
pid: 85127
exit_code: 0
parent_tool_call_id: tool_z62D0jHx72W17qY6V7y2OYCs
output_path: /Users/donglongjun/.kimi-code/sessions/wd_hotel-reservation-system_7d3e3acb70fb/session_6dc59ed8-1951-4b44-8be9-67c07ab4c87d/agents/main/tasks/bash-krhy8pnx/output.log
output_size_bytes: 1139
output_preview_bytes: 1139
output_truncated: false
full_output_available: true
full_output_tool: Read
full_output_hint: The preview above is the complete output. Use the Read tool with the output_path if you need to re-read the full log later (parameters: path, line_offset, n_lines; read about 300 lines per page).

[output]
 Container hotel-mysql Running 
2026-10-05T00:12:01.228+08:00  INFO 85159 --- [hotel-server] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection com.mysql.cj.jdbc.ConnectionImpl@5e278e25
2026-10-05T00:12:01.229+08:00  INFO 85159 --- [hotel-server] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-05T00:12:01.300+08:00  INFO 85159 --- [hotel-server] [           main] c.e.hotel.order.AdminConcurrencyTest     : Started AdminConcurrencyTest in 0.185 seconds (process running for 5.265)
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.730 s -- in com.example.hotel.order.AdminConcurrencyTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.338 s
[INFO] Finished at: 2026-10-05T00:12:01+08:00
[INFO] ------------------------------------------------------------------------


</details>

<details><summary>Thinking</summary>

Concurrency tests pass (7 total: OrderConcurrencyTest 4 + AdminConcurrencyTest 3). Now E2E: start backend in background, curl checks. Start mvn spring-boot:run in background, wait for startup, then curl.

</details>

#### Tool Call: Bash (`mvn spring-boot:run 2>&1 | tail -30`)
<!-- call_id: tool_kYGvEw9jdrdyZMk1VYXSOLmd -->
```json
{
  "command": "mvn spring-boot:run 2>&1 | tail -30",
  "cwd": "hotel-server",
  "description": "启动后端做 E2E 验证",
  "disable_timeout": true,
  "run_in_background": true
}
```

<details><summary>Tool Result: Bash (`mvn spring-boot:run 2>&1 | tail -30`)</summary>

<!-- call_id: tool_kYGvEw9jdrdyZMk1VYXSOLmd -->
task_id: bash-79d2w7xu
pid: 85203
description: 启动后端做 E2E 验证
status: running
automatic_notification: true
next_step: The completion arrives automatically in a later turn — do NOT wait, poll, or call TaskOutput on it; continue with your current work.
next_step: Use TaskStop only if the task must be cancelled.
human_shell_hint: The task is visible in the background-task panel.

</details>

#### Tool Call: Bash (`for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_cod…`)
<!-- call_id: tool_XlOnmJ4LhW01Dogk8lg7utij -->
```json
{
  "command": "for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/room-types 2>/dev/null | grep -q 200 && echo UP && break; sleep 2; done",
  "timeout": 90
}
```

<details><summary>Tool Result: Bash (`for i in $(seq 1 30); do curl -s -o /dev/null -w '%{http_cod…`)</summary>

<!-- call_id: tool_XlOnmJ4LhW01Dogk8lg7utij -->
UP


</details>

#### Tool Call: Bash (`ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth…`)
<!-- call_id: tool_pzGwi0dAtdkWHQZ1c82CN08x -->
```json
{
  "command": "ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"loginName\":\"admin\",\"password\":\"admin123\"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)[\"data\"][\"token\"])')\necho \"== 1. admin 调创建预订（期望 403 前台账号不可创建预订）\"\ncurl -s -X POST http://localhost:8080/api/orders -H \"X-Token: $ADMIN_TOKEN\" -H 'Content-Type: application/json' -d '{\"requestNo\":\"e2e-admin-001\",\"roomTypeId\":1,\"checkinDate\":\"2026-10-06\",\"checkoutDate\":\"2026-10-07\",\"guestName\":\"测试\",\"guestPhone\":\"13900000001\"}'; echo\necho \"== 2. admin 订单列表分页结构\"\ncurl -s \"http://localhost:8080/api/admin/orders?page=1&pageSize=10\" -H \"X-Token: $ADMIN_TOKEN\" | head -c 300; echo\necho \"== 3. pageSize=51（期望 1004 分页参数不正确）\"\ncurl -s \"http://localhost:8080/api/admin/orders?page=1&pageSize=51\" -H \"X-Token: $ADMIN_TOKEN\"; echo",
  "timeout": 60
}
```

<details><summary>Tool Result: Bash (`ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth…`)</summary>

<!-- call_id: tool_pzGwi0dAtdkWHQZ1c82CN08x -->
== 1. admin 调创建预订（期望 403 前台账号不可创建预订）
{"code":403,"message":"前台账号不可创建预订","data":null}
== 2. admin 订单列表分页结构
{"code":0,"message":"ok","data":{"list":[],"total":0,"page":1,"pageSize":10}}
== 3. pageSize=51（期望 1004 分页参数不正确）
{"code":1004,"message":"分页参数不正确","data":null}


</details>

<details><summary>Thinking</summary>

All three admin-side checks pass. Also check guest side /api/orders/mine pagination quickly with a registered guest. Let me do a quick guest register+login+mine check.

</details>

#### Tool Call: Bash (`PHONE=138$(printf '%08d' $RANDOM)
curl -s -X POST http://loc…`)
<!-- call_id: tool_CcERblRHMklGOfjxcBbA54pN -->
```json
{
  "command": "PHONE=138$(printf '%08d' $RANDOM)\ncurl -s -X POST http://localhost:8080/api/auth/register -H 'Content-Type: application/json' -d \"{\\\"phone\\\":\\\"$PHONE\\\",\\\"password\\\":\\\"Test1234\\\"}\" > /dev/null\nTOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d \"{\\\"loginName\\\":\\\"$PHONE\\\",\\\"password\\\":\\\"Test1234\\\"}\" | python3 -c 'import sys,json;print(json.load(sys.stdin)[\"data\"][\"token\"])')\necho \"== 4. 住客我的订单分页结构\"\ncurl -s \"http://localhost:8080/api/orders/mine?page=1&pageSize=10\" -H \"X-Token: $TOKEN\"; echo\necho \"== 5. 住客 page=0（期望 1004）\"\ncurl -s \"http://localhost:8080/api/orders/mine?page=0\" -H \"X-Token: $TOKEN\"; echo",
  "timeout": 60
}
```

<details><summary>Tool Result: Bash (`PHONE=138$(printf '%08d' $RANDOM)
curl -s -X POST http://loc…`)</summary>

<!-- call_id: tool_CcERblRHMklGOfjxcBbA54pN -->
== 4. 住客我的订单分页结构
{"code":0,"message":"ok","data":{"list":[],"total":0,"page":1,"pageSize":10}}
== 5. 住客 page=0（期望 1004）
{"code":1004,"message":"分页参数不正确","data":null}


</details>

#### Tool Call: TaskStop (`E2E 验证完成，停掉临时启动的后端`)
<!-- call_id: tool_3HafXrCNP6RQtshl47TfC2dY -->
```json
{
  "reason": "E2E 验证完成，停掉临时启动的后端",
  "task_id": "bash-79d2w7xu"
}
```

#### Tool Call: TodoList
<!-- call_id: tool_fWMW8SmxjOYznAMMu74L0nch -->
```json
{
  "todos": [
    {
      "status": "done",
      "title": "补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配"
    },
    {
      "status": "done",
      "title": "文档：PRD 已订满、分页/接口契约、测试用例"
    },
    {
      "status": "done",
      "title": "mvn test + 前端 build + E2E 验证"
    }
  ]
}
```

<details><summary>Tool Result: TaskStop (`E2E 验证完成，停掉临时启动的后端`)</summary>

<!-- call_id: tool_3HafXrCNP6RQtshl47TfC2dY -->
Wall time: 87.476 seconds
task_id: bash-79d2w7xu
status: killed
reason: E2E 验证完成，停掉临时启动的后端

</details>

<details><summary>Tool Result: TodoList</summary>

<!-- call_id: tool_fWMW8SmxjOYznAMMu74L0nch -->
Todo list updated.
Current todo list:
  [done] 补 HTTP/鉴权层 MockMvc 测试 + 既有测试适配
  [done] 文档：PRD 已订满、分页/接口契约、测试用例
  [done] mvn test + 前端 build + E2E 验证

Ensure that you continue to use the todo list to track progress. Mark tasks done immediately after finishing them, and keep exactly one task in_progress when work is underway.

</details>

第二轮评审修订全部完成，最终验证通过。

**文档更新（本轮收尾）**
- `docs/01-需求文档.md` v1.2：两处"不可订"统一为"已订满"（UC-02 异常、AC-05）
- `docs/05-前端原型需求.md` v1.2：P-C7/P-A2 补分页说明（每页 10 条），P-A5 补"在住房间改挂 → 该房间正在入住中，不可调整"
- `docs/03-测试用例.md` v1.1：新增 TC-A11（admin 禁下单）、TC-D11（订单列表分页）、TC-F09（在住禁改挂）、TC-G08（减容维护×并发下单）；TC-B02 文案同步"已订满"；§1 标注 AuthApiTest 已自动化的用例；§10 对照表同步
- `docs/02-技术方案.md` v1.4：§4.2 维护路径并发安全（建行前置独立小事务、减容守卫+加锁复查、在住禁改挂）、§5.3 入住复查改独立只读小事务、§5.4 admin 禁下单、§3.5 时区固定、§6.1/6.2 分页接口契约，修订记录追加 v1.4
- `ai-process/编码任务清单.md`：追加"第二轮评审修订"段落

**最终验证**
- 前端 `npm run build` ✓
- 并发测试复跑一轮：OrderConcurrencyTest + AdminConcurrencyTest 共 7 例全过（此前全量 65 例已绿）
- E2E curl（临时启动后端，已停）：admin 创建预订 → 403"前台账号不可创建预订" ✓；admin/住客订单列表返回 `PageResult{list,total,page,pageSize}` ✓；pageSize=51、page=0 → 1004"分页参数不正确" ✓

本轮所有改动（代码 + 测试 + 文档）尚未提交，建议 git 提交信息：

```
编码测试: 第二轮评审修订——UC-09并发修复、订单分页、鉴权与异常兜底、文档同步
```

需要我执行提交推送的话说一声即可。

## Turn 10

### User

更新进度 提交推送

### Assistant

<details><summary>Thinking</summary>

User wants progress update and commit+push. Check git status first, then commit with the message format and push.

</details>

#### Tool Call: Bash (`git status --short && git log --oneline -3`)
<!-- call_id: tool_vhNefCxFhYA0oHFP92qDPrhK -->
```json
{
  "command": "git status --short && git log --oneline -3"
}
```

<details><summary>Tool Result: Bash (`git status --short && git log --oneline -3`)</summary>

<!-- call_id: tool_vhNefCxFhYA0oHFP92qDPrhK -->
 M "ai-process/\347\274\226\347\240\201\344\273\273\345\212\241\346\270\205\345\215\225.md"
 M "docs/01-\351\234\200\346\261\202\346\226\207\346\241\243.md"
 M "docs/02-\346\212\200\346\234\257\346\226\271\346\241\210.md"
 M "docs/03-\346\265\213\350\257\225\347\224\250\344\276\213.md"
 M "docs/05-\345\211\215\347\253\257\345\216\237\345\236\213\351\234\200\346\261\202.md"
 M hotel-server/pom.xml
 M hotel-server/src/main/java/com/example/hotel/HotelApplication.java
 M hotel-server/src/main/java/com/example/hotel/auth/AuthService.java
 M hotel-server/src/main/java/com/example/hotel/common/exception/GlobalExceptionHandler.java
 M hotel-server/src/main/java/com/example/hotel/common/util/Validators.java
 M hotel-server/src/main/java/com/example/hotel/inventory/InventoryRowCreator.java
 M hotel-server/src/main/java/com/example/hotel/inventory/InventoryService.java
 M hotel-server/src/main/java/com/example/hotel/inventory/mapper/InventoryMapper.java
 M hotel-server/src/main/java/com/example/hotel/order/OrderAdminController.java
 M hotel-server/src/main/java/com/example/hotel/order/OrderController.java
 M hotel-server/src/main/java/com/example/hotel/order/OrderService.java
 M hotel-server/src/main/java/com/example/hotel/order/dto/OrderInfo.java
 M hotel-server/src/main/java/com/example/hotel/order/mapper/OrderMapper.java
 M hotel-server/src/main/java/com/example/hotel/room/RoomService.java
 M hotel-server/src/main/java/com/example/hotel/room/mapper/RoomMapper.java
 M hotel-server/src/test/java/com/example/hotel/order/OrderAdminServiceTest.java
 M hotel-server/src/test/java/com/example/hotel/order/OrderConcurrencyTest.java
 M hotel-server/src/test/java/com/example/hotel/order/OrderServiceTest.java
 M hotel-server/src/test/java/com/example/hotel/room/RoomAdminServiceTest.java
 M hotel-web/src/api/order.js
 M hotel-web/src/views/guest/MyOrdersView.vue
?? hotel-server/src/main/java/com/example/hotel/common/api/PageResult.java
?? hotel-server/src/main/java/com/example/hotel/order/CheckedInRechecker.java
?? hotel-server/src/test/java/com/example/hotel/auth/
fb3e848 编码测试: 评审修订——下单幂等、入住预检、前端金额口径持分
e431464 过程记录: 补充AI会话导出记录（T10住客端页面）
efbc3c4 编码测试: T10 住客端页面（P-C1~C8）与 availability 接口


</details>

#### Tool Call: Bash (`git add -A && git commit -m "编码测试: 第二轮评审修订——UC-09并发修复、订单分页、鉴…`)
<!-- call_id: tool_OjLVBS2ahZW4oN3ZclrbZvcQ -->
```json
{
  "command": "git add -A && git commit -m \"编码测试: 第二轮评审修订——UC-09并发修复、订单分页、鉴权与异常兜底、文档同步\" && git push",
  "timeout": 120
}
```

<details><summary>Tool Result: Bash (`git add -A && git commit -m "编码测试: 第二轮评审修订——UC-09并发修复、订单分页、鉴…`)</summary>

<!-- call_id: tool_OjLVBS2ahZW4oN3ZclrbZvcQ -->
[main aa4c2a0] 编码测试: 第二轮评审修订——UC-09并发修复、订单分页、鉴权与异常兜底、文档同步
 29 files changed, 694 insertions(+), 98 deletions(-)
 create mode 100644 hotel-server/src/main/java/com/example/hotel/common/api/PageResult.java
 create mode 100644 hotel-server/src/main/java/com/example/hotel/order/CheckedInRechecker.java
 create mode 100644 hotel-server/src/test/java/com/example/hotel/auth/AuthApiTest.java
To github.com:donglongjun886/hotel-reservation-system.git
   fb3e848..aa4c2a0  main -> main


</details>

已提交并推送：`aa4c2a0`（29 个文件，+694/−98），main 分支已同步到 GitHub。

**当前进度总览**

已完成：
- 第一、二阶段（需求/技术方案）及两轮 code review 修订
- 后端全部模块（auth / room / inventory / order），65 个测试全绿，含并发与 MockMvc 鉴权测试
- T10 住客端页面（P-C1~C8）✅ 已验收
- 第二轮评审修订（本轮提交）：UC-09 并发修复、在住禁改挂、入住复查优化、admin 禁下单、订单列表分页、时区/异常兜底、文档同步（PRD v1.2 / 技术方案 v1.4 / 测试用例 v1.1 / 原型 v1.2）

待做（按任务清单）：
- **T11 前台管理端页面**（P-A1~A5）：admin 登录、订单查询、订单详情（入住/退房办理）、房型维护、房间维护
- **T12 联调收尾**：start.sh 全链路实测、README、主链路 UI 冒烟

下一步就是 T11 前端管理端页面，要开始的话说一声。
