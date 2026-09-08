/* Tab 3 — Solution.  Edit the text between the backticks. */
page({
  slug: 'solution',

  eyebrow: 'Solution',
  title: 'What the product does today',
  lead: 'Not a prototype and not a slide. A working application: register, log, train, connect a wearable, and see the whole picture on one dashboard.',

  body: `
::: chips.accent
React 18, TypeScript, Vite, PWA, Java 21, Spring Boot 3.3, PostgreSQL 16, Flyway, JWT auth, WHOOP API v2, Docker Compose
:::

## Four ways to log a meal

::: cards
### Manual
Name, meal, time, and the five macros. The path that always works, offline
included.

### Saved food
Reuse something logged before; enter any quantity and every macro scales.

### Describe
"300 g chicken breast, 200 g cooked basmati rice and 15 g olive oil" becomes a
structured estimate with items and confidence.

### Photograph
Take or choose a picture of the plate; the same structured estimate comes back.
:::

::: note.warning The review step is structural, not a convention
Describe and Photograph both stop at a review screen. Nothing reaches the food
log until the user confirms it. Both the original prediction and the corrected
values are stored, so estimation accuracy can be measured later rather than
assumed.
:::

## Today

One screen, computed from stored data:

- Calories against target, with the remainder
- Protein, carbohydrate, fat and fibre, each with what is left
- Latest body weight and its 7-day trend
- WHOOP recovery, sleep and strain, plus today's workouts
- A strip of calculated insights

## Training and body

- Sessions modelled three levels deep — session, exercise, set — so
  exercise-level strength tracking later is a UI change, not a migration
- Body weight entries with a trend, so a single heavy day does not read as
  progress lost

## Progress

Daily, weekly and monthly views with charts for calories, macros, weight,
workout frequency, recovery, strain and sleep.

::: note.positive Accessibility
Every chart has a table view, and colour never carries identity on its own —
each series has a legend and a label. The palette was checked for
colour-vision separation and 3:1 contrast against the chart surface.
:::

## Wearable

WHOOP connects with OAuth 2.0 from Settings. The first sync imports 90 days;
after that a background job syncs hourly, and an on-demand sync is one tap.

::: note.accent Where the tokens live
WHOOP tokens never reach the browser. They are encrypted at rest with AES-GCM
and only ever used server-side.
:::

## What we deliberately did not build

No Kubernetes, no message broker, no microservices, no CQRS, no generic
"framework" layer. At this size each of those is cost without benefit.
Extension points exist where they were cheap — the wearable port, the AI
interface, the storage interface — and nowhere else.
`
});
