# Event Platform

This is a full-stack Event Management Platform built with **Spring Boot** (Backend), **Next.js** (Frontend), and **PostgreSQL** (Database).

_(Note: This project is in active development. This README will be updated as new features are added.)_

## 🏗️ Architecture Overview

The platform is built on a modern, containerized full-stack architecture:

| Component | Technology | Primary Role |
| :--- | :--- | :--- |
| **Frontend** | Next.js, Tailwind CSS | React SPA for stateless UI rendering, client-side routing, and file validation. |
| **Backend** | Spring Boot 3, Java 21 | Core REST API handling business logic, CSV ingestion, auth, and event state. |
| **Database** | PostgreSQL | Relational storage utilizing `JSONB` for dynamic, schema-less event configurations. |
| **Infrastructure**| Docker Compose | Orchestrates the entire stack, ensuring secure communication on an isolated network. |

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PLATFORM ARCHITECTURE                             │
│                                                                             │
│  ┌──────────────────┐             HTTP / REST         ┌──────────────────┐  │
│  │   Next.js SPA    │ ──────────────────────────────► │    Spring Boot   │  │
│  │   (Frontend)     │                                 │     Backend      │  │
│  │                  │ ◄─────────── JSON ───────────── │ (Java + Maven)   │  │
│  │ ├─ Participant UI│                                 │                  │  │
│  │ │  └─ Live Radar │                                 │ ├─ Auth & JWT    │  │
│  │ ├─ Judge Portal  │                                 │ ├─ Event State   │  │
│  │ ├─ Mentor Center │          Multipart/Form         │ ├─ CSV Ingestion │  │
│  │ ├─ Admin / CSV   │ ──────────────────────────────► │ ├─ Evaluations   │  │
│  │ ├─ Leaderboard   │                                 │ ├─ Math Brain    │  │
│  │ └─ Team Audits   │                                 │ └─ Mentor Engine │  │
│  └──────────────────┘                                 │    └─ Auto-Cancel│  │
│           ▲                                           └────────┬─────────┘  │
│           │                                                    │            │
│      User Browser                                              │            │
│      (Outside Docker)                                  JPA / Hibernate      │
│                                                                │            │
│                                                                ▼            │
│                                                       ┌──────────────────┐  │
│                                                       │    PostgreSQL    │  │
│                                                       │     Database     │  │
│                                                       │ ├─ Users         │  │
│                                                       │ ├─ Events (JSONB)│  │
│                                                       │ ├─ Registrations │  │
│                                                       │ │  └─ total_score│  │
│                                                       │ ├─ Submissions   │  │
│                                                       │ │  └─ avg_score  │  │
│                                                       │ ├─ Evaluations   │  │
│                                                       │ │  └─ eval_audits│  │
│                                                       │ ├─ MentorProfiles│  │
│                                                       │ └─ MentorSessions│  │
│                                                       └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Installation & Setup

This project uses Docker Compose for a seamless setup experience. You do not need to install Java, Node.js, or PostgreSQL locally.

1. Clone the repository and navigate to the root directory.
2. Build and start the entire stack using Docker:

```bash
docker-compose up -d --build
```

This will spin up three containers:

- `event_db`: PostgreSQL Database (Port 5433)
- `event_backend`: Spring Boot API (Port 8080)
- `event_frontend`: Next.js App (Port 3000)

To stop the application:

```bash
docker-compose down
```

_(Note: Use `docker-compose down -v` if you need to completely wipe the database volume and start fresh)._

---

## Database Handling

The PostgreSQL database runs inside a Docker container. We expose it on port `5433` to prevent conflicts with any native PostgreSQL instances running on your Mac.

**Connect to the Database via Terminal (psql):**

```bash
psql -h 127.0.0.1 -p 5433 -U admin -d event_platform
```

_Password:_ `adminpassword`

**Common PostgreSQL DB Commands once connected:**

- `\dt` - List all tables
- `\q` - Exit the database
- `SELECT * FROM registrations;` - View all registered teams

---

## Database Architecture

<img width="928" height="526" alt="Screenshot 2026-06-19 at 04 11 11" src="https://github.com/user-attachments/assets/1d95731e-aabb-47f6-ac18-c6064f0862a7" />

---

## Backend API Routes

The Spring Boot backend is served at `http://localhost:8080`. All API routes are prefixed with `/api`.

### Admin Routes (`UserController`)

- **`POST /api/user/login`**
  - **Purpose:** Authenticates staff/admins.
  - **Payload:** `{ "username": "...", "passwordHash": "..." }`
  - **Returns:** A JSON object containing a secure JWT token (`{ "token": "..." }`).

- **`POST /api/user/import`**
  - **Purpose:** Ingests the Unstop CSV file to bulk-register teams.
  - **Payload:** `multipart/form-data` containing the CSV `file` and `eventId`.
  - **Behavior:** Parses the CSV, extracts dynamic `member_details`, generates unique `team_passcode`s, and securely Upserts the records into the database.

----

### Team Routes (`RegistrationController`)

- **`POST /api/login`**
  - **Purpose:** Authenticates participating teams into their dashboard.
  - **Payload:** `{ "teamName": "...", "teamPasscode": "..." }`
  - **Returns:** A JSON object containing a secure JWT token (`{ "token": "..." }`).

---

### Submission Progression (`SubmissionController`)

*(Requires `Authorization: Bearer <token>` Header. Protected by `JwtAuthFilter`)*

- **`GET /api/status`**
  - **Purpose:** Automatically calculates a team's current allowed Feature and Round based on their approved submission history.
  - **Returns:** `TeamStatusResponse` (`{ "allowedTaskId": "FEATURE-1", "allowedRound": 1, "isPending": false }`).
  - **Security:** Prevents teams from manually selecting their round on the frontend. The backend dictates the timeline.

- **`POST /api/submit`**
  - **Purpose:** Submits a project repository and description for judging.
  - **Security:** Validates the requested `taskId` and `roundNumber` strictly against the `TeamStatusResponse`. Rejects illegal progression attempts with a `403 Forbidden`.

----

### Admin Dashboard & Global Controls (`AdminController`)

*(Requires `Authorization: Bearer <token>` Header. Protected by `JwtAuthFilter`)*

- **`GET /api/admin/events/{id}`**
  - **Purpose:** Fetches the configuration for a specific event (e.g., the JSON roadmap).
- **`PUT /api/admin/events/{id}/round/{round}`**
  - **Purpose:** Admin command to increase or reset the Global Ceiling (current event round).
  - **Security:** Validates against the `total_rounds` config to prevent illegal ceiling values.
- **`PUT /api/admin/events/{id}/meeting-team`**
  - **Purpose:** Sets the `active_meeting_team_id` in the event config. Automatically provisions a PENDING `ROUND-3` submission for that team so judges can grade their live presentation.
- **`GET /api/admin/leaderboard`**
  - **Purpose:** Fetches the dynamically sorted global leaderboard utilizing JPQL and the `total_score` field on `Registration`.
- **`GET /api/admin/teams/{id}`**
  - **Purpose:** Deep-dive audit for a specific team, returning their fully parsed JSON `member_details`, credentials, and historical submissions.

----

### Judging & Finalization (`AdminController` & `EvaluationController`)

*(Requires `Authorization: Bearer <token>` Header. Protected by `JwtAuthFilter`)*

- **`GET /api/admin/submissions?status={status}`**
  - **Purpose:** Fetches submissions by state (`PENDING`, `GRADED`, `APPROVED`, `REJECTED`).
  - **Data Hydration:** Safely dynamically assigns the `@Transient` `teamName` field to submissions before serialization so Judges know who they are grading.
- **`GET /api/evaluations/me`**
  - **Purpose:** Fetches the judge's historical evaluations, allowing them to review and edit past grades.
- **`POST /api/evaluate`**
  - **Purpose:** Submitted by judges to officially grade a submission. Accepts a dynamic `scoreBreakdown` mapping and qualitative `feedback`.
  - **Upsert & Audit Trails:** If a judge re-grades a submission, the system seamlessly Upserts the new score and archives the previous score breakdown/feedback into the `evaluation_audits` table to maintain a transparent paper trail.
- **Idempotent Math Brain V2 (`EvaluationService` & `AdminService`)**
  - **Purpose:** Evaluates the arithmetic mean of all judge evaluations and determines if the submission passes.
  - **Behavior:** Dynamically parses the `passing_threshold` from the event config. Transitions submission to `APPROVED` or `REJECTED`. 
  - **Advanced Scoring:** Instead of a brittle running tally, it calculates `Registration.total_score` by sweeping all submissions and taking the **highest `APPROVED` score per unique `taskId`**. This eliminates double-counting points when teams resubmit for the same task or judges edit past grades out of order.

----

### Live Mentor Dispatch Engine (`MentorController`)

*(Requires `Authorization: Bearer <token>` Header. Protected by `JwtAuthFilter`)*

The platform uses a resilient "Uber-style" On-Demand Queue for hackathon mentorship instead of fragile calendar booking systems.

- **`PUT /api/mentors/me/status`**
  - **Purpose:** Admin/Judge endpoint to "Clock In" and "Clock Out" of mentor duty.
- **`GET /api/mentors/available`**
  - **Purpose:** Fetches the live radar of active mentors (pollable by teams).
- **`POST /api/mentors/sessions/request`**
  - **Purpose:** Teams use this to "Hail" a mentor for help. Validates that a team can only have 1 active request.
- **`PUT /api/mentors/sessions/{id}/accept`**
  - **Purpose:** Mentors intercept a request from the queue and drop their Live Meeting link. Instantly updates the Mentor's status to `BUSY`.
- **Auto-Cancel Garbage Collector (`MentorService`)**
  - A Spring `@Scheduled(fixedRate = 60000)` cron job automatically sweeps the database and cancels ghosted `REQUESTED` sessions older than 10 minutes to un-brick the queue.

---

## ⚙️ Event Configuration (`JSONB`)

Instead of hard-coding the hackathon rules into rigid relational tables, the entire event is driven by a massive schema-less `JSONB` document stored in the `events.config` column.

This allows organizers to design completely custom workflows:
- **`roadmap`**: An array defining the progression tasks (e.g., `FEATURE-1`, `FEATURE-2`).
- **`rubric`**: Defined per task (e.g., "UI/UX", "Code Quality"). Judges dynamically grade against these categories.
- **`passing_threshold`**: Configurable globally or per task, enabling the Math Brain to automatically determine if a team proceeds.
- **`leaderboardPublished`**: A boolean flag that Admins can toggle to reveal or hide the leaderboard from participants during different phases of the event.

---

## 🖥️ Frontend Applications (Next.js)

The platform is split into three core Web Experiences:

1. **The Participant Dashboard (`/dashboard`)**:
   - Strictly controlled by the Backend State Machine. Teams only see the feature they are currently allowed to submit.
   - Includes the **Live Mentor Radar**, allowing them to instantly hail help from active judges.
2. **The Admin Command Center (`/admin`)**:
   - Used to bulk-import CSVs of participants (generating secure team credentials).
   - Global event controls (e.g., releasing new rounds to all teams, publishing leaderboards).
   - Real-time Leaderboard with deep-dive Team Auditing.
3. **The Judging & Mentor Portal (`/judging`)**:
   - A unified queue for reviewing code submissions. Dynamic sliders are rendered based on the `JSONB` rubric.
   - Includes the **Mentor Command Center** (`/judging/mentor-board`), where judges can clock in to take live support tickets.
