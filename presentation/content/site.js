/* ---------------------------------------------------------------------------
   Site-wide settings and the tab bar.

   This is the only file that decides which tabs exist, what they are called
   and in what order they appear. Adding a tab is three steps:

     1. add an entry to `pages` below,
     2. copy any content/<slug>.js and change its `slug`,
     3. copy any <slug>.html in the folder above and change the two places
        that name the slug (data-page and the content/<slug>.js script tag).

   Removing a tab is just deleting its line from `pages`.
   --------------------------------------------------------------------------- */
site({
  /* Shown top-left, next to the square mark. */
  brand: 'FitTrack',
  mark: 'FT',
  tagline: 'Client presentation',

  /* Shown small, at the very bottom of every page. */
  footer: 'FitTrack · Confidential',

  /* The tabs, in presentation order. `slug` must match the content file name
     and the <slug>.html file name. `label` is what the audience reads. */
  pages: [
    { slug: 'open',               label: 'Open' },
    { slug: 'our-vision',         label: 'Our Vision' },
    { slug: 'solution',           label: 'Solution' },
    { slug: 'demo',               label: 'Demo' },
    { slug: 'solution-structure', label: 'Solution Structure' },
    { slug: 'operate',            label: 'Operate' },
    { slug: 'timeline',           label: 'Timeline' },
    { slug: 'team',               label: 'Team' },
    { slug: 'our-experience',     label: 'Our Experience' },
    { slug: 'thank-you',          label: 'Thank You' }
  ]
});
