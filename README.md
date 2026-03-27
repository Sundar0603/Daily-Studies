# Daily Studies Tracker

A Vue 3 + Spring Boot (embedded Tomcat) + MySQL app that tracks daily study topic counts.
Each topic auto-increments its count for every day you didn't study it. Study it → decrement.

---

## Stack

| Layer    | Technology                        |
|----------|-----------------------------------|
| Frontend | Vue 3 (Options API), Vite, SCSS   |
| Backend  | Spring Boot 3, embedded Tomcat    |
| Database | MySQL 8                           |
| Java     | 17                                |

---

## One-time Setup

### 1. Database

```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

This creates the `daily_studies` database and seeds the 6 initial topics with their counts.

### 2. Backend credentials

Edit `backend/src/main/resources/application.properties` and update:

```properties
spring.datasource.username=<your-mysql-user>
spring.datasource.password=<your-mysql-password>
```

### 3. Frontend dependencies

```bash
cd frontend
npm install
```

### 4. Topic highlight images *(optional)*

Drop image files into `frontend/public/images/` matching the names in the table below.
The topic with the highest count will display its image as a background with a golden glow.

| Topic         | Image file            |
|---------------|-----------------------|
| Technical     | `technical.jpg`       |
| Non-Technical | `non-technical.jpg`   |
| Investments   | `investments.jpg`     |
| Security      | `security.jpg`        |
| AI            | `ai.jpg`              |
| Stocks        | `stocks.jpg`          |

---

## Running Locally

**Terminal 1 — backend:**
```bash
cd backend
mvn spring-boot:run
# Server starts on http://localhost:8086
```

**Terminal 2 — frontend:**
```bash
cd frontend
npm run dev
# Dev server starts on http://localhost:5173
```

Open **http://localhost:5173** in your browser.

---

## API Reference

| Method | Endpoint               | Body                          | Response            |
|--------|------------------------|-------------------------------|---------------------|
| GET    | `/api/topics`          | —                             | Array of topics     |
| PUT    | `/api/topics/{topic}`  | `{count, itemToStudyNext?}`   | Updated topic       |
| POST   | `/api/topics`          | `{topic}`                     | Created topic       |
| DELETE | `/api/topics/{topic}`  | —                             | 204 No Content      |

Topic JSON shape:
```json
{
  "topic": "AI",
  "count": 18,
  "itemToStudyNext": "Transformers paper",
  "lastUpdatedDate": "2026-03-19"
}
```

---

## How Auto-Increment Works

On every GET `/api/topics` call the backend computes:

```
daysMissed = today − lastUpdatedDate
```

If `daysMissed > 0`, that many days are added to `count` and `lastUpdatedDate` is reset to today.
After a PUT (i.e., you studied), `lastUpdatedDate` is set to today so the count won't grow again until tomorrow.

---

## Project Structure

```
Daily Studies/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── public/
│   │   └── images/          ← drop topic highlight images here
│   └── src/
│       ├── main.js
│       └── App.vue
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/dailystudies/
│       │   ├── DailyStudiesApplication.java
│       │   ├── config/CorsConfig.java
│       │   ├── controller/TopicController.java
│       │   ├── model/Topic.java
│       │   ├── repository/TopicRepository.java
│       │   └── service/TopicService.java
│       └── resources/
│           ├── application.properties
│           └── schema.sql
└── README.md
```
