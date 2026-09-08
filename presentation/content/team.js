/* ---------------------------------------------------------------------------
   Tab 8 — Team.

   In the ":::people" block the heading is "### Name | Role". The circular
   avatar is generated from the initials of the name — no image files needed.

       ### Ada Lovelace | Backend lead
   --------------------------------------------------------------------------- */
page({
  slug: 'team',

  eyebrow: 'Team',
  title: 'Who did the work',
  lead: 'A small team, each person accountable for a whole vertical rather than a layer. Replace the names below with your own before the meeting.',

  body: `
::: people
### Name Surname | Engagement lead
Your point of contact. Owns scope, schedule and the weekly written update.

### Name Surname | Backend
Domain model, module boundaries, authentication and the wearable integration.

### Name Surname | Frontend
The application shell, the four logging flows and the progress charts.

### Name Surname | Design
The design system this deck is built from, the review-step interaction, and the
accessibility work on the chart palette.

### Name Surname | Quality
The test strategy, the structural schema guards, and the stubbed integrations
that keep the suite offline.
:::

## How we work

::: cards
### One team, one backlog
No hand-off between a "design phase" and a "build phase". The people who
designed the review screen are the people who built it.

### Written decisions
Every decision taken where the brief left room is written down, with the
trade-off and what it would cost to reverse. You have that document.

### Demoable increments
Two-week increments, each ending in something running that you can click.

### Honest limits
We tell you what is unverified before you find it. There is a section of this
deck that does nothing else.
:::

## Who you would talk to

| Question | Person | Response time |
|---|---|---|
| Scope, schedule, commercials | Engagement lead | Same working day |
| Anything technical | Backend or frontend lead | Same working day |
| Production incident | On-call rota | Per the agreed SLA |

::: note Replace before the meeting
Names, roles, photographs and response times are placeholders. Edit them in
**content/team.js** — nothing else needs to change.
:::
`
});
