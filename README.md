# HiveNote

**English** | [简体中文](README.zh-CN.md)

A campus note-sharing and discussion platform built with Vue 3 and Spring Boot. HiveNote brings study resources, peer discussions, and content moderation into one application.

## Features

- **Study resources:** upload and download notes, add covers and tags, browse by category or school, and discover popular, recent, recommended, and exam-preparation resources.
- **Personal library:** manage uploads, like and bookmark notes, and join comment threads.
- **Community:** publish posts with images, reply to discussions, and like posts.
- **Accounts:** registration, password login, Google OAuth integration, profiles, avatars, school information, and contact-information visibility settings.
- **Administration:** approve or reject notes, maintain review history, manage recommendations and pinned posts, handle reports, and moderate users and avatars.
- **Review assistance:** a DeepSeek integration generates suggestions from note metadata; it does not inspect uploaded file contents.

The application interface is primarily Chinese. This language switch changes the documentation only.

## Technology

| Layer | Stack |
| --- | --- |
| Frontend | Vue 3, Vue Router 4, Element Plus, Axios, Vite 7, Sass |
| Backend | Java, Spring Boot 3.1.5, MyBatis-Plus 3.5.3.1, Hutool, Lombok |
| Database | MySQL |
| Storage | Local filesystem for uploaded files |

## Getting started

### Prerequisites

- JDK 17 and Maven 3.6.3 or later.
- Node.js 20.19+ or 22.12+ with npm, matching the Vite 7 engine requirement.
- MySQL 8.x.

### 1. Clone the repository

```sh
git clone https://github.com/DeepBluesL/hivenote.git
cd hivenote
```

### 2. Initialize MySQL

Open the MySQL client from the repository root:

```sh
mysql -u root -p
```

Then run:

```sql
SOURCE hivenote.sql;
```

The script creates and selects the `hivenote` database and imports sample data. **Use a fresh development database: the script drops and recreates several tables.** Sample records refer to uploaded files that may not be present in your local installation.

The current dump already includes cover, school, and privacy fields. Do not reapply the scripts in `sql/` to a fresh import; they are historical migrations and contain unconditional `ADD COLUMN` statements. On backend startup, `DatabaseInitializer` adds further missing fields and tables. The configured database user needs permission to create and alter tables; check startup logs for migration errors.

### 3. Configure and start the backend

Review [application.yml](back/src/main/resources/application.yml). Override database credentials using environment variables, or edit the configuration locally without committing credentials.

PowerShell example, from the repository root:

```powershell
$env:SPRING_DATASOURCE_USERNAME = "your_mysql_user"
$env:SPRING_DATASOURCE_PASSWORD = "your_mysql_password"
cd back
mvn spring-boot:run
```

For Bash, use `export NAME="value"` instead of `$env:NAME = "value"`.

The backend listens at `http://localhost:8080/zwz`. Uploads are stored under `${user.home}/hivenote/files/` by default.

### 4. Start the frontend

In a second terminal, from the repository root:

```sh
cd front
npm ci
npm run dev
```

Open [http://localhost:5173](http://localhost:5173), or the URL printed by Vite. The default API base is `http://localhost:8080/zwz`; the development server also proxies `/zwz` to port 8080.

Register an account to try the user workflow. The SQL seed includes a development administrator, `admin` / `123456`, accessible at `/admin/login`. Change the seeded password before exposing the application.

## Configuration

| Environment variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `FILE_UPLOAD_PATH` | Writable upload directory; include a trailing `/` |
| `GOOGLE_CLIENT_ID` | Your Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Your Google OAuth client secret |
| `GOOGLE_REDIRECT_URI` | Backend OAuth callback URL |
| `GOOGLE_FRONTEND_URL` | Frontend origin used after OAuth login |
| `VITE_API_BASE` | Frontend API base, read by Vite at development/build time |

For local Google login, configure your own OAuth client with callback `http://localhost:8080/zwz/user/google/callback` and frontend origin `http://localhost:5173`. Regular password login does not require Google OAuth setup.

For a frontend API override, create `front/.env` (ignored by Git):

```dotenv
VITE_API_BASE=/zwz
```

Restart Vite after changing environment values. A production build requires rebuilding to incorporate changes.

General uploads accept PDF, MP4, DOCX, ZIP, PNG, JPG/JPEG, GIF, and WEBP, up to 50 MB per file. Community post images have a separate 10 MB limit.

## Build and deployment

Frontend, from `front/`:

```sh
npm run build
npm run preview
```

The build output is `front/dist/`. `preview` is for checking the build locally.

Backend, from `back/`:

```sh
mvn clean package
java -jar target/hivenote-backend-1.0.0.jar
```

To select [application-prod.yml](back/src/main/resources/application-prod.yml), append `--spring.profiles.active=prod` to the Java command and supply your own environment overrides. That profile contains deployment-specific database, storage, and OAuth defaults.

When hosting the frontend, configure a fallback to `index.html` for Vue Router history routes. Route API and file requests to the backend, preserving its `/zwz` context path. If using an external `/api/zwz` prefix, strip `/api` in the reverse proxy and set `VITE_API_BASE` accordingly. Keep the upload directory persistent and writable.

## Repository layout

```text
hivenote/
├── README.md                 # English documentation
├── README.zh-CN.md           # Chinese documentation
├── front/                   # Vue application
│   └── src/                 # Views, components, API clients, routing
├── back/                    # Maven / Spring Boot application
│   └── src/main/
│       ├── java/com/hivenote/ # Controllers, services, entities, configuration
│       └── resources/       # Application settings and migration SQL
├── hivenote.sql              # Initial schema and sample data
└── sql/                     # Historical incremental migrations
```

## Current limitations

- Password recovery is a simulated frontend flow, not a working email recovery service.
- Login sessions live in backend memory; restarting the backend signs users out.
- The source currently contains embedded Google OAuth and DeepSeek credentials. Replace and rotate these credentials before deployment. The DeepSeek key is hardcoded in `NoteController`; no environment-based setting is implemented for it yet.
