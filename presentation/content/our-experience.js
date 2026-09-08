/* Tab 9 — Our Experience.  Replace the placeholder case studies with real ones. */
page({
  slug: 'our-experience',

  eyebrow: 'Our Experience',
  title: 'What we have built before',
  lead: 'The patterns behind this product are not new to us. Replace the entries below with the references you want to put in front of this client.',

  body: `
::: stats
00 | projects delivered | replace with your number
00 | years building products | replace with your number
00% | of engagements extended | replace with your number
:::

## Selected work

::: cards
### Client, sector, year
**The problem.** One or two sentences.

**What we built.** One or two sentences.

**The outcome.** A number, if you have one.

### Client, sector, year
**The problem.** One or two sentences.

**What we built.** One or two sentences.

**The outcome.** A number, if you have one.

### Client, sector, year
**The problem.** One or two sentences.

**What we built.** One or two sentences.

**The outcome.** A number, if you have one.
:::

## What we bring to this specific brief

::: cards.accent
### Integrations that survive their vendor
Ports and adapters, with the third-party SDK confined to a single class. We
have swapped a payments provider, a mapping provider and a wearable this way
without touching domain code.

### AI that a regulator would accept
A model proposes; a person confirms; both the prediction and the correction are
stored. Accuracy becomes something you can measure rather than claim.

### Schemas that do not drift
Structural tests that compare the ORM mapping to the migration SQL, so a
mismatch fails the build instead of production startup.

### Honest reporting
Every engagement ends with a written list of what is unverified. It is on this
deck, three tabs back.
:::

## References

::: quote
A short, specific quotation from a client is worth more than a page of claims.
Put one here.
— Name, Role, Company
:::

::: note Replace before the meeting
Everything on this page is placeholder text. Edit **content/our-experience.js**.
:::
`
});
