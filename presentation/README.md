# Client presentation

A ten-tab presentation website that replaces the slide deck. It is plain HTML,
CSS and JavaScript — no build step, no dependencies, no internet connection
needed in the room. Double-click `open.html` and present.

The design is the product's own design system, lifted from
`frontend/src/styles/global.css`, so the deck and the application look like one
thing.

---

## The one thing to know

**All the words live in `content/`. Nothing else needs editing.**

```
presentation/
├── open.html                 ← page shells. No words in them.
├── our-vision.html
├── solution.html
├── demo.html
├── solution-structure.html
├── operate.html
├── timeline.html
├── team.html
├── our-experience.html
├── thank-you.html
├── index.html                ← redirects to open.html
├── content/                  ← EVERYTHING YOU EDIT IS HERE
│   ├── site.js               ← the tabs: names, order, brand
│   ├── open.js
│   ├── our-vision.js
│   └── ... one file per tab
└── assets/
    ├── theme.css             ← colours, type, spacing
    └── deck.js               ← the renderer
```

To change what the audience reads, open `content/<tab>.js`, edit the text
between the backticks, and reload the page in the browser. That is the whole
loop.

---

## Presenting

- Double-click `open.html`, or open `index.html`.
- **← and →** move between tabs. So does clicking a tab, or the buttons at the
  bottom of each page.
- Full screen: `F11` on Windows/Linux, `Ctrl+Cmd+F` on a Mac.
- **Backup:** print any page to PDF from the browser. The print stylesheet
  drops the navigation and switches to a light, readable layout.

---

## Editing a page

Each file in `content/` looks like this:

```js
page({
  slug: 'our-vision',

  eyebrow: 'Our Vision',                    // small label above the title
  title: 'One picture of the day',          // the big heading
  lead: 'A sentence under the heading.',    // optional
  centred: false,                           // true centres the hero

  body: `
  ...everything else goes here...
  `
});
```

Only `slug` matters technically — it must match the file name and the HTML file
name. Everything else is yours.

### Two characters to avoid

Inside the `body` text, avoid a lone **backtick** and the sequence **`${`** —
both are JavaScript syntax and will break the page. Everything else, including
apostrophes, quotes and accented characters, is safe.

If a page ever goes blank, that is almost always the cause. Open the browser
console (`F12`) and it will point at the line.

---

## What you can write in `body`

Plain Markdown, plus a few blocks that produce the styled elements.

### Markdown

The hero title is the page's only `h1`, so `#` and `##` both render as a
section heading; `###` is the sub-heading below it.

```
## A section heading      (a divider rule is drawn above it)
### A sub-heading

A paragraph. **Bold**, *italic*, `code`, and [a link](https://example.com).

- a bullet
- another bullet

1. a numbered step
2. another

> A pull quote.

| Column | Column |
|---|---|
| cell | cell |

---   (a horizontal rule)

![Caption text](assets/screenshot.png)
```

### Cards — a responsive grid

```
::: cards
### Card title
Body text, bullets, anything.

### Second card
More text.
:::
```

Use `::: cards.accent` for the highlighted variant.

### Stats — big numbers

One per line, `value | label | hint`. The hint is optional.

```
::: stats
109 | backend tests | no Docker needed
39 | frontend tests |
:::
```

### Steps — a numbered walk-through

```
::: steps
### First thing
What happens.

### Second thing
What happens next.
:::
```

### Timeline — phases on a rail

The heading is `left | right`: the left half becomes the small coloured label,
the right half the phase title.

```
::: timeline
### Weeks 1-2 | Discovery
What happens in this phase.

### Weeks 3-6 | Build
And in this one.
:::
```

### People — cards with generated initials

The heading is `Name | Role`. The circular avatar is drawn from the initials,
so there are no image files to manage.

```
::: people
### Ada Lovelace | Engagement lead
One or two sentences.
:::
```

### Columns — two side by side

```
::: columns
### The problem
Text.

### The consequence
Text.
:::
```

### Callouts

```
::: note.accent An optional label
Text.
:::
```

`::: note` and `::: note.accent` are cyan, `::: note.warning` amber,
`::: note.positive` green. The words after the block name become the small
label; leave them off for no label.

### Quote

A line beginning with an em dash at the end becomes the attribution.

```
::: quote
The sentence you want to land.
— Name, Role
:::
```

### Chips — a row of tags

Comma-separated.

```
::: chips.accent
React, TypeScript, Spring Boot, PostgreSQL
:::
```

---

## Adding or removing a tab

**To rename or reorder tabs**, edit the `pages` list in `content/site.js`. That
list drives the tab bar, the numbering, the progress bar and the previous/next
buttons everywhere.

**To add a tab** called, say, *Approach*:

1. Add `{ slug: 'approach', label: 'Approach' },` to `pages` in
   `content/site.js`, in the position you want it.
2. Copy any file in `content/` to `content/approach.js` and change its `slug`
   to `'approach'`.
3. Copy any `.html` file to `approach.html` and change the two places that name
   the old slug — `data-page="..."` and the `content/....js` script tag.

**To remove a tab**, delete its line from `pages`. The file can stay; nothing
links to it any more.

---

## Images

Put image files in `assets/` and reference them from any content file:

```
![A caption, or leave this empty](assets/today.png)
```

Images are styled automatically — rounded, bordered, and never wider than the
column.

---

## Keeping sensitive content out of git

The content files are the only place client-confidential wording lives. If this
material must not be committed, add this to the repository's `.gitignore`:

```gitignore
presentation/content/*.js
!presentation/content/site.js
```

Keep a copy of the folder somewhere safe first — once ignored, git will not
protect it for you.

---

## Changing the look

`assets/theme.css` starts with the design tokens — colours, radii, spacing.
Changing `--accent` changes the accent everywhere: tabs, stat numbers, links,
timeline dots, the progress bar. The values as shipped are the product's own,
so the deck matches the application on screen next to it.
