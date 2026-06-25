# Sora — AI Flight Booking Assistant

Sora is a conversational flight-booking assistant. You describe what you want in plain language, the AI searches for flights, confirms the details with you, and books only when you say so. It handles one passenger, single-destination flights — no hotels, no multi-city.

---

## What's built (Phase 1)

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.1.0, Java 21, Maven |
| Database | PostgreSQL 17 (pgvector image) via Docker |
| AI | Spring AI 2.0.0 + Ollama (local LLM) |
| Auth | JWT (stateless, BCrypt passwords) |
| Frontend | Vue 3, Vite, TypeScript, Tailwind CSS |

Phase 2 features (conversation history, RAG, reschedule) and Phase 3 are not wired in yet — the frontend placeholders for them are muted.

---

## Prerequisites

- **Docker Desktop** — for Postgres
- **Ollama** — for the local LLM ([ollama.com](https://ollama.com))
- **Java 21+** — if you don't have it globally, IntelliJ's bundled JBR works (see backend startup below)
- **Node 18+** — for the frontend

---

## 1. Start Postgres

```bash
docker compose up -d
```

This starts `sora-postgres` on port `5432` with database `sora`, user `sora`, password `sora`. Data persists in the `postgres_data` Docker volume.

Stop it with `docker compose down`. To also wipe the data: `docker compose down -v`.

---

## 2. Start Ollama

**Install Ollama** from [ollama.com](https://ollama.com), then pull the model the app expects:

```bash
ollama pull llama3.2:3b-instruct-q8_0
```

Start the Ollama server (it may already be running as a background service after install):

```bash
ollama serve
```

Ollama listens on `http://localhost:11434` by default, which matches the app's config.

To use a different model, override via environment variable when starting the backend:

```bash
OLLAMA_MODEL=llama3.2 ./mvnw spring-boot:run ...
```

---

## 3. Configure secrets

The backend needs a JWT signing secret. Copy the example and it's already filled in for local dev:

```bash
# backend/src/main/resources/
cp application-local.yml.example application-local.yml
```

`application-local.yml` is git-ignored. Never commit it. For production, pass `JWT_SECRET` as an environment variable instead.

---

## 4. Start the backend

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

## 5. Start the frontend

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

Both scripts handle Docker Desktop, Postgres, Ollama, backend, and frontend — opening the backend and frontend in their own terminal windows, then printing the URLs.

On macOS the script auto-detects Java 21+ from `JAVA_HOME`, the macOS `java_home` utility, or IntelliJ's bundled JBR — whichever it finds first.

Or manually:

```
1. docker compose up -d          # Postgres
2. ollama serve                  # Ollama (if not auto-started)
3. cd backend && ./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
4. cd frontend && npm run dev
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

Sora uses **Spring AI's tool-calling (agentic) loop**. When you send a message, the flow is:

```
Your message
    → Spring AI sends message + tool list to Ollama
    → Ollama decides which tool(s) to call and returns a tool-call request
    → Spring AI executes the Java method
    → The result is fed back to Ollama as context
    → Ollama generates the next response (may call more tools)
    → Loop ends when Ollama produces a plain text reply
    → That reply is returned to you
```

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
