# FitTrack

A personal nutrition, training, recovery and health-performance tracker.

Log what you eat — by typing macros, reusing a saved food, describing a meal in
plain language, or photographing it — track training and body weight, connect a
WHOOP account, and see the whole picture on a Today dashboard and in daily,
weekly and monthly trends.

- **Frontend** — React 18 + TypeScript + Vite, installable as a PWA
- **Backend** — Java 21 + Spring Boot 3.3, a modular monolith
- **Database** — PostgreSQL 16 with Flyway migrations
- **Auth** — Spring Security with JWT access tokens and rotating refresh tokens
- **Wearable** — WHOOP Developer API v2 (OAuth 2.0)
- **AI** — a multimodal LLM behind a provider-independent interface
- **Storage** — object storage behind a `StorageService` abstraction
- **Deployment** — Docker Compose

---

## Quick start

You need Docker and Docker Compose. Nothing else.

```bash
git clone <this repository>
cd fitness-tracker

cp .env.example .env

# Fill in the two secrets the stack refuses to start without:
#   POSTGRES_PASSWORD, JWT_SECRET and TOKEN_ENCRYPTION_KEY
openssl rand -base64 48   # -> JWT_SECRET
openssl rand -base64 32   # -> TOKEN_ENCRYPTION_KEY  (must be exactly 32 bytes)
openssl rand -base64 24   # -> POSTGRES_PASSWORD

docker compose up --build
```

Then open:

| What | Where |
|---|---|
| The app | <http://localhost:8081> |
| API | <http://localhost:8080/api> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI document | <http://localhost:8080/v3/api-docs> |
| Health | <http://localhost:8080/actuator/health> |

Register an account at <http://localhost:8081/register>. Once yours exists you
can close public sign-up with `REGISTRATION_ENABLED=false`.

**Your data survives restarts.** PostgreSQL and uploaded images live in named
Docker volumes, so `docker compose restart`, `docker compose down` and
`docker compose up` all keep your history. Only `docker compose down -v`
deletes it.

```bash
docker compose down          # stop, keep data
docker compose up -d         # start again, data intact
docker compose logs -f backend
docker compose down -v       # DELETES the database and uploads
```

### Optional: demo data

To explore the app with a month of history already in place, set these in
`.env` before the first `docker compose up`:

```dotenv
SEED_ENABLED=true
SEED_EMAIL=demo@fittrack.local
SEED_PASSWORD=<choose one>
SEED_DAYS=30
```

Seeding is skipped without a password, and skipped again if the account already
exists, so restarting never duplicates history. Leave `SEED_ENABLED=false` for
anything but local exploration.

---

## Running it for development

Run PostgreSQL in Docker and the two applications on your machine, so both
reload as you edit.

```bash
# 1. Database only
docker compose up -d db

# 2. Backend  (http://localhost:8080)
cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/fittrack
export DATABASE_USER=fittrack
export DATABASE_PASSWORD=<your POSTGRES_PASSWORD>
export JWT_SECRET=<your JWT_SECRET>
export TOKEN_ENCRYPTION_KEY=<your TOKEN_ENCRYPTION_KEY>
mvn spring-boot:run

# 3. Frontend  (http://localhost:5173)
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to `http://localhost:8080`, so the browser
sees a single origin. That is what lets the refresh-token cookie stay
`HttpOnly; SameSite=Lax` without any cross-site relaxation — **use
<http://localhost:5173>, not the backend port, when developing.**

### Commands

**Backend** (`cd backend`)

```bash
mvn test                     # the whole suite (H2 — no Docker needed)
mvn test -Dtest=WhoopIntegrationTest
mvn spring-boot:run
mvn package                  # builds the jar
```

**Frontend** (`cd frontend`)

```bash
npm run dev                  # dev server with API proxy
npm test                     # unit and component tests
npm run typecheck
npm run lint
npm run build                # production build + service worker
npm run preview              # serve the production build
```

---

## Configuration

Everything is environment-driven; `.env.example` documents every variable and
contains no real credentials. The essentials:

| Variable | Required | What it does |
|---|---|---|
| `POSTGRES_PASSWORD` | ✅ | Database password. Compose refuses to start without it. |
| `JWT_SECRET` | ✅ | Signs access tokens. At least 32 bytes. |
| `TOKEN_ENCRYPTION_KEY` | ✅ | Encrypts third-party OAuth tokens at rest, and signs local storage URLs. Exactly 32 bytes. |
| `APP_CORS_ORIGINS` | | Browser origins allowed to call the API. |
| `REGISTRATION_ENABLED` | | Set to `false` to close public sign-up. |
| `JWT_COOKIE_SECURE` | | Set to `true` when serving over HTTPS. |

Everything else — the AI provider, object storage, WHOOP — is **optional**. The
app runs fully without any of them; the features that need them explain what is
missing rather than failing.

### AI food analysis

```dotenv
AI_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-...
ANTHROPIC_MODEL=claude-opus-5
```

Without a key, `/api/ai/food-analysis/status` reports `available: false`, the
photo and describe tabs are hidden, and manual entry is unaffected.

### Object storage

`STORAGE_PROVIDER=local` (the default) writes to a Docker volume and is fine for
a single-node deployment. `STORAGE_PROVIDER=cloudjs` uses the Cloud.js adapter
and needs `CLOUDJS_CLOUD_NAME`, `CLOUDJS_API_KEY` and `CLOUDJS_API_SECRET`. If
those are missing the app logs a warning and falls back to local storage rather
than failing to start. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#cloudjs-storage)
for what "Cloud.js" was taken to mean here.

### WHOOP

1. Create an app at <https://developer.whoop.com>.
2. Add `http://localhost:8080/api/whoop/callback` as a redirect URI — it must
   match `WHOOP_REDIRECT_URI` exactly.
3. Put the client id and secret in `.env`:

```dotenv
WHOOP_CLIENT_ID=...
WHOOP_CLIENT_SECRET=...
WHOOP_REDIRECT_URI=http://localhost:8080/api/whoop/callback
WHOOP_APP_REDIRECT_URI=http://localhost:8081/settings
```

Then connect from **Settings → WHOOP**. The first sync imports the last 90 days;
after that a background job syncs hourly, and you can sync on demand. Your WHOOP
tokens never reach the browser.

---

## Using it

**Add food — four ways.** Every screen has a quick-add button in the bottom
right; on Nutrition the same options are buttons.

1. **Manual** — name, meal, time, and the five macros.
2. **Saved** — reuse a food you saved; enter any quantity and the macros scale.
3. **Describe** — "300 g chicken breast, 200 g cooked basmati rice and 15 g
   olive oil" → a structured estimate.
4. **Photo** — take or choose a picture of the meal → a structured estimate.

**An AI estimate is never logged automatically.** Options 3 and 4 open a review
screen showing the detected items, the model's confidence, and the assumptions
it had to make. Every number is editable and any item can be removed. Only what
is on screen when you press *Save these values* becomes nutrition — and both the
original prediction and your corrected values are stored, so estimation accuracy
can be measured later.

**Today** shows calories against target, each macro with what remains, your
latest weight and its trend, WHOOP recovery, sleep, strain and today's workouts,
plus a strip of calculated insights ("420 kcal remaining", "7-day weight average
decreased 0.4 kg"). Those figures are computed from your stored data — no
language model produces them.

**Progress** has daily, weekly and monthly views with charts for calories,
macros, weight, workout frequency, recovery, strain and sleep. Every chart has a
table view.

---

## Testing

```bash
cd backend  && mvn test    # 109 tests
cd frontend && npm test    #  39 tests
```

The backend suite runs against **H2 in PostgreSQL compatibility mode**, so it
needs no Docker daemon and finishes in well under a minute. Flyway migrations
remain the source of truth for PostgreSQL, and `FlywayMigrationIT` applies them
to a real PostgreSQL container to check the parts H2 cannot express — JSONB
columns, partial indexes, expression indexes. That test **self-skips** where no
Docker daemon is present, so `mvn test` is green either way; run it in CI, where
Docker exists.

External services are never contacted by the suite: WHOOP is a WireMock stub and
the AI provider is a stub implementing the same interface as the real one.

Most tests run inside a transaction that is rolled back afterwards, which is fast
and keeps them isolated. Anything whose state must **survive a thrown exception**
(refresh-token theft detection, WHOOP failure recording, a failed AI analysis)
is marked `@CommittedIntegrationTest` instead and commits for real, with the
database truncated between tests — under a wrapping transaction those paths
produce false passes.

Two structural checks guard the schema itself: `SchemaConsistencyTest` compares
Hibernate's mapping metadata against the migration SQL, so a table or column in
one and not the other fails the build rather than production startup (where
`ddl-auto: validate` would catch it far too late); `FlywayMigrationIT` checks the
migrations themselves.

Covered: registration, login, refresh-token rotation and replay detection,
per-user data isolation across every resource, food creation and item-derived
totals, the AI confirm workflow, image validation, storage signing and path
traversal, WHOOP OAuth state validation, token encryption and refresh, sync
idempotency and pagination, and the dashboard's arithmetic.

---

## Project layout

```
fitness-tracker/
├── backend/                      Spring Boot modular monolith
│   └── src/main/java/com/fittrack/
│       ├── common/               base entity, errors, security, crypto, seed
│       ├── auth/                 registration, login, refresh tokens
│       ├── user/                 profile, settings, goals, body measurements
│       ├── nutrition/            food entries, items, saved foods
│       ├── training/             sessions, exercises, sets
│       ├── whoop/                OAuth, client, sync, wearable adapter
│       ├── ai/                   FoodAnalysisService + provider adapters
│       ├── analytics/            dashboard, energy model, insights, series
│       └── storage/              StorageService + storage adapters
├── frontend/                     React + TypeScript + Vite
│   └── src/features/             dashboard, nutrition, training, progress,
│                                 integrations, settings
├── docs/ARCHITECTURE.md          decisions, trade-offs, open questions
├── docker-compose.yml
└── .env.example
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for why things are built the
way they are, the API surface, and the decisions taken where the brief was
ambiguous.

## Licence

MIT.
