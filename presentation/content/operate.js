/* Tab 6 — Operate.  Edit the text between the backticks. */
page({
  slug: 'operate',

  eyebrow: 'Operate',
  title: 'Running it in production',
  lead: 'One command to start, environment-driven configuration, and a security posture we can walk through line by line.',

  body: `
## Deployment

One Docker Compose stack: database, backend, frontend. Nothing else is required
on the host.

::: stats
1 | command to start | docker compose up --build
3 | required secrets | database password, token signing key, encryption key
2 | named volumes | the database and uploaded images survive restarts
:::

Data survives restarts, stops and restarts of the whole stack. Only an explicit
volume teardown removes it.

## Configuration

Everything is environment-driven. The example environment file documents every
variable and contains no real credentials.

| Variable | Required | What it does |
|---|---|---|
| Database password | yes | Compose refuses to start without it |
| Token signing secret | yes | Signs access tokens; at least 32 bytes |
| Token encryption key | yes | Encrypts third-party OAuth tokens at rest and signs storage URLs |
| Allowed origins | no | Browser origins permitted to call the API |
| Registration enabled | no | Set to false to close public sign-up |
| Cookie secure flag | no | Set to true when serving over HTTPS |

::: note.accent Everything else is optional
The AI provider, object storage and the wearable are all optional. The
application runs fully without any of them; the features that need them explain
what is missing rather than failing to start.
:::

## Security

::: cards
### Passwords and sessions
bcrypt at cost 12, timing-equalised login. Short-lived access tokens held only
in memory; refresh tokens hashed at rest, rotated on every use, and the whole
family revoked when a used token is replayed.

### Third-party credentials
OAuth tokens encrypted with AES-GCM. The OAuth state value is single-use,
expiring, and validated before the code exchange.

### Isolation
Every resource is looked up by identifier *and* owner — one choke point for
per-user isolation, covered by tests on every resource.

### Uploads and storage
Size limits and magic-byte content sniffing on upload. Stored images are served
through HMAC-signed, expiring URLs.

### Boundaries
CORS restricted to configured origins. Bean validation on every request, with a
single consistent error envelope.

### Operations
Secrets only from the environment, never committed. Containers run as a
non-root user. Tokens, passwords and secrets are never logged.
:::

## Scaling path

- **Storage** — the default local adapter assumes a single backend instance.
  The cloud adapter is the switch that makes the backend horizontally
  scalable, and it is a configuration change, not a code change.
- **Analytics** — computed per request today, which is correct at personal
  scale. A materialised daily rollup is the obvious next step.
- **Wearable sync** — polling with an overlap window that covers late scoring.
  Webhooks would reduce latency when volume justifies it.

## Known limits, stated plainly

::: note.warning We would rather you hear these from us
- The cloud storage adapter has not been verified against a live account.
- The live wearable OAuth round-trip is unverified end to end; everything up to
  the credential boundary is tested against a stubbed API.
- No email verification or password reset — both need an email provider that
  the brief did not include.
- AI cost is uncapped per user; there is no per-account rate limit yet.
- Refresh tokens are not bound to a device or IP. Rotation plus reuse detection
  is the mitigation.
:::
`
});
