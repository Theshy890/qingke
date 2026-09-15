# 🌿 青稞绿植 — 智慧绿植养护管理系统

> 一个面向绿植爱好者的全栈项目：植物知识科普、AI 识别与病害诊断、养护记录与提醒、社区互动，以及完整的后台管理系统。

### 项目缘起与定位

本项目最初按**商用上线**标准设计：完成了域名选型与备案方案调研，架构上按生产环境要求落地了 JWT 角色鉴权、敏感配置外置、AOP 操作审计、统一异常边界等工程实践。

在推进公开部署的过程中，对照《互联网信息服务算法备案管理规定》与《生成式人工智能服务管理暂行办法》逐项评估后确认：**生成式 AI 服务（病害诊断、AI 客服）与 UGC 模块（社区发帖、评论、私信）的备案主体仅限企业**，个人开发者无法完成合规手续。与其带着合规风险上线，我选择将项目定位调整为**本地运行的完整演示版（Demo）**——所有核心业务链路均可在本地复现，仅剥离了依赖企业资质的第三方服务环节。

### 功能范围调整说明

相比最初的商用蓝图，当前仓库移除了以下**必须企业资质或外部服务支撑**的环节，其余功能全部保留：

| 移除项 | 原设计 | 移除原因 | 现行替代 |
|---|---|---|---|
| 邮箱验证码注册 | `SysEmailVerifyCode` 实体 + 发送验证码接口 + spring-boot-starter-mail | 邮件外发需备案域名与邮箱资质 | 注册仅保留账号/密码/密保校验 |
| 提醒实时触达 | WebSocket 将到期提醒实时推送到在线用户浏览器 | 需 7×24 服务器值守，且主动推送属受限服务 | 提醒由 Quartz 生成后落入站内通知（sys_notice），用户登录后在提醒页查看；WebSocket 推送作为预留扩展点 |
| 第三方登录（微信等） | 微信扫码 / 公众号 OAuth 授权登录 | 开放平台仅对企业主体开放认证（需营业执照） | 账号密码 + JWT 登录 |
| AI 生成式服务上线 | 病害诊断与 AI 客服对外提供服务 | 算法备案主体仅限企业 | 代码完整保留，本地配置个人 API Key 即可运行 |
| 社区 UGC 上线 | 发帖/评论/关注/私信/访客 | 个人非经营性备案不得开展 UGC 业务 | 代码与后台审核模块完整保留，仅不对外部署 |

> 这段"做减法"的经历本身也是项目的一部分：**合规评估 → 架构取舍 → 无侵入降级**，是我在这个项目中获得的超出技术栈本身的收获。

## ✨ 核心功能

### 用户端

| 模块 | 说明 |
|---|---|
| 🌱 植物知识库 | 植物养护知识分类浏览、搜索、详情阅读，按热度（点击/收藏）排序 |
| 📷 AI 植物识别 | 拍照上传，智谱 GLM-4.6V-Flash 多模态大模型一步返回植物名称、简介与置信度（消耗积分，10 积分/次） |
| 🩺 AI 病害诊断 | 同一次识别中由大模型分析病害症状，给出诊断与养护建议（多 API Key 轮询池，失败自动退积分） |
| 💬 AI 客服助手 | 基于大模型的绿植养护问答，支持图片提问，聊天记录持久化 |
| ❤️ 收藏 / 👤 个人中心 | 知识收藏、个人资料、安全设置、积分签到 |
| 🔔 养护提醒 | 记录养护周期，Quartz 定时任务批量生成提醒，站内通知展示（提醒页） |
| 👥 社区 | 发帖、评论、点赞、关注、访客记录、私信（消息中心） |

### 管理端（后台）

- 用户管理、植物知识/分类的增删改查与审核
- 社区帖子/评论审核管理
- 系统公告发布、通知管理
- 数据统计面板（ECharts 可视化：用户增长、内容分布等）
- 操作日志（AOP 切面自动记录关键操作）

## 🛠 技术栈

### 前端（`Vue/`）

- **Vue 3.5** + **Vite 7** + Vue Router 4（Composition API）
- **Element Plus 2.11**（按需自动导入：unplugin-auto-import / unplugin-vue-components）
- **ECharts 5.5**（后台数据可视化）、ExcelJS（报表导出）
- Axios 封装（JWT 拦截器、401 统一处理、响应码拦截）
- Sass 样式 + 自定义卡通主题

### 后端（`Springboot/`）

- **Spring Boot 3.2**（Java 17）+ Spring Web + AOP
- **MyBatis-Plus 3.5** + PageHelper 分页
- **MySQL 8** + HikariCP
- **JWT**（jjwt 0.11）鉴权：签发携带角色声明的 Token，管理端敏感接口由 `AdminAuthInterceptor` 按路径清单拦截校验
- **Quartz** 定时任务（每小时从养护记录批量生成站内提醒）
- OkHttp + 智谱 GLM-4.6V-Flash API（多模态识别与诊断，多 Key 轮询 + 失败降级）
- Hutool、Lombok、统一响应体 `Result<T>`、全局异常处理

## 📁 项目结构

### 双端架构

同一套前后端服务承载两个独立入口，通过路由前缀 + JWT 角色声明分离：

```
                         ┌─────────────────────────┐
   用户端 /#/user/*  ──► │  Vue 3 SPA（同一应用）    │
   管理端 /#/admin/* ──► │  路由守卫按 role 分流登录  │
                         └───────────┬─────────────┘
                                     │ /api（Vite 代理 / 同域）
                         ┌───────────▼─────────────┐
                         │  Spring Boot 3.2        │
                         │  JwtUtil 签发含 role 的  │
                         │  Token；管理端敏感接口由  │
                         │  AdminAuthInterceptor    │
                         │  按路径清单拦截校验       │
                         └───────────┬─────────────┘
                                     │ MyBatis-Plus
                         ┌───────────▼─────────────┐
                         │  MySQL 8（qingke 库）    │
                         └─────────────────────────┘
```

- **用户端**：登录走 `/api/sys-user/app-login`，Token 存入 localStorage，可访问知识库、AI 识别、养护记录、社区等
- **管理端**：独立登录页（`AdminLogin.vue`），`WebConfig` 中 `AdminAuthInterceptor` 按路径白名单拦截用户管理、内容审核、操作日志等敏感接口，普通用户 Token 无法越权

```
qingke/
├── Springboot/                  # 后端服务
│   └── src/main/java/com/qingke/
│       ├── common/              # JWT、统一响应、全局异常、AOP 日志切面
│       ├── config/              # Quartz、MyBatis-Plus、AI 客户端等配置
│       ├── controller/          # REST 接口（用户端 + 管理端）
│       ├── entity/ mapper/      # MyBatis-Plus 实体与数据访问
│       ├── service/             # 业务逻辑
│       ├── interceptor/         # 管理端 JWT 鉴权拦截器
│       └── job/                 # Quartz 定时任务
├── Vue/                         # 前端应用
│   └── src/
│       ├── api/                 # 接口封装（axios）
│       ├── views/admin/         # 后台管理页面
│       ├── views/user/          # 用户端页面
│       ├── components/          # 通用组件（AI 客服、提醒弹窗等）
│       └── router/ store/       # 路由 / 全局状态
└── db/                          # 数据库建表脚本
```

> 根目录另含本说明文档 `README.md`：项目定位、功能范围调整说明、技术栈、快速启动与第三方服务申请指南。

## 🚀 快速启动

### 环境要求

- JDK 17+、Maven 3.6+、Node.js 18+、MySQL 8.x

### 1. 初始化数据库

```sql
CREATE DATABASE qingke DEFAULT CHARACTER SET utf8mb4;
```

导入 `db/qingke.sql` 建表脚本：

```bash
mysql -u root -p qingke < db/qingke.sql
```

### 2. 配置密钥

敏感配置不入库（见 `.gitignore`），有两种方式任选：

- **环境变量**：`MYSQL_PASSWORD`、`ZHIPU_API_KEY_1..5`、`JWT_SECRET`
- **本地文件**：在 `Springboot/src/main/resources/` 下创建 `application-local.yml`（该文件已被 git 忽略），覆盖对应配置项

### 3. 启动后端

```bash
cd Springboot
mvn spring-boot:run        # 默认端口 8080
```

### 4. 启动前端

```bash
cd Vue
npm install
npm run dev                # Vite 开发服务器，/api 自动代理到 8080
```

浏览器访问终端提示的地址即可。

## 🔑 第三方服务申请

| 服务 | 用途 | 申请地址 |
|---|---|---|
| 智谱 AI GLM-4.6V-Flash | 植物识别 / 病害诊断 / AI 客服 | https://open.bigmodel.cn/ |

> 有免费额度，注册创建应用后把 Key 填入第 2 步即可（支持配置多个 Key 轮询分摊限额）。

## 📄 License

本项目为个人学习与作品展示，代码可自由阅读参考。