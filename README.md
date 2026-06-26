# Sora — AI Flight Booking Assistant

Sora is a conversational flight-booking assistant. You describe what you want in plain language, the AI searches for flights, confirms the details with you, and books only when you say so. It handles one passenger, single-destination flights — no hotels, no multi-city.

Built as a learning project to understand Spring Boot (layered architecture, Spring Security, Spring Data JPA) and the difference between a simple chatbot and a Spring AI agent — where the LLM doesn't just reply with text, but calls real Java methods that read and write to a database.

---

## What's built (Phase 1)

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.1.0, Java 21, Maven |
| Database | PostgreSQL 17 (pgvector image) via Docker |
| AI | Spring AI 2.0.0 + GitHub Models (gpt-4o-mini) |
| Auth | JWT (stateless, BCrypt passwords) |
| Frontend | Vue 3, Vite, TypeScript, Tailwind CSS |

Phase 2 features (conversation history, RAG, reschedule) and Phase 3 are not wired in yet — the frontend placeholders for them are muted.

---

## Prerequisites

- **Docker Desktop** — for Postgres
- **Java 21+** — if you don't have it globally, IntelliJ's bundled JBR works (see backend startup below)
- **Node 18+** — for the frontend
- **GitHub Personal Access Token** — used as the AI API key (free, no billing required); store it in `backend/src/main/resources/application-local.yml` (gitignored)

---

## 1. Start Postgres

```bash
docker compose up -d
```

This starts `sora-postgres` on port `5432` with database `sora`, user `sora`, password `sora`. Data persists in the `postgres_data` Docker volume.

Stop it with `docker compose down`. To also wipe the data: `docker compose down -v`.

---

## 2. Configure secrets

Create `backend/src/main/resources/application-local.yml` (gitignored — never commit it):

```yaml
app:
  jwt:
    secret: any-random-string-at-least-32-chars-long

spring:
  ai:
    openai:
      api-key: github_pat_YOUR_TOKEN_HERE
```

For production, pass `JWT_SECRET` and `GROQ_API_KEY` as environment variables instead.

---

## 3. Start the backend

From the `backend/` directory:

```bash
# If Java 21+ is not on your PATH, point to IntelliJ's bundled JBR first:
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr"   # Windows
# export JAVA_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr"          # macOS

./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

The app starts on **http://localhost:8080**.

On first boot, two dev users are seeded automatically:

| Username | Password |
|---|---|
| `alice` | `password` |
| `bob` | `password` |

The schema is rebuilt from scratch on each restart (`ddl-auto: create-drop`). That means bookings are wiped each time — expected for dev.

---

## 4. Start the frontend

From the `frontend/` directory:

```bash
npm install       # first time only
npm run dev
```

The frontend starts on **http://localhost:5173** and proxies `/api` calls to the backend.

---

## Startup order summary

After the first-time setup below is done once, every subsequent start is just:

**Windows (PowerShell):**
```powershell
.\start.ps1
```

**macOS:**
```bash
chmod +x start.sh   # first time only
./start.sh
```

Both scripts handle Docker Desktop, Postgres, backend, and frontend — opening each in their own terminal window then printing the URLs.

On macOS the script auto-detects Java 21+ from `JAVA_HOME`, the macOS `java_home` utility, or IntelliJ's bundled JBR — whichever it finds first.

Or manually:

```
1. docker compose up -d          # Postgres
2. cd backend && ./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
3. cd frontend && npm run dev
```

---

## Backend — API reference

All `/api/**` routes except auth require a `Bearer <token>` header.

### Auth

| Method | Path | Body | Returns |
|---|---|---|---|
| `POST` | `/api/auth/login` | `{"username":"alice","password":"password"}` | `{"token":"..."}` |
| `POST` | `/api/auth/register` | `{"username":"...","password":"..."}` | `{"token":"..."}` |

### Bookings

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/bookings` | List the authenticated user's bookings (confirmed + cancelled) |
| `PATCH` | `/api/bookings/{id}/cancel` | Cancel a booking by ID — only the owner can cancel their own |

Booking response shape:

```json
{
  "id": 1,
  "bookingReference": "SORA-A1B2C3",
  "carrier": "ANA",
  "flightNumber": "NH645",
  "origin": "HND",
  "destination": "KMJ",
  "departureTime": "2026-08-20T07:00:00",
  "arrivalTime": "2026-08-20T09:10:00",
  "price": 18500.00,
  "currency": "JPY",
  "status": "CONFIRMED",
  "createdAt": "2026-06-25T10:30:00"
}
```

### Chat (AI)

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/api/chat` | `{"message":"..."}` | Full response returned once the model finishes |
| `POST` | `/api/chat/stream` | `{"message":"..."}` | SSE stream — tokens arrive as they're generated |

The chat endpoint is the main entry point for the app. Send natural-language messages; the AI handles everything else.

---

## AI — how it works

Sora uses **Spring AI's tool-calling (agentic) loop**. The diagram below shows how a natural-language message travels through every layer before a reply reaches the browser.

```
Browser (Vue 3)
│  POST /api/chat  { message: "find flights to Tokyo next Friday" }
│
▼
ChatController          ← HTTP entry point, reads authenticated Principal
│
▼
ChatService             ← orchestrates the full AI turn
│   MessageChatMemoryAdvisor  ← prepends conversation history (per-user, in-memory)
│
│  sends: system prompt + history + user message + tool list
▼
LLM  (gpt-4o-mini via GitHub Models)
│
│  The model reads the tool list and decides what to call.
│  It returns a tool-call request instead of a plain reply.
│
▼
Spring AI runtime       ← intercepts the tool-call, routes to the right Java method
│
▼
SoraTools  (@Tool methods — this is where AI meets CRUD)
│
├── searchFlights ────────────► FlightSearchProvider
│                                (SeededFlightSearchProvider in Phase 1;
│                                 swap for SerpApiFlightSearchProvider later)
│
├── getMyBookings ────────────► BookingService
├── createBooking ────────────► BookingService ──► BookingRepository ──► PostgreSQL
└── cancelBooking ────────────► BookingService ──► BookingRepository ──► PostgreSQL

     ↑ tool result is returned to Spring AI as a new message
     ↑ Spring AI sends the updated thread back to the LLM
     ↑ the LLM may call more tools, or produce a plain text reply
     ↑ loop repeats until the LLM stops calling tools

Final plain-text reply  (+ flights list if a search ran)
│
▼
ChatController          ← wraps reply + flights into ChatResponse JSON
│
▼
Browser (Vue 3)         ← renders text as markdown, flights as boarding-pass cards
```

**Key points:**
- The LLM never touches the database directly — it can only call the five named tools.
- `createBooking` and `cancelBooking` are guarded by the system prompt rule: the model must summarise the action and receive explicit confirmation ("yes / confirm") before calling them.
- `BookingService` enforces per-user ownership in Java — even if the model called `cancelBooking` with another user's ID, the service would reject it.
- Conversation memory (`MessageChatMemoryAdvisor`) means turn 2 ("book the first one") works without re-sending the flight list — the history is stored server-side, keyed by username.

### Available tools

| Tool | What it does |
|---|---|
| `searchFlights` | Finds flights between two IATA airports on a specific date |
| `searchCheapestFlights` | Finds cheapest round-trip options across a whole month (flexible dates) |
| `getMyBookings` | Fetches the current user's booking list |
| `createBooking` | Creates a confirmed booking — only called after explicit user confirmation |
| `cancelBooking` | Cancels one of the user's bookings by ID — also requires confirmation |
| `getToday` | Returns today's date so the model knows the current date |

The model is instructed never to call `createBooking` or `cancelBooking` without first summarising the action and asking "confirm?". A booking row is created only on explicit confirmation — there is no draft state.

### Flight data (Phase 1)

The app ships a **seeded flight provider** (`SeededFlightSearchProvider`) with hardcoded HND → KMJ routes and generic fallback data for any other route. This means the app is fully functional for demos without any external API key.

When you're ready to use real flight data, swap in a `SerpApiFlightSearchProvider` that implements the same `FlightSearchProvider` interface — no other code changes needed.

---

## Security model

- Passwords are hashed with **BCrypt**.
- Login returns a **JWT** signed with HMAC-SHA256. The secret lives in `application-local.yml` or the `JWT_SECRET` env var — never in source.
- Every request to `/api/**` (except `/api/auth/**`) must include `Authorization: Bearer <token>`.
- Booking reads and writes are **always scoped to the authenticated user** — a user can never see or cancel another user's bookings.
- Sessions are **stateless** — no cookies, no `JSESSIONID`.

---

## Project layout

```
sora/
├── docker-compose.yml          # Postgres (pgvector)
├── frontend/                   # Vue 3 + Vite + TypeScript + Tailwind
│   └── src/
│       ├── views/              # Login, Chat, Bookings
│       └── stores/             # Pinia: auth, chat, bookings
└── backend/                    # Spring Boot
    └── src/main/java/com/sora/backend/
        ├── controller/         # HTTP layer: AuthController, BookingController, ChatController
        ├── service/            # Business logic: BookingService, ChatService, SoraTools (AI tools)
        ├── repository/         # Spring Data JPA: BookingRepository, UserRepository
        ├── domain/             # JPA entities: Booking, User
        ├── dto/                # Request/response shapes (never expose entities directly)
        ├── flight/             # FlightSearchProvider interface + SeededFlightSearchProvider
        ├── security/           # JWT filter, SecurityConfig, UserDetailsService
        └── config/             # CORS, DevDataInitializer
```
