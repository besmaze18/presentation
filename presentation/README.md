# Presentation site

A tabbed website to present in place of a slide deck. Design matches the
maroon / gold brand style (no map backgrounds).

## How to view it

Just **double-click `open.html`** (or `index.html`). No server, no internet
required — it works straight off your disk. Use the tabs at the top to move
between sections. It also works on a projector / big screen in any browser.

> The heading fonts use Google Fonts when you're online, and fall back to a
> clean system font when you're offline — either way it looks right.

## Change the placeholder names ONCE (Country, platform names, etc.)

Open **`content/config.js`**. It has five values you can change, and they
update **everywhere** on the site automatically:

```js
window.SITE_PARAMS = {
  PLATFORM: "TTTT Platform",   // your platform name
  LEGACY:   "LEGACY",          // the client's existing / legacy estate
  COUNTRY:  "Country",         // the country name
  XXXX:     "XXXX",            // delivery partner / company 1
  YYYY:     "YYYY",            // delivery partner / company 2
};
```

Change only the text on the right of each colon. Anywhere a content file
writes `{{PLATFORM}}`, `{{COUNTRY}}`, `{{LEGACY}}`, `{{XXXX}}` or `{{YYYY}}`,
your value is dropped in. So set the real names here once and every page
updates.

## How to edit the content yourself (no coding needed)

**All the words live in the `content/` folder** — one file per tab:

| Tab              | Edit this file                    |
|------------------|-----------------------------------|
| Open             | `content/open.js`                 |
| Our Vision       | `content/highlights.js`           |
| Solution         | `content/solution.js`             |
| Demo             | `content/demo.js`                 |
| Operate          | `content/operate.js`              |
| Structure        | `content/structure.js`            |
| Timeline         | `content/timeline.js`             |
| Team             | `content/team.js`                 |
| Our Experience   | `content/our-experience.js`       |
| Thank You        | `content/thank-you.js`            |

Open one of these in any text editor, change the text inside the quotes,
save, then refresh the page in your browser. **You never touch the HTML.**

Anywhere you see `[SOMETHING IN BRACKETS]` is a placeholder for you to fill in
(this is where your sensitive content goes). None of it is committed with real
data until you type it in.

### Colouring a word maroon
In the big `title`, wrap a word in `*stars*` and it turns maroon:
`title: "Our *Vision*"` → "Our **Vision**".

### Building blocks you can use in a page's `sections`
Each section has a `type`. The common ones:

- `cards`   — numbered white cards (`items: [{ num, title, body }]`).
  Extras: `num: null` hides the number; `num: "PROCESS"` shows a custom
  gold label; `variant: "filled"` makes the whole row solid maroon;
  each item can have `bullets: ["…","…"]` for a highlighted list.
- `columns` — plain cards without numbers (`items: [{ title, body }]`)
- `stack`   — the layered platform-as-a-service architecture diagram
  (`consumers`, `connector`, `provider.rows`, `platforms`). Rows are either
  a set of `items` (cells) or `{ type: "bar", title, sub }` full-width bars.
- `bands`   — a multi-band architecture diagram (used in Solution). A list of
  `bands`, each with a `title`, optional `note`, `cells` and/or nested `groups`.
  Cells take `title`, `sub`, `ref`, `badges: ["N","3"]`, and an optional
  `view` to make the cell a drill-down link. An optional `legend` draws the key.
- `video`   — swappable demo video slots (`items: [{ caption, src, poster }]`).
  Put files in `assets/video/` and set `src`. Empty slots show a placeholder.
- `link`    — a cross/back link to another view (`{ to: "architecture", label }`).

- `deck`    — coloured "function cards" (used in Structure / Operate). Each item:
  `{ color, emoji, header, sub, lines: [...], chips: [...], body }`. Colours:
  maroon / dark / blue / teal / gold / slate.
- `supportbar` — a full-width dark bar (`icon`, `title`, `body`), e.g. Operate L3.
- `orgchart` — a compact program org chart (Team): `governance`, `sme` (+`note`),
  `leadership`, `governancePods` (+`desc`), and `delivery` with `streamAligned`,
  `enabling`, `foundation`. Each has a `label` and a `boxes` list.
- `credentials` — case-study cards (Our Experience): `items:[{ image, title,
  subtitle, tags:[...], body }]`. Add `image: "assets/img/x.jpg"` for a photo.
- Gantt bars/tasks accept `icon: "sprint"` to show an agile sprint icon.
- `vision` / `sdlc` / `accel` — the animated Highlights stage visuals (provider
  diagram, AI-in-SDLC orbit ring, accelerator cards).
- `journey` — the animated provisioning journey (Solution → The Journey view):
  `layers` (the mini architecture) + `steps` (the story) + `final` message.
  Play / Step / Reset buttons, ← → and P keys, a travelling orb and a live log.
  Four switchable visualizations (top-right "View:"): Network (force-directed
  technology graph — each step's `tech: [...]` list flies in as satellite
  nodes; same-named technologies become shared nodes linking steps; drag
  nodes, click a numbered step to jump), Architecture, Flow map, Orbit.
  Set `defaultViz` on the journey block to change the default.
- `tiers` — the Operate escalation ladder: `items: [{ level, color, title,
  tag, body, chips }]`, connected badges, animated entry.
- `pods` — clickable pod boxes (Team): `items: [{ color, title, sub,
  members: [{ name, role }] }]`; clicking opens the member list in a pop-up.
- The Timeline gantt animates on load (bars grow, stars pop) and has a
  ↻ Replay button next to the filters.

### Staged pages
A content file can define `stages: [{ n, label, lede, sections }]` — this
renders a stepper (01 → 04) and shows one stage at a time with entry
animations. ← / → move between stages, Space replays the animation.
No tab uses this today; Our Vision now uses sub-tabs instead (see below).
- `gantt`   — an interactive month timeline (Timeline). `months`, `scale` (labels),
  optional `hint`, `filters: [{id,label}]`, and `rows: [{ label, start, end, text,
  color, groups:[filterIds], milestone, milestoneLabel, phase, tasks:[…] }]`.
  A row with `tasks` is clickable to expand its sub-bars. Bar colours: maroon /
  blue / teal / gold / dark / slate.
- Architecture `bands` and their `groups` accept a `color` on the header
  (blue / teal / gold / slate / dark) for visual variety.

### Sub-tabs (Our Vision, Solution)
A content file can define `views: [ { id, label, title, lead, sections } ]`
instead of a single `sections` list. This draws a row of pill buttons — the
sub-tabs — under the page title, and shows one view at a time.

Each view is one slide. To add a slide, copy a whole
`{ id, label, title, lead, sections }` block and give it a new `id`; to reorder
the slides, move the blocks; to rename a sub-tab, change its `label`. The
active view is kept in the address bar, so `highlights.html#principles` opens
that sub-tab directly — useful for jumping straight to a slide mid-meeting.

Three tabs use sub-tabs today:

**Our Vision** (`content/highlights.js`) — Vision Entry → Solution Principles.

**Timeline** (`content/timeline.js`) — Our Approach → Project Timeline.

**Our Experience** (`content/our-experience.js`) — Case Studies → Google
Partnership → Worldwide Leaders.

A content file can also define `details: { "id": { title, lead, sections } }` —
these are **pop-up** views. Any `band`, band `group`, or cell with
`detail: "id"` becomes clickable and opens that pop-up over the page (close
with ✕, Esc, or by clicking outside).

**Solution** (`content/solution.js`) uses six sub-tabs — Platform Provider →
The Architecture → GCP at the Heart → QORE → Proposed Architecture → The
Journey. In the two architecture sub-tabs, clicking a coloured layer header
collapses or expands that layer, so you can take one layer at a time.

### Fixed columns
Add `cols: 2` (or 3 / 4) to a `cards` or `columns` block to force that many
columns (they collapse to one column on narrow screens).
- `steps`   — screenshot + caption cards (`items: [{ label, caption, body, image }]`)
- `panel`   — dark maroon banner (`title`, `subtitle`, `items: [...]`)
- `tags`    — pill chips (`title`, `items: ["...", "..."]`)
- `timeline`— vertical timeline (`items: [{ date, title, body }]`)
- `team`    — people grid (`items: [{ name, role, photo }]`)
- `text`    — paragraphs (`heading`, `body: ["para 1", "para 2"]`)
- `image`   — a picture (`src`, `caption`)

Copy an existing block in any content file as a starting point — the structure
is the same everywhere.

### Adding pictures (screenshots, team photos)
Put image files in `assets/img/`, then point to them from a content file, e.g.
`image: "assets/img/step1.png"` or `photo: "assets/img/jane.jpg"`.

## Adding, removing or reordering tabs
Open `assets/site.js` and edit the `PAGES` list at the top. To add a tab:
copy one of the `.html` files, copy a `content/*.js` file, and add a line to
`PAGES`.

## Where the design lives
Colours, fonts and spacing are in `assets/styles.css`. The brand colours are at
the very top (`--maroon`, `--gold`) if you ever want to tweak them.
