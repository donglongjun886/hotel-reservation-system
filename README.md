# 酒店预订系统

酒店管理演示系统，覆盖 **预订 → 订单查询 → 办理入住 → 退房** 的业务闭环，分住客端与前台管理端两个界面。

## 技术栈

- 后端：Spring Boot 3 + Java 17 + MyBatis-Plus（Maven 工程 `hotel-server/`）
- 前端：Vue 3 + Vite + Element Plus（`hotel-web/`，hash 路由，构建产物由后端 jar 托管）
- 数据库：MySQL 8（`docker-compose.yml`，容器端口映射 `3307:3306`）

## 前置条件

- JDK 17+
- Maven 3.6+
- Node.js 18+ 与 npm
- Docker（Docker Desktop 已启动）

## 启动方式

### 一键启动（推荐）

```bash
./start.sh
```

脚本依次完成：前置环境检测 → 前端构建 → 产物拷入后端 static → 后端打包 → 启动 MySQL 容器 → 启动应用。

启动后访问 **http://localhost:8080**（前端为 hash 路由，刷新不 404）：

- 住客端：首页房型列表，注册/登录后可预订
- 前台管理端：`http://localhost:8080/#/admin/login`

数据库脚本（`schema.sql` / `data.sql`）随应用启动自动执行，幂等（`CREATE TABLE IF NOT EXISTS` / `INSERT IGNORE`），重启不会重复初始化。

### 本地开发模式

```bash
docker compose up -d                      # 仅启动 MySQL
cd hotel-server && mvn spring-boot:run    # 后端，8080 端口
cd hotel-web && npm run dev               # 前端，5173 端口，/api 代理到 8080
```

数据库连接可用环境变量覆盖：`MYSQL_HOST`（默认 localhost）、`MYSQL_PORT`（默认 3307）、`MYSQL_USER`、`MYSQL_PASSWORD`。

## 预置账号与数据

- 前台管理员：**admin / admin123**
- 预置 4 个房型、10 个房间，开箱即可演示主链路

## 运行测试

```bash
docker compose up -d                      # 需先启动 MySQL
cd hotel-server && mvn test
```

集成测试覆盖接口鉴权、库存防超卖、订单全生命周期、并发场景（防超卖/并发入住/订单号唯一等）。

## 目录结构

```
hotel-server/        # 后端，按业务模块分包：auth / room / inventory / order
hotel-web/           # 前端，页面在 src/views/guest 与 src/views/admin
docs/                # 过程文档：需求、技术方案、测试用例、原型
docker-compose.yml   # MySQL 8
start.sh             # 一键构建并启动
```
