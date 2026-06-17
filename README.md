# Event Platform

This is a full-stack Event Management Platform built with **Spring Boot** (Backend), **Next.js** (Frontend), and **PostgreSQL** (Database).

_(Note: This project is in active development. This README will be updated as new features are added.)_

## 🏗️ Architecture Overview

The platform is built on a modern, containerized full-stack architecture:

| Component          | Technology             | Primary Role                                                                         |
| :----------------- | :--------------------- | :----------------------------------------------------------------------------------- |
| **Frontend**       | Next.js, Tailwind CSS  | React SPA for stateless UI rendering, client-side routing, and file validation.      |
| **Backend**        | Spring Boot 3, Java 21 | Core REST API handling business logic, CSV ingestion, auth, and event state.         |
| **Database**       | PostgreSQL             | Relational storage utilizing `JSONB` for dynamic, schema-less event configurations.  |
| **Infrastructure** | Docker Compose         | Orchestrates the entire stack, ensuring secure communication on an isolated network. |

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DOCKER COMPOSE NETWORK                            │
│                                                                             │
│  ┌──────────────────┐             HTTP / REST         ┌──────────────────┐  │
│  │   Next.js SPA    │ ──────────────────────────────► │    Spring Boot   │  │
│  │   (Frontend)     │                                 │     Backend      │  │
│  │                  │ ◄─────────── JSON ───────────── │ (Java + Maven)   │  │
│  │ ├─ Participant UI│                                 │                  │  │
│  │ ├─ Admin / CSV   │          Multipart/Form         │ ├─ Auth API      │  │
│  │ └─ Judge Portal  │ ──────────────────────────────► │ ├─ CSV Ingestion │  │
│  └──────────────────┘                                 │ ├─ State Machine │  │
│           ▲                                           │ └─ Evaluations   │  │
│           │                                           └────────┬─────────┘  │
│      User Browser                                              │            │
│      (Outside Docker)                                          │            │
│                                                        JPA / Hibernate      │
│                                                                │            │
│                                                                ▼            │
│                                                       ┌──────────────────┐  │
│                                                       │    PostgreSQL    │  │
│                                                       │     Database     │  │
│                                                       │                  │  │
│                                                       │ ├─ Events        │  │
│                                                       │ ├─ Registrations │  │
│                                                       │ └─ JSONB config  │  │
│                                                       └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

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
│                           DOCKER COMPOSE NETWORK                            │
│                                                                             │
│  ┌──────────────────┐             HTTP / REST         ┌──────────────────┐  │
│  │   Next.js SPA    │ ──────────────────────────────► │    Spring Boot   │  │
│  │   (Frontend)     │                                 │     Backend      │  │
│  │                  │ ◄─────────── JSON ───────────── │ (Java + Maven)   │  │
│  │ ├─ Participant UI│                                 │                  │  │
│  │ ├─ Admin / CSV   │          Multipart/Form         │ ├─ Auth API      │  │
│  │ └─ Judge Portal  │ ──────────────────────────────► │ ├─ CSV Ingestion │  │
│  └──────────────────┘                                 │ ├─ State Machine │  │
│           ▲                                           │ └─ Evaluations   │  │
│           │                                           └────────┬─────────┘  │
│      User Browser                                              │            │
│      (Outside Docker)                                          │            │
│                                                        JPA / Hibernate      │
│                                                                │            │
│                                                                ▼            │
│                                                       ┌──────────────────┐  │
│                                                       │    PostgreSQL    │  │
│                                                       │     Database     │  │
│                                                       │                  │  │
│                                                       │ ├─ Events        │  │
│                                                       │ ├─ Registrations │  │
│                                                       │ └─ JSONB config  │  │
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

<img width="906" height="531" alt="Screenshot 2026-06-17 at 02 48 35" src="https://github.com/user-attachments/assets/ef1a2c2f-6e66-4b64-8017-30ed7cd64515" />

---

## Backend API Routes

The Spring Boot backend is served at `http://localhost:8080`. All API routes are prefixed with `/api`.

### Admin Routes (`AdminController`)

- **`POST /api/admin/login`**
  - **Purpose:** Authenticates staff/admins.
  - **Payload:** `{ "username": "...", "password": "..." }`
  - **Returns:** A JSON object containing a secure JWT token.

- **`POST /api/admin/import`**
  - **Purpose:** Ingests the Unstop CSV file to bulk-register teams.
  - **Payload:** `multipart/form-data` containing the CSV `file` and `eventId`.
  - **Behavior:** Parses the CSV, extracts dynamic `member_details`, generates unique `team_passcode`s, and securely Upserts the records into the database.

### Team Routes (`AuthController`)

- **`POST /api/login`**
  - **Purpose:** Authenticates participating teams into their dashboard.
  - **Payload:** `{ "teamName": "...", "teamPasscode": "..." }`
  - **Returns:** Validation string (to be updated to JWT in the future).
