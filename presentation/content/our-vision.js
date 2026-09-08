/* Tab 2 — Our Vision.  Edit the text between the backticks. */
page({
  slug: 'our-vision',

  eyebrow: 'Our Vision',
  title: 'One picture of the day, not five apps',
  lead: 'People do not fail at tracking because logging is hard. They fail because nothing joins what they ate, what they trained and how they recovered into a single, honest number.',

  body: `
## What we understood from the brief

::: columns
### The problem
Nutrition lives in one app, training in a second, recovery on a wrist strap,
and body weight in a note. Each is accurate on its own and useless together.

The user is left doing the arithmetic — badly, and only when motivated.

### The consequence
Logging stops within three weeks. Not because the food diary was wrong, but
because it never answered the only question that matters: *am I moving towards
the goal, or away from it?*
:::

## The outcome we designed towards

::: cards.accent
### Logging must take seconds
Four routes into the same food entry — typed macros, a saved food, a plain
sentence, or a photograph. The slowest one is under a minute; the fastest is
two taps.

### The numbers must be trustworthy
Everything on the dashboard is calculated from stored data. No language model
produces a figure the user is asked to act on.

### One screen must answer the question
Calories against target, each macro with what remains, weight and its trend,
recovery, sleep, strain and today's training — on the first screen, without
scrolling.
:::

## The principles we held to

::: steps
### Estimation is a proposal, never a fact
An AI estimate is never logged automatically. Photo and describe both open a
review screen showing the detected items, the model's confidence, and the
assumptions it had to make. Every number is editable and any item can be
removed. Only what is on screen when the user presses *Save these values*
becomes nutrition.

### Insights are calculated, never generated
"420 kcal remaining", "7-day weight average decreased 0.4 kg" — these come from
arithmetic over the user's own data. They are reproducible, explainable, and
they do not change if a model is swapped.

### The product works when nothing is connected
No AI key, no wearable, no cloud storage: the app still runs end to end. Every
optional integration explains what is missing rather than failing.

### History is never rewritten
Nutrition goals are stored as a dated series, not a mutable row. Changing your
targets today does not retroactively re-score last month.
:::

::: quote
The measure of this product is not how much data it collects. It is whether
someone still opens it in week twelve.
— Design principle we worked to
:::
`
});
