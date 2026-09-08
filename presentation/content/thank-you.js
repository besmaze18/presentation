/* Tab 10 — Thank You.  The closing slide. */
page({
  slug: 'thank-you',

  eyebrow: 'Thank You',
  title: 'Thank you',
  lead: 'We would rather leave you with the questions than the slides. Ask us anything.',
  centred: true,

  body: `
::: cards.accent
### What we showed
A working nutrition, training and recovery tracker: four ways to log a meal, a
dashboard whose numbers are calculated rather than generated, and a wearable
integration that can be replaced without touching the domain.

### What we propose
Hardening for real users, then scale and latency, then depth in training —
in two-week increments, each ending in something you can click.

### What we need
Your priorities, production credentials for the unverified integrations, and a
pilot group of ten to twenty real users for four weeks.
:::

## Questions we expect, and our answers

::: cards
### What happens if the AI provider changes its pricing?
One class imports the SDK. Changing provider is a configuration change and one
adapter; nothing in the domain moves.

### How accurate is the photo estimate?
We do not claim a number yet, and we would not trust one from a lab. Both the
prediction and the user's correction are stored, so after the pilot the answer
will be measured from real meals.

### Can this run on our infrastructure?
Yes. One Docker Compose stack, three required secrets, no managed services
assumed. The cloud storage adapter is the switch for multi-instance.

### What is not finished?
Email verification and password reset, a per-account AI rate limit, and
verification of two integrations against live accounts. All three are in the
first proposed phase.
:::

## Contact

| | |
|---|---|
| Engagement lead | Name Surname |
| Email | name@company.com |
| Phone | +00 000 000 0000 |

::: note Replace before the meeting
Contact details are placeholders. Edit **content/thank-you.js**.
:::
`
});
