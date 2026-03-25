# Netcafe Platform

网咖管理系统 Demo，包含 3 个子项目：

- `backend`：Spring Boot 后端
- `admin-frontend`：管理端前端
- `kiosk-frontend`：机位端前端

当前项目已经实现的接口文档见：

- [doc/后端已实现接口文档.md](./doc/后端已实现接口文档.md)

## 1. 环境要求

- JDK `17`
- Node.js `18+`
- npm `9+`
- Maven `3.9+`
- MySQL `8.x`

推荐本地开发环境：

- IntelliJ IDEA 或其他支持 Spring Boot 的 Java IDE
- VS Code 或 WebStorm 用于前端开发

## 2. 项目结构

```text
netcafe-platform/
├── backend/           后端 Spring Boot
├── admin-frontend/    管理端 Vue 3 + Vite
├── kiosk-frontend/    机位端 Vue 3 + Vite
├── doc/               项目文档
└── memory/            开发过程记忆文件
```

## 3. 默认端口

- 后端：`8080`
- 管理端：`5173`
- 机位端：`5174`
- MySQL：`3306`

## 4. 数据库准备

### 4.1 创建数据库

数据库名默认使用：

- `netcafe-platform`

可以先执行：

```sql
CREATE DATABASE IF NOT EXISTS `netcafe-platform`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;
```

### 4.2 导入表结构

项目表结构脚本在：

- [backend/sql/netcafe_schema.sql](./backend/sql/netcafe_schema.sql)

导入示例：

```bash
mysql -uroot -p netcafe-platform < backend/sql/netcafe_schema.sql
```

## 5. 后端配置说明

后端配置文件在：

- [backend/src/main/resources/application.yml](./backend/src/main/resources/application.yml)

当前默认配置：

- 服务端口：`8080`
- 数据库地址：`jdbc:mysql://localhost:3306/netcafe-platform`
- 数据库用户名：`netcafe_dev`
- 数据库密码：`netcafe123`
- JWT 密钥：`netcafe-local-dev-secret-change-me`

当前项目直接使用 `application.yml` 中的明文开发配置启动，不依赖环境变量。
如果你的本机数据库账号或密码不同，直接修改 `application.yml` 即可。

## 6. 启动后端

### 6.1 使用 Maven 启动

```bash
cd backend
mvn spring-boot:run
```

### 6.2 使用 IDEA 启动

启动主类：

- [backend/src/main/java/com/netcafe/platform/NetcafeApplication.java](./backend/src/main/java/com/netcafe/platform/NetcafeApplication.java)

启动前确认：

- Project SDK 是 `JDK 17`
- 运行配置使用的也是 `JDK 17`
- MySQL 已启动
- 数据库库名和账号密码已配置正确

### 6.3 后端启动验证

健康检查地址：

- [http://127.0.0.1:8080/actuator/health](http://127.0.0.1:8080/actuator/health)

如果启动成功，通常会返回：

```json
{"status":"UP"}
```

## 7. 启动管理端

```bash
cd admin-frontend
npm install
npm run dev
```

启动后访问：

- [http://localhost:5173/admin/login](http://localhost:5173/admin/login)

当前管理端 `Vite` 已配置代理，会把以下接口前缀转发到后端 `8080`：

- `/auth`
- `/admins`
- `/users`
- `/machines`
- `/sessions`
- `/session-orders`
- `/stats`
- `/system`
- `/audit`
- `/machine-templates`
- `/recharges`

代理配置文件：

- [admin-frontend/vite.config.ts](./admin-frontend/vite.config.ts)

## 8. 启动机位端

```bash
cd kiosk-frontend
npm install
npm run dev
```

启动后访问：

- [http://localhost:5174/login](http://localhost:5174/login)

机位端 `Vite` 已配置 `/kiosk` 代理到后端 `8080`。

代理配置文件：

- [kiosk-frontend/vite.config.ts](./kiosk-frontend/vite.config.ts)

## 9. 本地联调顺序

推荐按下面顺序启动：

1. 启动 MySQL
2. 导入 `backend/sql/netcafe_schema.sql`
3. 启动后端 `backend`
4. 启动管理端 `admin-frontend`
5. 启动机位端 `kiosk-frontend`

## 10. 热更新说明

- 后端：开启了 `Spring Boot DevTools`，修改代码后会触发热重启
- 管理端：`Vite` 自带热更新
- 机位端：`Vite` 自带热更新

注意：

- 后端更准确地说是“热重启”，不是完全无感的热替换
- 前端页面样式和脚本改动通常可以直接热更新

## 11. 常用测试入口

管理端：

- 登录页：[http://localhost:5173/admin/login](http://localhost:5173/admin/login)
- 仪表盘：[http://localhost:5173/admin/dashboard](http://localhost:5173/admin/dashboard)

机位端：

- 登录页：[http://localhost:5174/login](http://localhost:5174/login)
- 状态页：[http://localhost:5174/status](http://localhost:5174/status)
- 结算页：[http://localhost:5174/checkout](http://localhost:5174/checkout)

## 12. 常用测试账号

管理员测试账号：

- 超级管理员：`superadmin_test / 123456`

机位端测试示例：

- 机位编号：`A-027`
- 可正常上机身份证：`320101199605066666`

## 13. 常见问题

### 13.1 后端启动失败，提示 Java 版本不对

原因通常是当前终端默认 JDK 不是 `17`。

处理方式：

- 检查 `java -version`
- 检查 IDEA 的 Project SDK
- 确保运行配置绑定的是 `JDK 17`

### 13.2 后端启动失败，提示数据库连接失败

优先检查：

- MySQL 是否已启动
- 数据库 `netcafe-platform` 是否存在
- 用户名和密码是否正确
- `application.yml` 是否配置正确

### 13.3 前端接口 404 或 代理失败

优先检查：

- 后端是否已经启动在 `8080`
- 前端是否用的是 `npm run dev`
- `vite.config.ts` 代理配置是否被改动

### 13.4 页面能打开但没有数据

优先检查：

- 数据库是否已经导入表结构
- 是否已经准备测试数据
- 当前访问的接口是否已在 [doc/后端已实现接口文档.md](./doc/后端已实现接口文档.md) 中实现

## 14. 补充说明

- 这是一个 Demo 项目，目标是支撑前端展示和基础业务联调
- 当前后端已实现员工管理、用户管理、机位管理、上机管理、系统设置、审计日志、订单与充值、运营统计、机位端自助上机等模块
- 若后续涉及新增表或字段，优先通过数据库工具直接执行；若不方便，可补充 SQL 脚本到项目中再手动执行
