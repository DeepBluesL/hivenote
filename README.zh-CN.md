# HiveNote

[English](README.md) | **简体中文**

基于 Vue 3 与 Spring Boot 的校园笔记分享与讨论平台，将学习资料、同学交流和内容审核整合在一起。

## 主要功能

- **学习资料：** 上传、下载笔记，设置封面和标签，按分类或学校浏览，发现热门、最新、精选和期末冲刺资料。
- **个人资料库：** 管理个人上传，点赞、收藏笔记，参与评论与回复。
- **社区讨论：** 发布图文帖子、回复讨论、点赞帖子。
- **账号管理：** 注册、密码登录、Google OAuth 登录、个人资料、头像、学校信息及联系方式展示设置。
- **管理后台：** 审核笔记、查看审核记录、设置推荐与帖子置顶、处理举报、管理用户封禁及头像。
- **辅助审核：** 通过 DeepSeek 根据笔记元数据生成审核建议，不读取上传文件的实际内容。

应用界面以中文为主；顶部语言链接仅切换项目文档。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vue Router 4、Element Plus、Axios、Vite 7、Sass |
| 后端 | Java、Spring Boot 3.1.5、MyBatis-Plus 3.5.3.1、Hutool、Lombok |
| 数据库 | MySQL |
| 文件存储 | 本地文件系统 |

## 本地运行

### 环境要求

- JDK 17，Maven 3.6.3 或更高版本。
- Node.js 20.19+ 或 22.12+，以及 npm，满足 Vite 7 的运行要求。
- MySQL 8.x。

### 1. 克隆仓库

```sh
git clone https://github.com/DeepBluesL/hivenote.git
cd hivenote
```

### 2. 初始化数据库

在项目根目录打开 MySQL 客户端：

```sh
mysql -u root -p
```

然后执行：

```sql
SOURCE hivenote.sql;
```

脚本会创建并选中 `hivenote` 数据库，导入表结构和示例数据。**请使用全新的开发数据库：脚本会删除并重建部分数据表。** 示例记录引用的上传文件不一定包含在本地安装中。

当前初始化脚本已包含封面、学校和隐私字段。全新导入后无需执行 `sql/` 下的历史迁移脚本，否则其中无条件执行的 `ADD COLUMN` 会导致重复字段错误。后端启动时，`DatabaseInitializer` 会补充其他缺失字段和表；数据库账号需要建表及修改表结构的权限，并应检查启动日志中的迁移错误。

### 3. 配置并启动后端

查看 [application.yml](back/src/main/resources/application.yml)。通过环境变量覆盖数据库账号密码，或在本地修改配置，但不要提交真实凭据。

在项目根目录执行以下 PowerShell 示例：

```powershell
$env:SPRING_DATASOURCE_USERNAME = "your_mysql_user"
$env:SPRING_DATASOURCE_PASSWORD = "your_mysql_password"
cd back
mvn spring-boot:run
```

Bash 用户可将 `$env:NAME = "value"` 改为 `export NAME="value"`。

后端地址为 `http://localhost:8080/zwz`，默认上传目录为 `${user.home}/hivenote/files/`。

### 4. 启动前端

另开一个终端，在项目根目录执行：

```sh
cd front
npm ci
npm run dev
```

访问 [http://localhost:5173](http://localhost:5173)，或 Vite 在终端中显示的实际地址。默认 API 地址为 `http://localhost:8080/zwz`，开发服务器也会将 `/zwz` 代理到 8080 端口。

注册账号即可体验普通用户流程。SQL 示例数据包含开发管理员 `admin` / `123456`，登录入口为 `/admin/login`；对外开放服务前请修改示例密码。

## 配置说明

| 环境变量 | 用途 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL JDBC 连接地址 |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |
| `FILE_UPLOAD_PATH` | 可写的上传目录，末尾需保留 `/` |
| `GOOGLE_CLIENT_ID` | 自行申请的 Google OAuth 客户端 ID |
| `GOOGLE_CLIENT_SECRET` | 自行申请的 Google OAuth 客户端密钥 |
| `GOOGLE_REDIRECT_URI` | 后端 OAuth 回调地址 |
| `GOOGLE_FRONTEND_URL` | OAuth 登录完成后跳转的前端来源地址 |
| `VITE_API_BASE` | Vite 在开发或构建时读取的前端 API 地址 |

本地使用 Google 登录时，请为自己的 OAuth 客户端配置回调地址 `http://localhost:8080/zwz/user/google/callback`，前端来源地址设为 `http://localhost:5173`。普通密码登录无需配置 Google OAuth。

如需覆盖前端 API 地址，可创建 `front/.env`（已被 Git 忽略）：

```dotenv
VITE_API_BASE=/zwz
```

修改后重启 Vite；生产构建需要重新构建才能应用环境变量变更。

普通文件上传支持 PDF、MP4、DOCX、ZIP、PNG、JPG/JPEG、GIF 和 WEBP，单文件上限为 50 MB。社区帖子图片的单独上限为 10 MB。

## 构建与部署

在 `front/` 目录执行：

```sh
npm run build
npm run preview
```

产物位于 `front/dist/`；`preview` 用于本地检查构建结果。

在 `back/` 目录执行：

```sh
mvn clean package
java -jar target/hivenote-backend-1.0.0.jar
```

如需启用 [application-prod.yml](back/src/main/resources/application-prod.yml)，在 Java 命令末尾添加 `--spring.profiles.active=prod`，并通过环境变量覆盖为自己的配置。该配置文件包含特定部署环境的数据库、存储路径与 OAuth 默认值。

前端托管服务需要将 Vue Router 的历史模式路由回退到 `index.html`。API 和文件请求应转发至后端，并保留 `/zwz` 上下文路径。如果对外使用 `/api/zwz` 前缀，反向代理需移除 `/api`，同时设置对应的 `VITE_API_BASE`。上传目录应可写且持久化保存。

## 目录结构

```text
hivenote/
├── README.md                 # 英文文档，仓库默认展示
├── README.zh-CN.md           # 中文文档
├── front/                   # Vue 前端
│   └── src/                 # 页面、组件、API 请求及路由
├── back/                    # Maven / Spring Boot 后端
│   └── src/main/
│       ├── java/com/hivenote/ # 控制器、服务、实体及配置
│       └── resources/       # 应用配置与迁移 SQL
├── hivenote.sql              # 初始化表结构与示例数据
└── sql/                     # 历史增量迁移脚本
```

## 当前限制

- 找回密码目前是前端模拟流程，尚未接入真实邮件找回服务。
- 登录会话保存在后端内存中，重启后需要重新登录。
- 源码目前包含内嵌的 Google OAuth 和 DeepSeek 凭据，部署前应更换并轮换这些凭据。DeepSeek 密钥硬编码在 `NoteController` 中，尚未实现环境变量配置。
- AI 建议只分析元数据，依赖有效的外部服务凭据，最终审核由管理员决定。
- 仓库目前未包含许可证文件。
