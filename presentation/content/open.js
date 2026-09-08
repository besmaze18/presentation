/* ---------------------------------------------------------------------------
   Tab 1 — Open

   Everything the audience reads is between the backticks below. Write plain
   Markdown; the special ":::" blocks are documented in ../README.md.

   Two things to avoid inside the text: a backtick, and the two characters
   dollar-sign followed by an opening brace. Both are JavaScript syntax and
   will break the page. Everything else — apostrophes, quotes, accents,
   em dashes — is safe.
   --------------------------------------------------------------------------- */
page({
  slug: 'open',

  eyebrow: 'Opening',
  title: 'FitTrack',
  lead: 'A personal nutrition, training and recovery tracker — and the one number that ties them together.',
  centred: false,

  body: `
::: stats
4 | ways to log a meal | typed, saved, described, photographed
1 | deployable | one Docker Compose command
0 | vendor lock-in | AI, storage and wearable are all swappable
:::

## Why we are here

Thank you for the time today. In the next thirty minutes we would like to walk
you through what we understood from your brief, what we built, and what it
would take to run it in production.

We will keep the talking short and spend most of the time in the product itself.

## What we will cover

::: cards
### Our Vision
The problem as we understood it, and the outcome we designed towards.

### Solution
What the product does today, end to end — and the decisions behind it.

### Demo
A live walk-through of the working application.

### Solution Structure
How it is put together, and why it can be extended without being rewritten.

### Operate
Deployment, configuration, security and what running it actually costs.

### Timeline
Where we are, and what the next phases look like.

### Team & Experience
Who did the work, and what we have delivered before.
:::

::: note.accent Ground rules
Please interrupt at any point. Questions during the demo are far more useful
than questions saved for the end.
:::
`
});
