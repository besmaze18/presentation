# Architecture notes

Why this is built the way it is, what was decided where the brief left room, and
what is deliberately not finished.

---

## Shape

A **modular monolith**. One Spring Boot application, one database, one
deployable — with real boundaries between modules rather than layers stacked
across the whole codebase.

```
com.fittrack
├── common      base entity, error handling, security, encryption, seed data
├── auth        registration, login, refresh-token rotation
├── user        profile, settings, nutrition goals, body measurements
├── nutrition   food entries, food items, saved foods
├── training    training sessions, exercises, sets
├── whoop       OAuth, HTTP client, synchronisation, wearable adapter
├── ai          FoodAnalysisService + provider adapters
├── analytics   dashboard, energy model, insights, aggregated series
└── storage     StorageService + storage adapters
```

Each module owns `domain` (entities + repositories), `service`, `dto` and `api`.
The rules that keep the boundaries real:

- **Integrations are inverted.** `analytics` defines `WearableDataPort`, and
  `whoop` implements it. Nothing in `analytics` or `dashboard` mentions WHOOP,
  so a second wearable is a new adapter and no changes anywhere else. A no-op
  adapter is active when nothing is connected, which is why the dashboard works
  fully without a device.
- **Third-party SDKs live in exactly one class.** `AnthropicFoodAnalysisProvider`
  is the only file that imports the Anthropic SDK; `WhoopClient` is the only file
  that knows WHOOP's URLs; the storage adapters are the only files that know
  their provider's HTTP contract.
- **Entities never leave a controller.** Every endpoint returns a DTO.

### What was deliberately not built

No Kubernetes, no message broker, no microservices, no CQRS, no generic
"framework" layer. The brief asked for an MVP and those would all be cost
without benefit at this size. Extension points exist where they are cheap
(the wearable port, the AI interface, the storage interface, the
exercise/set structure); nowhere else.

---

## Authentication

Short-lived JWT access tokens plus rotating refresh tokens.

- **Access token** — 15 minutes, signed HS256, held only in the SPA's memory. A
  page reload discards it.
- **Refresh token** — 30 days, delivered as an `HttpOnly; SameSite=Lax` cookie
  scoped to `/api/auth`, so JavaScript cannot read it and an XSS bug cannot
  exfiltrate a long-lived credential.
- **Storage** — only a SHA-256 hash of the refresh token is persisted. A
  database leak yields nothing usable.
- **Rotation and reuse detection** — every refresh issues a new token and
  revokes the old one. Tokens are grouped into a *family*; presenting an
  already-consumed token revokes the whole family, on the assumption it was
  stolen.

The cookie being `SameSite=Lax` is why the SPA and API must share an origin.
In Docker, nginx proxies `/api` to the backend; in development, the Vite dev
server does the same. The alternative — `SameSite=None; Secure` — would work
cross-origin but weakens CSRF protection for no gain here.

Passwords are bcrypt at cost 12. Login runs a hash comparison even for an
unknown email, so response timing does not reveal whether an account exists.

---

## Time zones and "which day is this?"

Every user has a time zone, and `FoodEntry`, `TrainingSession` and the WHOOP
tables each store a **denormalised local date** alongside the instant.

Two reasons. First, "what did I eat on Tuesday" then becomes a single indexed
lookup instead of a range scan with a time-zone conversion. Second, and more
important, history stays **stable**: if you travel or change your configured
zone, past days do not silently re-bucket underneath you. The date is decided
once, when the record is created, and the instant is kept for anything that
needs true chronology.

A night's sleep is attributed to the day it *ended* on, which is how people
think about it.

---

## The energy model

The brief was explicit that a wearable's reported burn is not gospel TDEE, and
the model reflects that. Four figures are stored and reported **independently**:

| Figure | Source |
|---|---|
| Intake | Sum of logged food |
| Wearable expenditure | The device, converted from kilojoules |
| Estimated BMR | Mifflin-St Jeor |
| Estimated TDEE | BMR × activity multiplier |

The response also carries `balanceBasis`, naming which figure the balance was
computed against (`WEARABLE_EXPENDITURE`, `ESTIMATED_TDEE` or `NONE`). The UI
labels the panel "Energy (estimates)" and says so in words.

When height, birth date or weight is missing, BMR and TDEE are **null**, not
guessed. A missing number is more honest than a fabricated one, and the UI says
what to fill in.

`EnergyCalculator` is a small, dependency-free component with unit tests pinning
each formula, which is what makes it safe to later swap in a personalised
expenditure estimate derived from intake, body-weight trend and wearable data.

---

## Insights are calculated, never generated

Everything in the dashboard's insight strip — "420 kcal remaining", "7-day
weight average decreased 0.4 kg", "Recovery is 14 points above your 30-day
average" — is computed by `InsightCalculator` from stored data. No language
model is involved, and each insight carries a stable `code` so a future
conversational layer can *explain* one without re-deriving the number.

Thresholds are explicit rather than incidental: a weight move under 0.2 kg reads
as "stable", a recovery deviation under 8 points reads as "in line with your
average". Weight deltas compare **rolling averages against the preceding
period**, not two single readings, because day-to-day water weight would
otherwise dominate.

---

## AI food analysis

`FoodAnalysisService` is the provider-independent interface:

```java
FoodAnalysisResult analyzeText(TextAnalysisRequest request);
FoodAnalysisResult analyzeImage(ImageAnalysisRequest request);
FoodAnalysisResult estimateNutrition(NutritionEstimateRequest request);
```

`AnthropicFoodAnalysisProvider` is the one shipped implementation. Substituting
a vendor means adding a sibling class and changing `AI_PROVIDER`; no domain,
controller or persistence code changes.

### The review step is structural, not a convention

An analysis creates an `AiAnalysis` row in `PENDING_REVIEW` and returns a
*proposal*. There is no code path from an analysis to a `FoodEntry` that does
not pass through `POST /api/ai/food-analysis/{id}/confirm`, and that endpoint
saves the values **in the request** — the ones the user accepted — not the
prediction. Confirming twice is rejected.

### Validation

Responses use structured output against an explicit JSON schema, and are then
re-validated by `FoodAnalysisResponseParser`, which rejects negative,
non-numeric, implausible or unnamed values outright. A plausible-looking wrong
number is worse than an honest failure, because the user cannot tell the
difference. Failures raise `AiUnavailableException` → HTTP 503, and the UI
falls back to manual entry.

### Measuring accuracy later

`ai_analyses` keeps the prediction, the model's confidence, the model name,
latency, token counts, the image reference **and** the user's confirmed values
side by side. That pairing is the point of the table: it is what makes it
possible to ask later how accurate the estimates actually were, by model and by
meal type.

An image is uploaded *before* analysis, so a provider failure still leaves the
user with their photo and a recorded attempt rather than losing both.

---

## Cloud.js storage

**This is the one place the brief was genuinely ambiguous, and the decision is
documented here rather than designed around.**

"Cloud.js" is not an unambiguous object-storage product. Rather than invent an
API, the application depends only on its own `StorageService` interface:

```java
StoredObject upload(StorageUpload upload);
Optional<byte[]> retrieve(String key);
void delete(String key);
String accessibleUrl(String key, Duration ttl);
```

`CloudJsStorageAdapter` implements that against the **Cloudinary-compatible HTTP
contract** Cloud.js exposes for image storage — a signed multipart
`POST /{cloud_name}/image/upload`, a signed `image/destroy`, and signed delivery
URLs for private assets, with SHA-1 signatures over the sorted parameters plus
the API secret.

**If your Cloud.js deployment exposes a different contract, that class is the
only thing that changes.** Nothing outside `com.fittrack.storage.adapter`
references a Cloud.js concept, and no domain code imports a provider type. The
adapter is not exercised against a live account here, so treat it as
implemented-to-the-credential-boundary: the shape is right, the signing scheme
is the documented one, and the seam is where it should be.

`LocalFilesystemStorageAdapter` is the default and is fully exercised. Selecting
`cloudjs` without complete credentials logs a warning and falls back to local
storage rather than failing to start.

### Signed URLs

An `<img>` element cannot send an `Authorization` header, so meal photos are not
served from a bearer-token endpoint. Local storage returns an **HMAC-SHA256
signed, time-limited URL**: unguessable, bound to its key so it cannot be
replayed against another object, and dead after 15 minutes. A bad signature and
a missing file return the same 404, so probing reveals nothing.

Uploads are validated by **magic bytes**, not the declared content type or the
file extension — a shell script renamed to `.jpg` is rejected. Only the storage
key and metadata reach PostgreSQL; image bytes never do.

---

## WHOOP

Implemented against the current **v2** API. Base
`https://api.prod.whoop.com/developer`; OAuth at `/oauth/oauth2/auth` and
`/oauth/oauth2/token` with credentials in the request body, which is what WHOOP
expects; collections at `/v2/cycle`, `/v2/recovery`, `/v2/activity/sleep` and
`/v2/activity/workout`, paginated with `next_token`.

### The OAuth callback

The callback is the only unauthenticated endpoint, because WHOOP redirects the
browser to it directly. It is authorised entirely by the `state` parameter,
which does double duty: it is the CSRF defence *and* the only way to know which
user began the flow. State rows are single-use and short-lived, validated and
consumed **before** the code is exchanged, so a forged or replayed callback is
rejected without ever contacting WHOOP.

### Tokens

Encrypted at rest with AES-GCM and a fresh nonce per value, so identical tokens
are not even identifiable in the database. Nothing — no token, client id or
client secret — is ever returned to the frontend or written to a log line;
error messages carry a status code and nothing else.

Access tokens refresh transparently just before expiry. A refresh WHOOP
*rejects* marks the connection `REAUTHORISATION_REQUIRED` rather than retrying
forever; a transient outage records the error without invalidating the grant.
The distinction matters: only the first needs the user to do something.

### Idempotency

Every imported record is upserted on its WHOOP identifier, under a unique index
scoped to the user. Re-running a sync over the same window updates rows; it
never duplicates them.

The first sync reaches back 90 days. Later syncs resume from the previous
window's end **minus a two-day overlap**, because WHOOP finalises a night's
sleep and recovery some time after the activity ends — without the overlap,
scores that arrive late would be missed permanently.

### Workouts become training sessions

A WHOOP workout is mirrored into a `TrainingSession` so imported and manual
training share one list, one set of analytics and one screen. The projection
rewrites **only device-measured fields**. A title, category, exertion rating or
note the user added survives every later sync — re-importing must never fight
the user. The API enforces the same split: an imported session accepts a PATCH
annotation but rejects a full PUT.

Raw payloads are kept in JSONB next to the extracted columns, so a field WHOOP
adds later is captured before the schema catches up, and every field is read
defensively — an unscored workout degrades one column, not the whole sync.

Synchronisation is a scheduled hourly job, each connection in its own
transaction so one expired grant cannot stall everyone else. WHOOP also offers
webhooks; polling is the simpler MVP choice and is what the overlap window is
designed around.

---

## Data model notes

UUID primary keys, `created_at`/`updated_at` on every table, foreign keys with
deliberate delete behaviour, and indexes chosen from the actual query shapes.

**Where JSONB is used, and where it is not.** There is no generic key-value
table. JSONB appears in exactly two roles: raw third-party payloads
(`whoop_*.raw_payload`) and AI metadata (`ai_analyses.prediction`,
`ai_analyses.raw_response`). Both are genuinely semi-structured and neither is
queried on. Everything the application filters, sorts or aggregates by has its
own typed column.

**Nutrition goals are a dated series**, not a mutable row. Resolving "the goal
in effect on day X" is a descending index scan, which means changing your
targets today does not retroactively rewrite how last month was scored — and it
is the seam through which targets can later become dynamic per day or per
training load.

**Training is modelled three levels deep** — session → exercise → set — even
though V1's UI barely uses the lower two. Adding exercise-level strength
tracking later is then a UI change, not a domain migration.

**Unique constraints carry meaning**: a case-insensitive unique index on
`LOWER(email)`; partial unique indexes on `(user_id, source, external_id)` so
imported records cannot duplicate while manual ones are unconstrained; one
WHOOP account per application account.

---

## Testing strategy

The suite runs against **H2 in PostgreSQL compatibility mode** with a
Hibernate-generated schema. It needs no Docker daemon and finishes in under a
minute, which keeps it worth running on every change.

The cost is that H2 cannot express everything PostgreSQL can. `FlywayMigrationIT`
closes that gap by applying the real migrations to a real PostgreSQL container
and asserting on the parts H2 cannot represent — JSONB columns, partial indexes,
expression-based unique indexes — plus that a second `migrate()` is a no-op and
`validate()` passes. It **self-skips** where no Docker daemon exists, so
`mvn test` stays green on a machine without one; run it in CI, where Docker is
present.

External services are never contacted. WHOOP is a WireMock stub returning real
v2 response shapes; the AI provider is a stub implementing the same interface as
the real adapter, and can be told to fail on demand so graceful degradation is
exercised rather than assumed.

Data isolation is tested per resource rather than once: for every owned
resource, a second user gets 404 on read, update and delete. Isolation bugs are
the kind that only appear where nobody looked.

---

## Frontend notes

**Mobile first, because that is where logging happens.** A quick-add button is
reachable from every screen with the four actions that need to be fast: add
food, take a food photo, add weight, add training. Inputs are 44px minimum and
16px font, which stops iOS Safari zooming the viewport on focus. The photo tab
uses a `capture="environment"` input to open the camera directly, with a second
plain file input beside it so desktop and photo-library flows also work.

**One round trip per screen.** The dashboard is assembled server-side;
`/api/analytics/series` returns dense, gap-filled, pre-bucketed rows. The
browser never receives raw history to aggregate.

**Charts.** Colours were validated with a colour-vision checker rather than
chosen by eye, which caught a real defect: the first hand-picked palette put
protein and carbs 5.2 apart under deuteranopia — indistinguishable. The palette
is now a validated set assigned in fixed slot order, passing the lightness band,
chroma floor, colour-vision separation and 3:1 contrast checks against the chart
surface, and the same colour means the same macro everywhere in the app.

Colour never carries identity alone: single-series charts are named by their
card title, the one multi-series chart has a legend, and **every chart has a
table view** for readers who cannot use the plot. Each chart has a single
y-axis; two measures with different scales get two charts.

**PWA.** Installable, with a service worker precaching the app shell. API
responses are excluded from the cache — they are user-specific and change
constantly, and a stale calorie total would be worse than a loading spinner.

---

## Security summary

- bcrypt (cost 12) password hashing; timing-equalised login
- Short-lived JWTs; refresh tokens hashed at rest, rotated, family-revoked on reuse
- Third-party OAuth tokens encrypted with AES-GCM
- OAuth `state` single-use, expiring, validated before the code exchange
- Every resource looked up by `(id, ownerId)` — one choke point for isolation
- Bean Validation on every request DTO; a consistent `ApiError` envelope
- Upload size limits and magic-byte content sniffing
- HMAC-signed, expiring URLs for stored images
- CORS restricted to configured origins
- Secrets only from the environment; `.env` git-ignored; `.env.example` has no real values
- Containers run as a non-root user
- Tokens, passwords and secrets never logged

---

## Known limits

Honest about what an MVP does not do:

- **The Cloud.js adapter is unverified against a live account.** See above.
- **No refresh-token binding to a device or IP.** Rotation plus reuse detection
  is the mitigation.
- **WHOOP sync is polling, not webhooks.** Simpler, and the overlap window
  covers late scoring. Webhooks would reduce latency.
- **Analytics are computed per request.** Fine at personal scale; a materialised
  daily rollup is the obvious next step if history grows large.
- **Single-node file storage by default.** The local adapter assumes one backend
  instance; use the Cloud.js adapter to scale out.
- **No email verification or password reset.** Both need an email provider,
  which the brief did not include.
- **`UnitSystem` is stored but the UI is metric throughout.** The setting exists
  and is persisted; the display conversion is not implemented.
- **AI cost is uncapped per user.** There is no per-account rate limit on
  analysis requests yet.

---

## API surface

All endpoints are under `/api` and require `Authorization: Bearer <token>`
except where noted. Full request and response schemas are in the OpenAPI
document at `/v3/api-docs`, browsable at `/swagger-ui.html`.

| Area | Endpoints |
|---|---|
| Auth *(public)* | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`; `POST /auth/logout-all` |
| User | `GET|PUT /users/me`, `/users/me/settings`, `/users/me/goals` |
| Nutrition | `GET /nutrition/days/{date}`, `GET /nutrition/entries`, `GET|PUT|DELETE /nutrition/entries/{id}`, `POST /nutrition/entries` |
| Saved foods | `GET|POST /foods`, `GET|PUT|DELETE /foods/{id}`, `POST /foods/{id}/log` |
| AI | `GET /ai/food-analysis/status`, `POST /ai/food-analysis/text`, `/estimate`, `/image`, `GET /ai/food-analysis/{id}`, `POST /ai/food-analysis/{id}/confirm`, `DELETE /ai/food-analysis/{id}` |
| Training | `GET|POST /training/sessions`, `GET|PUT|PATCH|DELETE /training/sessions/{id}` |
| Body | `GET|POST /body-measurements`, `GET /body-measurements/trend`, `PUT|DELETE /body-measurements/{id}` |
| WHOOP | `GET /whoop/status`, `POST /whoop/authorize`, `GET /whoop/callback` *(public — state-authorised)*, `POST /whoop/sync`, `DELETE /whoop/connection` |
| Dashboard | `GET /dashboard` |
| Analytics | `GET /analytics/series` |
| Storage | `GET /storage/files/**` *(public — signature-authorised)* |

Errors share one envelope:

```json
{
  "timestamp": "2026-03-10T12:00:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/nutrition/entries",
  "errors": [{ "field": "macros.calories", "message": "must be at least 0.0" }]
}
```
