/* ---------------------------------------------------------------------------
   Tab 7 — Timeline.

   In the ":::timeline" block, "### Left | Right" puts the left half in the
   small coloured label and the right half in the phase title. For example:

       ### Weeks 1-2 | Discovery
   --------------------------------------------------------------------------- */
page({
  slug: 'timeline',

  eyebrow: 'Timeline',
  title: 'Where we are, and what comes next',
  lead: 'Seven phases are delivered. What follows is scoped, sequenced and costed — and each phase ends with something you can use.',

  body: `
## Delivered

::: timeline
### Phase 1 | Scaffold, Docker, PostgreSQL, auth
The deployable shape of the system, registration and login, and the migration
pipeline that every later phase builds on.

### Phase 2 | Nutrition domain and manual logging
Food entries, food items, saved foods, and item-derived totals.

### Phase 3 | Today dashboard, energy model, insights
The first screen, the arithmetic behind it, and the calculated insight strip.

### Phase 4 | Object storage and AI food analysis
The storage abstraction, the provider-independent AI interface, and the review
step that keeps estimates out of the log until confirmed.

### Phase 5 | Wearable OAuth and idempotent sync
OAuth with encrypted token storage, the sync job, and the idempotency that
makes a repeated import a no-op.

### Phase 6 | Training log, body weight, progress analytics
Sessions, exercises and sets; weight and trend; daily, weekly and monthly
charts with table views.

### Phase 7 | Schema guards, seed data, offline handling, documentation
The structural tests that catch schema drift at build time, demo data, offline
behaviour, and the architecture notes.
:::

## Proposed next

::: timeline
### Next | Hardening for real users
Email verification and password reset, a per-account rate limit on AI analysis,
and verification of the cloud storage adapter against a live account.

### Then | Scale and latency
A materialised daily rollup for analytics, and wearable webhooks in place of
polling.

### Then | Depth in training
Exercise-level strength tracking and personal records — a UI change, because
the domain already models session, exercise and set.

### Later | Dynamic targets
Nutrition goals that move with training load. The dated-series goal model was
built to be this seam.
:::

::: note.accent How we would like to run it
Two-week increments, each ending in something deployed and demonstrable, with a
short written note of what changed and what it cost. No phase depends on a
decision that has not been made yet.
:::

## What we need from you

::: cards
### Decisions
Which of the "next" items matter most to you, and in what order.

### Access
Credentials for the production accounts — AI provider, storage, wearable
developer app — so the unverified integrations can be verified.

### A pilot group
Ten to twenty real users for four weeks. Retention in week three is the only
metric that will tell us whether the design holds.
:::
`
});
