/* Tab 5 — Solution Structure.  Edit the text between the backticks. */
page({
  slug: 'solution-structure',

  eyebrow: 'Solution Structure',
  title: 'How it is put together',
  lead: 'A modular monolith: one application, one database, one deployable — with real boundaries between modules rather than layers smeared across the codebase.',

  body: `
## The modules

| Module | Owns |
|---|---|
| common | base entity, error handling, security, encryption, seed data |
| auth | registration, login, refresh-token rotation |
| user | profile, settings, nutrition goals, body measurements |
| nutrition | food entries, food items, saved foods |
| training | training sessions, exercises, sets |
| whoop | OAuth, HTTP client, synchronisation, wearable adapter |
| ai | food analysis service and provider adapters |
| analytics | dashboard, energy model, insights, aggregated series |
| storage | storage service and storage adapters |

Each module owns its own domain, service, DTO and API layers.

## The three rules that keep the boundaries real

::: cards.accent
### Integrations are inverted
Analytics defines the wearable port; WHOOP implements it. Nothing in analytics
or the dashboard mentions WHOOP, so a second wearable is a new adapter and no
change anywhere else.

### Third-party SDKs live in exactly one class
One file imports the AI SDK. One file knows the wearable's URLs. One file per
storage provider knows its HTTP contract. Swapping any of them is a contained
change.

### Entities never leave a controller
Every endpoint returns a DTO. The database schema is free to move without
breaking a client.
:::

## The stack

::: columns
### Frontend
React 18, TypeScript and Vite, installable as a PWA. A single origin in
development, so the refresh-token cookie stays HttpOnly and SameSite=Lax with
no cross-site relaxation.

### Backend
Java 21 and Spring Boot 3.3. PostgreSQL 16 with Flyway migrations as the source
of truth for the schema.
:::

## Data model decisions worth naming

- **UUID primary keys**, timestamps on every table, foreign keys with
  deliberate delete behaviour, and indexes chosen from the actual query shapes.
- **JSONB appears twice only** — raw third-party payloads and AI metadata. Both
  are genuinely semi-structured and neither is queried on. Everything the
  application filters, sorts or aggregates by has a typed column.
- **Goals are a dated series.** Resolving "the goal in effect on day X" is a
  descending index scan. It is also the seam through which targets can later
  become dynamic per day or per training load.
- **Unique constraints carry meaning** — case-insensitive on email, partial
  unique indexes so imported records cannot duplicate while manual ones stay
  unconstrained, one wearable account per application account.

## Testing

::: stats
109 | backend tests | H2 in PostgreSQL compatibility mode, no Docker needed
39 | frontend tests | unit and component
0 | external calls | the wearable is stubbed, the AI provider is a stub
:::

Two structural checks guard the schema itself: one compares the ORM mapping
against the migration SQL so a drift fails the build rather than production
startup; the other applies the migrations to a real PostgreSQL container to
check what H2 cannot express — JSONB columns, partial indexes, expression
indexes.

::: note Honest note
Anything whose state must survive a thrown exception — refresh-token theft
detection, sync failure recording, a failed AI analysis — commits for real
rather than running inside a rolled-back transaction. Under a wrapping
transaction those paths produce false passes.
:::

## What we would build next, structurally

- A materialised daily rollup, once history grows past personal scale
- Webhooks instead of polling for the wearable, to cut sync latency
- A per-account rate limit on AI analysis, to cap cost per user
`
});
