/* =========================================================================
   Shared site engine — builds the top navigation and renders each page
   from the content object you define in the /content/*.js files.

   You normally do NOT need to touch this file.
   - To change WORDS on a page  -> edit the matching file in /content
   - To change the TAB ORDER / LABELS / add a page -> edit the PAGES list below
   ========================================================================= */

/* The tabs, in order. `id` must match the <body data-page="..."> on the page
   and the window.PAGE_ID set by the content file. `file` is the html file. */
const PAGES = [
  { id: "open",           label: "Open",            file: "open.html" },
  { id: "highlights",     label: "Our Vision",      file: "highlights.html" },
  { id: "solution",       label: "Solution",        file: "solution.html" },
  { id: "demo",           label: "Demo",            file: "demo.html" },
  { id: "structure",      label: "Structure",       file: "structure.html" },
  { id: "operate",        label: "Operate",         file: "operate.html" },
  { id: "timeline",       label: "Timeline",        file: "timeline.html" },
  { id: "team",           label: "Team",            file: "team.html" },
  { id: "our-experience", label: "Our Experience",  file: "our-experience.html" },
  { id: "thank-you",      label: "Thank You",       file: "thank-you.html" },
];

/* ---- small helpers -------------------------------------------------- */
function el(tag, cls, html) {
  const n = document.createElement(tag);
  if (cls) n.className = cls;
  if (html != null) n.innerHTML = html;
  return n;
}
/* Replace {{TOKEN}} placeholders using the values in content/config.js */
function applyParams(s) {
  s = String(s == null ? "" : s);
  const p = window.SITE_PARAMS || {};
  for (const k in p) s = s.split("{{" + k + "}}").join(p[k]);
  return s;
}
function esc(s) {
  return applyParams(s)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
/* Wrap words flagged with *stars* in the accent colour, e.g. "Our *Understanding*" */
function accentize(s) {
  return esc(s).replace(/\*(.+?)\*/g, '<span class="accent">$1</span>');
}
/* Fixed-column grid modifier, e.g. cols:2 -> "grid grid--2" (responsive) */
function gridCls(cols) { return "grid" + (cols ? " grid--" + cols : ""); }
/* Escape + params, then bold **text** — for body copy that needs emphasis. */
function richText(s) { return esc(s).replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>"); }

/* ---- navigation ----------------------------------------------------- */
function buildNav(activeId) {
  const nav = el("nav", "site-nav");
  const inner = el("div", "site-nav__inner");
  PAGES.forEach(p => {
    const a = el("a", p.id === activeId ? "is-active" : "", esc(p.label));
    a.href = p.file;
    inner.appendChild(a);
  });
  nav.appendChild(inner);
  return nav;
}

/* ---- block renderers ------------------------------------------------ */
const BLOCKS = {
  cards(b) {
    const wrap = el("div", gridCls(b.cols));
    (b.items || []).forEach((c, i) => {
      const card = el("article", "card" + (b.variant === "filled" ? " card--filled" : ""));
      if (c.num !== null && c.num !== false) {
        const num = c.num || String(i + 1).padStart(2, "0");
        card.appendChild(el("span", "card__num", esc(num)));
      }
      if (c.title) card.appendChild(el("h3", "card__title", esc(c.title)));
      (Array.isArray(c.body) ? c.body : [c.body]).forEach(p => {
        if (p) card.appendChild(el("p", "card__body", esc(p)));
      });
      if (c.bullets && c.bullets.length) {
        const ul = el("ul", "card__bullets");
        c.bullets.forEach(x => ul.appendChild(el("li", "", esc(x))));
        card.appendChild(ul);
      }
      wrap.appendChild(card);
    });
    return wrap;
  },

  stack(b) {
    const wrap = el("div", "stack");
    if (b.consumers) {
      const g = el("div", "stack__group");
      if (b.consumers.title) g.appendChild(el("div", "stack__group-title", esc(b.consumers.title)));
      const row = el("div", "stack__row");
      (b.consumers.items || []).forEach(it => row.appendChild(el("div", "stack__chip", esc(it))));
      g.appendChild(row);
      wrap.appendChild(g);
    }
    if (b.connector) wrap.appendChild(el("div", "stack__connector", "↓ " + esc(b.connector) + " ↓"));
    if (b.provider) {
      const box = el("div", "stack__group stack__provider");
      if (b.provider.title) box.appendChild(el("div", "stack__group-title", esc(b.provider.title)));
      (b.provider.rows || []).forEach(row => {
        if (row.type === "bar") {
          const bar = el("div", "stack__bar" + (row.gold ? " is-gold" : ""));
          bar.appendChild(el("span", "stack__bar-title", esc(row.title)));
          if (row.sub) bar.appendChild(el("span", "stack__bar-sub", " — " + esc(row.sub)));
          box.appendChild(bar);
        } else {
          const r = el("div", "stack__row");
          (row.items || []).forEach(cell => {
            const c = el("div", "stack__cell");
            c.appendChild(el("div", "stack__cell-title", esc(cell.title)));
            if (cell.sub) c.appendChild(el("div", "stack__cell-sub", esc(cell.sub)));
            r.appendChild(c);
          });
          box.appendChild(r);
        }
      });
      wrap.appendChild(box);
    }
    if (b.platforms && b.platforms.length) {
      const r = el("div", "stack__row stack__clouds");
      b.platforms.forEach(p => r.appendChild(el("div", "stack__cloud", esc(typeof p === "string" ? p : p.label))));
      wrap.appendChild(r);
    }
    return wrap;
  },

  steps(b) {
    const wrap = el("div", "grid");
    (b.items || []).forEach(s => {
      const card = el("article", "card step-card");
      const media = el("div", "step-card__media", s.image ? "" : esc(s.caption || "Screenshot"));
      if (s.image) media.style.backgroundImage = `url('${s.image}')`;
      card.appendChild(media);
      const foot = el("div", "step-card__foot");
      foot.appendChild(el("span", "step-card__label", esc(s.label || "Step")));
      foot.appendChild(el("p", "step-card__text", esc(s.body)));
      card.appendChild(foot);
      wrap.appendChild(card);
    });
    return wrap;
  },

  panel(b) {
    const p = el("div", "panel");
    if (b.title) p.appendChild(el("div", "panel__title", esc(b.title)));
    if (b.subtitle) p.appendChild(el("p", "panel__subtitle", esc(b.subtitle)));
    if (b.items && b.items.length) {
      const ul = el("ul", "panel__items");
      b.items.forEach(it => {
        const label = typeof it === "string" ? it : it.label;
        const gold = typeof it === "object" && it.gold;
        ul.appendChild(el("li", gold ? "is-gold" : "", esc(label)));
      });
      p.appendChild(ul);
    }
    return p;
  },

  tags(b) {
    const wrap = el("div", "text-block");
    if (b.title) wrap.appendChild(el("h3", "section-title plain", esc(b.title)));
    const ul = el("ul", "tags");
    (b.items || []).forEach(t => ul.appendChild(el("li", "", esc(t))));
    wrap.appendChild(ul);
    return wrap;
  },

  timeline(b) {
    const ul = el("ul", "timeline");
    (b.items || []).forEach(t => {
      const li = el("li");
      li.appendChild(el("div", "timeline__date", esc(t.date)));
      li.appendChild(el("div", "timeline__title", esc(t.title)));
      li.appendChild(el("p", "timeline__body", esc(t.body)));
      ul.appendChild(li);
    });
    return ul;
  },

  team(b) {
    const wrap = el("div", "team-grid");
    (b.items || []).forEach(m => {
      const card = el("article", "team-card");
      const photo = el("div", "team-card__photo");
      const showInitial = () => {
        const initial = (applyParams(m.name) || "").replace(/[^A-Za-z0-9]/g, "").charAt(0);
        photo.textContent = initial ? initial.toUpperCase() : "•";
      };
      if (m.photo) {
        const img = el("img", "team-card__img");
        img.src = m.photo;
        img.alt = applyParams(m.name) || "";
        img.addEventListener("error", () => {
          img.remove();
          showInitial();
          photo.title = "Image not found: " + m.photo + " — check the path in the content file.";
        });
        photo.appendChild(img);
      } else {
        showInitial();
      }
      card.appendChild(photo);
      card.appendChild(el("h3", "team-card__name", esc(m.name)));
      card.appendChild(el("p", "team-card__role", esc(m.role)));
      wrap.appendChild(card);
    });
    return wrap;
  },

  columns(b) {
    const wrap = el("div", gridCls(b.cols));
    (b.items || []).forEach(c => {
      const card = el("article", "card");
      if (c.title) card.appendChild(el("h3", "card__title", esc(c.title)));
      card.appendChild(el("p", "card__body", esc(c.body)));
      wrap.appendChild(card);
    });
    return wrap;
  },

  /* Coloured function cards: a coloured header + a list of line items and/or
     chips. Each item: { color, emoji, header, sub, lines:[], chips:[], body }. */
  deck(b) {
    const wrap = el("div", gridCls(b.cols));
    (b.items || []).forEach(card => {
      const c = el("article", "deck-card deck-card--" + (card.color || "maroon"));
      const head = el("div", "deck-card__head");
      if (card.emoji) head.appendChild(el("div", "deck-card__emoji", esc(card.emoji)));
      head.appendChild(el("div", "deck-card__title", esc(card.header)));
      if (card.sub) head.appendChild(el("div", "deck-card__sub", esc(card.sub)));
      c.appendChild(head);
      if (card.lines && card.lines.length) {
        const list = el("div", "deck-card__list");
        card.lines.forEach(x => list.appendChild(el("div", "deck-card__line", esc(x))));
        c.appendChild(list);
      }
      if (card.body) c.appendChild(el("p", "deck-card__body", esc(card.body)));
      if (card.chips && card.chips.length) {
        const chips = el("div", "deck-card__chips");
        card.chips.forEach(x => chips.appendChild(el("span", "chip", esc(x))));
        c.appendChild(chips);
      }
      wrap.appendChild(c);
    });
    return wrap;
  },

  /* Full-width dark support bar (e.g. Operate Level 3). */
  supportbar(b) {
    const bar = el("div", "support-bar");
    if (b.icon) bar.appendChild(el("div", "support-bar__icon", esc(b.icon)));
    const txt = el("div");
    txt.appendChild(el("div", "support-bar__title", esc(b.title)));
    if (b.body) txt.appendChild(el("div", "support-bar__text", esc(b.body)));
    bar.appendChild(txt);
    return bar;
  },

  /* Compact program org chart (Team). Regions: governance, sme, leadership,
     governancePods, delivery {streamAligned, enabling, foundation}. */
  orgchart(b) {
    const boxRow = (arr, cls) => {
      const r = el("div", "org__boxrow");
      (arr || []).forEach(x => r.appendChild(el("div", "org-box" + (cls ? " " + cls : ""), esc(x))));
      return r;
    };
    const region = (label, cls) => {
      const r = el("div", "org__region" + (cls ? " " + cls : ""));
      if (label) r.appendChild(el("div", "org__label", esc(label)));
      return r;
    };
    const wrap = el("div", "org");

    if (b.governance) {
      const g = region(b.governance.label, "org__gov");
      g.appendChild(boxRow(b.governance.boxes));
      wrap.appendChild(g);
    }
    if (b.sme) {
      const s = region(b.sme.label, "org__sme");
      const col = el("div", "org__smecol");
      (b.sme.boxes || []).forEach(x => col.appendChild(el("div", "org-box", esc(x))));
      s.appendChild(col);
      if (b.sme.note) s.appendChild(el("p", "org__note", esc(b.sme.note)));
      wrap.appendChild(s);
    }

    const main = el("div", "org__main");
    if (b.leadership) {
      const l = region(b.leadership.label);
      l.appendChild(boxRow(b.leadership.boxes));
      main.appendChild(l);
    }
    if (b.governancePods) {
      const gp = region(b.governancePods.label);
      const inner = el("div", "org__gp");
      if (b.governancePods.desc) inner.appendChild(el("p", "org__desc", esc(b.governancePods.desc)));
      inner.appendChild(boxRow(b.governancePods.boxes, "org-box--soft"));
      gp.appendChild(inner);
      main.appendChild(gp);
    }
    if (b.delivery) {
      const d = region(b.delivery.label, "org__delivery-region");
      const grid = el("div", "org__delivery");
      const left = el("div", "org__delivery-left");
      [b.delivery.enabling, b.delivery.foundation].forEach(sec => {
        if (!sec) return;
        const box = el("div", "org__subregion");
        box.appendChild(el("div", "org__subtitle", esc(sec.label)));
        box.appendChild(boxRow(sec.boxes));
        left.appendChild(box);
      });
      const right = el("div", "org__stream");
      if (b.delivery.streamAligned) {
        right.appendChild(el("div", "org__subtitle org__subtitle--center", esc(b.delivery.streamAligned.label)));
        const bars = el("div", "org__streambars");
        (b.delivery.streamAligned.boxes || []).forEach(x => bars.appendChild(el("div", "org-bar", esc(x))));
        right.appendChild(bars);
      }
      grid.appendChild(left);
      grid.appendChild(right);
      d.appendChild(grid);
      main.appendChild(d);
    }
    wrap.appendChild(main);
    return wrap;
  },

  /* Credential / case-study cards: image + title/subtitle + tags + body. */
  credentials(b) {
    const wrap = el("div", "cred");
    (b.items || []).forEach(it => {
      const card = el("article", "cred-card");
      const left = el("div", "cred-card__left");
      const media = el("div", "cred-card__media" + (it.image ? "" : " is-empty"));
      if (it.image) media.style.backgroundImage = `url('${it.image}')`;
      else media.textContent = "Image placeholder";
      left.appendChild(media);
      left.appendChild(el("h3", "cred-card__title", esc(it.title)));
      if (it.subtitle) left.appendChild(el("p", "cred-card__sub", esc(it.subtitle)));
      card.appendChild(left);
      const right = el("div", "cred-card__right");
      if (it.tags && it.tags.length) {
        const t = el("div", "cred-card__tags");
        it.tags.forEach(x => t.appendChild(el("span", "cred-card__tag", esc(x))));
        right.appendChild(t);
      }
      right.appendChild(el("p", "cred-card__text", richText(it.body)));
      card.appendChild(right);
      wrap.appendChild(card);
    });
    return wrap;
  },

  /* High-level component placement: a sovereign control plane on top, its
     capability row, a funnel, and cloud-estate columns whose Workload & Data
     box carries component chips (active or greyed-off). */
  placement(b) {
    const wrap = el("div", "plc");
    const cp = b.controlPlane || {};
    const bar = el("div", "plc__cp");
    bar.appendChild(el("div", "plc__cptitle", esc(cp.title)));
    const cprow = el("div", "plc__cprow");
    (cp.boxes || []).forEach(x => cprow.appendChild(el("div", "plc__cpbox", esc(x))));
    bar.appendChild(cprow);
    if (b.revealsRest) {
      bar.classList.add("plc__cp--btn");
      bar.appendChild(el("div", "plc__cphint", "▾  Click to reveal the solution architecture"));
    }
    wrap.appendChild(bar);

    wrap.appendChild(el("div", "plc__funnel"));

    const clouds = el("div", "plc__clouds");
    (b.clouds || []).forEach(c => {
      const col = el("div", "plc__col" + (c.hi ? " plc__col--hi" : ""));
      const head = el("div", "plc__cloudhead");
      head.appendChild(el("span", null, esc(c.name)));
      if (c.hi) head.appendChild(el("span", "plc__sov", "Sovereign"));
      col.appendChild(head);
      (b.layers || []).forEach(L => col.appendChild(el("div", "plc__layer", esc(L))));
      const wl = el("div", "plc__layer plc__wl");
      wl.appendChild(el("div", "plc__wllabel", esc(b.workloadLabel || "Workload & Data")));
      if ((c.components || []).length) {
        const chips = el("div", "plc__chips");
        (c.components || []).forEach(comp => {
          const off = typeof comp === "object" ? comp.off : false;
          const label = typeof comp === "object" ? comp.c : comp;
          chips.appendChild(el("span", "plc__chip" + (off ? " is-off" : ""), esc(label)));
        });
        wl.appendChild(chips);
      }
      col.appendChild(wl);
      clouds.appendChild(col);
    });
    wrap.appendChild(clouds);
    return wrap;
  },

  /* Platform-provider relationship diagram that expands into the operating
     model grid. Click the "Operating Model" bar to expand; Back to return. */
  providermodel(b) {
    const wrap = el("div", "pp");
    if (b.heading) wrap.appendChild(el("h3", "pp__title", accentize(b.heading)));
    const stage = el("div", "pp__stage");

    /* --- overview: relationship diagram + notes --- */
    const overview = el("div", "pp__overview");
    const diagram = el("div", "pp__diagram");
    const d = b.diagram || {};

    const cons = el("div", "pp__group pp__group--consumers");
    cons.appendChild(el("div", "pp__glabel", esc((d.consumers || {}).title || "Service Consumers")));
    const consRow = el("div", "pp__row");
    ((d.consumers || {}).boxes || []).forEach(x => consRow.appendChild(el("div", "pp__box pp__box--taupe", esc(x))));
    cons.appendChild(consRow);
    diagram.appendChild(cons);
    diagram.appendChild(el("div", "pp__conn", ""));

    diagram.appendChild(el("div", "pp__box pp__box--teal pp__facing", esc(d.facing || "Customer Facing Services & Platform OS")));

    const prov = el("div", "pp__group pp__group--provider");
    prov.appendChild(el("div", "pp__glabel", esc((d.provider || {}).title || "Platform Provider")));
    ((d.provider || {}).layers || []).forEach((L, i) => {
      const isOm = i === 0;
      const bar = el("div", "pp__bar" + (isOm ? " pp__om" : ""));
      bar.appendChild(el("span", null, esc(L)));
      if (isOm) bar.appendChild(el("span", "pp__omhint", "Expand ⤢"));
      prov.appendChild(bar);
    });
    diagram.appendChild(prov);
    diagram.appendChild(el("div", "pp__conn", ""));

    const ext = el("div", "pp__row pp__row--ext");
    (d.external || []).forEach(e => {
      const box = el("div", "pp__ext");
      box.appendChild(el("div", "pp__extlabel", esc(e.title)));
      box.appendChild(el("div", "pp__box pp__box--taupe", esc(e.box)));
      ext.appendChild(box);
    });
    diagram.appendChild(ext);
    overview.appendChild(diagram);

    const notes = el("div", "pp__notes");
    (b.notes || []).forEach(p => notes.appendChild(el("p", null, richText(p))));
    overview.appendChild(notes);
    stage.appendChild(overview);

    /* --- detail: operating model grid --- */
    const detail = el("div", "pp__detail");
    const back = el("button", "backlink pp__back", "← Back to the overview");
    detail.appendChild(back);
    const om = b.operatingModel || {};
    if (om.title) detail.appendChild(el("h3", "pp__omtitle", accentize(om.title)));
    const grid = el("div", "om");
    (om.rows || []).forEach(row => {
      const r = el("div", "om__row");
      if (row.tag) r.appendChild(el("div", "om__tag", esc(row.tag)));
      const panels = el("div", "om__panels");
      (row.panels || []).forEach(p => {
        const panel = el("div", "om__panel");
        panel.style.flexGrow = p.w || 1;
        panel.appendChild(el("div", "om__ptitle", esc(p.title)));
        const cells = el("div", "om__cells");
        (p.cells || []).forEach(c => cells.appendChild(el("div", "om__cell", esc(c))));
        panel.appendChild(cells);
        panels.appendChild(panel);
      });
      r.appendChild(panels);
      grid.appendChild(r);
    });
    if (om.footer && om.footer.length) {
      const f = el("div", "om__footer");
      om.footer.forEach(x => f.appendChild(el("div", "om__fcell", esc(x))));
      grid.appendChild(f);
    }
    detail.appendChild(grid);
    stage.appendChild(detail);
    wrap.appendChild(stage);

    const expand = () => { wrap.classList.add("is-expanded"); };
    prov.querySelectorAll(".pp__om").forEach(x => x.addEventListener("click", expand));
    back.addEventListener("click", () => wrap.classList.remove("is-expanded"));
    return wrap;
  },

  /* -------- A row of headline figures: { value, label }. Every tile is the
     same width, so a row of three or of seven still lines up. */
  statrow(b) {
    const wrap = el("div", "statrow");
    (b.items || []).forEach(it => {
      const cell = el("div", "statrow__item");
      cell.appendChild(el("div", "statrow__value", esc(it.value)));
      cell.appendChild(el("div", "statrow__label", esc(it.label)));
      wrap.appendChild(cell);
    });
    return wrap;
  },

  /* -------- Four cards around a central logo medallion, the way a vendor
     slide puts the product in the middle of what it gives you.
     { logo, logoAlt, logoCaption, items: [{ title, body }] }
     On narrow screens the medallion drops above the cards. */
  hubgrid(b) {
    const wrap = el("div", "hub");
    if (b.logo) {
      const medal = el("div", "hub__medallion");
      const img = el("img", "hub__logo");
      img.src = b.logo;
      img.alt = applyParams(b.logoAlt || "");
      medal.appendChild(img);
      if (b.logoCaption) medal.appendChild(el("div", "hub__caption", esc(b.logoCaption)));
      wrap.appendChild(medal);
    }
    const grid = el("div", "hub__grid");
    (b.items || []).forEach(it => {
      const card = el("article", "hub__card");
      card.appendChild(el("h3", "hub__title", esc(it.title)));
      if (it.body) card.appendChild(el("p", "hub__body", esc(it.body)));
      grid.appendChild(card);
    });
    wrap.appendChild(grid);
    return wrap;
  },

  /* -------- Two brands side by side with a rule between them, over a
     partnership title and tagline. { left, logo, logoAlt, right, title,
     tagline } — `left` and `right` are words, `logo` is an image path. */
  lockup(b) {
    const wrap = el("div", "lockup");
    const row = el("div", "lockup__brands");
    if (b.left) row.appendChild(el("span", "lockup__word", esc(b.left)));
    if (b.logo || b.right) {
      row.appendChild(el("span", "lockup__rule"));
      const mark = el("span", "lockup__mark");
      if (b.logo) {
        const img = el("img", "lockup__logo");
        img.src = b.logo;
        img.alt = applyParams(b.logoAlt || "");
        mark.appendChild(img);
      }
      if (b.right) mark.appendChild(el("span", "lockup__word", esc(b.right)));
      row.appendChild(mark);
    }
    wrap.appendChild(row);
    if (b.title) wrap.appendChild(el("div", "lockup__title", esc(b.title)));
    if (b.tagline) wrap.appendChild(el("div", "lockup__tag", esc(b.tagline)));
    return wrap;
  },

  text(b) {
    const wrap = el("div", "text-block");
    if (b.heading) wrap.appendChild(el("h3", null, esc(b.heading)));
    (Array.isArray(b.body) ? b.body : [b.body]).forEach(p => {
      if (p) wrap.appendChild(el("p", null, esc(p)));
    });
    if (b.bullets && b.bullets.length) {
      const ul = el("ul", "text-block__bullets");
      b.bullets.forEach(x => ul.appendChild(el("li", "", esc(x))));
      wrap.appendChild(ul);
    }
    return wrap;
  },

  image(b) {
    const fig = el("figure", "card");
    fig.style.padding = "0"; fig.style.overflow = "hidden";
    if (b.src) {
      const img = el("img");
      img.src = b.src; img.alt = b.caption || "";
      img.style.width = "100%"; img.style.display = "block";
      /* A wrong path should say so, not show a broken-image icon on a projector. */
      img.addEventListener("error", () => {
        img.remove();
        fig.insertBefore(el("div", "img-placeholder", "Image not found: " + esc(b.src)), fig.firstChild);
      });
      fig.appendChild(img);
    } else {
      /* No file yet — a labelled slot, so the page never looks broken while
         you are still collecting artwork. */
      fig.appendChild(el("div", "img-placeholder",
        esc(b.placeholder || "Image placeholder")));
    }
    if (b.caption) {
      const cap = el("figcaption", "card__body");
      cap.style.padding = "16px 22px"; cap.textContent = b.caption;
      fig.appendChild(cap);
    }
    return fig;
  },

  /* Swappable video slots for the live demo. Click a video (or its ⛶
     button) to expand it full screen; Esc exits. */
  video(b) {
    const wrap = el("div", "grid");
    (b.items || []).forEach(v => {
      const card = el("article", "card video-card");
      const vid = document.createElement("video");
      vid.controls = true; vid.preload = "metadata";
      if (v.poster) vid.poster = v.poster;
      if (v.src) {
        const s = document.createElement("source");
        s.src = v.src; vid.appendChild(s);
        const stage = el("div", "video-card__stage");
        stage.appendChild(vid);
        const goFull = () => {
          if (document.fullscreenElement) return;
          if (vid.requestFullscreen) vid.requestFullscreen();
          else if (vid.webkitRequestFullscreen) vid.webkitRequestFullscreen();
          if (vid.paused) { const p = vid.play(); if (p && p.catch) p.catch(() => {}); }
        };
        vid.addEventListener("click", goFull);
        const fs = el("button", "video-card__fs", "⛶");
        fs.title = "Full screen";
        fs.addEventListener("click", goFull);
        stage.appendChild(fs);
        card.appendChild(stage);
      } else {
        card.appendChild(el("div", "video-card__empty",
          "▶ " + esc(v.caption || "Video slot") +
          "<br><small>Drop an .mp4 in assets/video and set its path here.</small>"));
      }
      if (v.caption) card.appendChild(el("div", "video-card__cap", esc(v.caption)));
      if (v.body) card.appendChild(el("p", "video-card__body", esc(v.body)));
      wrap.appendChild(card);
    });
    return wrap;
  },

  /* A back / cross link to another view (used inside multi-view pages). */
  link(b) {
    const a = el("a", "backlink", (b.arrow === false ? "" : "← ") + esc(b.label));
    a.href = "#" + (b.to || "");
    return a;
  },

  /* Layered architecture diagram: a set of titled bands, each holding cells
     and/or nested groups. Cells and band/group headers may link to a view. */
  bands(b) {
    /* `collapsible: false` makes the layers static — no chevron, no click.
       Use it where the whole stack is the point and hiding a layer would
       break the story. */
    const canCollapse = b.collapsible !== false;
    const wrap = el("div", "bands" + (canCollapse ? "" : " bands--static"));
    /* legend entries can carry a `tip` — shown as a rich hover card over any
       badge with that key, both in the legend and on the cells */
    const tipMap = {};
    (b.legend || []).forEach(it => {
      if (it.tip) tipMap[String(it.key)] = { label: applyParams(it.label), tip: applyParams(it.tip) };
    });
    window.__badgeTips = Object.assign(window.__badgeTips || {}, tipMap);
    if (b.legend) {
      const lg = el("div", "legend");
      b.legend.forEach(it => {
        const item = el("span", "legend__item");
        const bd = el("span", "badge badge--" + String(it.key).toLowerCase(), esc(it.key));
        if (it.tip) bd.setAttribute("data-tipkey", String(it.key));
        item.appendChild(bd);
        item.appendChild(el("span", "legend__txt", esc(it.label)));
        lg.appendChild(item);
      });
      wrap.appendChild(lg);
    }
    (b.bands || []).forEach(band => {
      const sec = el("div", "band");
      const head = bandHead(band, "band__head");
      sec.appendChild(head);
      const bodyEl = el("div", "band__body");
      const inner = el("div", "band__inner");
      bodyEl.appendChild(inner);
      if (band.note) inner.appendChild(el("p", "band__note", esc(band.note)));
      if (band.cells && band.cells.length) {
        const grid = el("div", "band__grid");
        band.cells.forEach(cell => grid.appendChild(bandCell(cell)));
        inner.appendChild(grid);
      }
      (band.groups || []).forEach(g => {
        const sub = el("div", "band__sub");
        const sh = bandHead(g, "band__subhead");
        sub.appendChild(sh);
        const sBody = el("div", "band__body");
        const sInner = el("div", "band__inner");
        sBody.appendChild(sInner);
        if (g.note) sInner.appendChild(el("p", "band__note", esc(g.note)));
        const sg = el("div", "band__grid");
        (g.cells || []).forEach(cell => sg.appendChild(bandCell(cell)));
        sInner.appendChild(sg);
        sub.appendChild(sBody);
        if (canCollapse) collapsible(sub, sh, g.collapsed);
        inner.appendChild(sub);
      });
      sec.appendChild(bodyEl);
      if (canCollapse) collapsible(sec, head, band.collapsed);
      wrap.appendChild(sec);
    });
    return wrap;
  },

  /* Gantt: month scale, coloured phase bars, release filter, expandable tasks.
     rows: { label, start, end, text, color, groups:[], milestone, milestoneLabel,
             phase, tasks:[{ label, start, end, text, color }] } */
  gantt(b) {
    const n = b.months || 12;
    const pct = m => (Math.max(0, Math.min(n, m)) / n) * 100;
    const wrap = el("div", "gantt-wrap");

    if (b.hint) wrap.appendChild(el("p", "gantt__hint", esc(b.hint)));

    const gantt = el("div", "gantt");
    const rowEls = [];

    /* entry animation: bars grow in, milestone stars pop; replayable */
    function runAnim() {
      wrap.classList.remove("is-anim");
      void wrap.offsetWidth;
      let i = 0, j = 0;
      wrap.querySelectorAll(".gantt__bar").forEach(x => { x.style.animationDelay = (0.1 + (i++) * 0.09) + "s"; });
      wrap.querySelectorAll(".gantt__star").forEach(x => { x.style.animationDelay = (0.5 + (j++) * 0.15) + "s"; });
      wrap.classList.add("is-anim");
    }

    // filter buttons + replay control
    const fbar = el("div", "gantt-filters");
    if (b.filters && b.filters.length) {
      b.filters.forEach(f => {
        const btn = el("button", "gantt-filter", esc(f.label));
        btn.dataset.filter = f.id;
        btn.addEventListener("click", () => {
          fbar.querySelectorAll(".gantt-filter:not(.gantt-filter--replay)").forEach(
            x => x.classList.toggle("is-active", x === btn));
          rowEls.forEach(({ el: rEl, groups }) => {
            const show = f.id === "all" || (groups || []).indexOf(f.id) !== -1;
            rEl.style.display = show ? "" : "none";
          });
          runAnim();
        });
        fbar.appendChild(btn);
      });
      (fbar.firstChild || {}).className = "gantt-filter is-active";
    }
    const replayBtn = el("button", "gantt-filter gantt-filter--replay", "↻ Replay");
    replayBtn.addEventListener("click", runAnim);
    fbar.appendChild(replayBtn);
    wrap.appendChild(fbar);

    // header
    const head = el("div", "gantt__row gantt__row--head");
    head.appendChild(el("div", "gantt__label", ""));
    const scale = el("div", "gantt__track");
    (b.scale || Array.from({ length: n }, (_, i) => "M" + (i + 1))).forEach(lbl =>
      scale.appendChild(el("div", "gantt__tick", esc(lbl))));
    head.appendChild(scale);
    gantt.appendChild(head);

    const SPRINT_ICON = '<svg class="gicon" viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-2.64-6.36"/><polyline points="21 3 21 8 16 8"/></svg>';
    function bar(o, cls) {
      const el2 = el("div", "gantt__bar" + (o.color ? " c-" + o.color : "") + (cls || ""));
      el2.style.left = pct(o.start) + "%";
      el2.style.width = (pct(o.end != null ? o.end : n) - pct(o.start)) + "%";
      if (o.icon === "sprint" || o.text) {
        el2.innerHTML = (o.icon === "sprint" ? SPRINT_ICON : "") +
          (o.text ? "<span>" + esc(o.text) + "</span>" : "");
      }
      return el2;
    }
    function milestoneEl(o) {
      const p = pct(o.milestone);
      const m = el("div", "gantt__milestone" + (p >= 88 ? " is-end" : ""));
      m.style.left = p + "%";
      m.appendChild(el("span", "gantt__star", "★"));
      if (o.milestoneLabel) m.appendChild(el("span", "gantt__mlabel", esc(o.milestoneLabel)));
      return m;
    }

    (b.rows || []).forEach(r => {
      const hasTasks = r.tasks && r.tasks.length;
      const row = el("div", "gantt__row");
      const label = el("div", "gantt__label" + (r.phase ? " gantt__label--phase" : ""));
      if (hasTasks) label.appendChild(el("span", "gantt__chev", "▸"));
      label.appendChild(el("span", null, esc(r.label)));
      row.appendChild(label);
      const track = el("div", "gantt__track gantt__track--bars");
      if (r.start != null) track.appendChild(bar(r));
      if (r.milestone != null) track.appendChild(milestoneEl(r));
      row.appendChild(track);
      gantt.appendChild(row);
      rowEls.push({ el: row, groups: r.groups });

      if (hasTasks) {
        const tasks = el("div", "gantt__tasks");
        r.tasks.forEach(t => {
          const trow = el("div", "gantt__task");
          trow.appendChild(el("div", "gantt__label", esc(t.label)));
          const ttrack = el("div", "gantt__track gantt__track--bars");
          if (t.start != null) ttrack.appendChild(bar(t));
          if (t.milestone != null) ttrack.appendChild(milestoneEl(t));
          trow.appendChild(ttrack);
          tasks.appendChild(trow);
        });
        gantt.appendChild(tasks);
        rowEls.push({ el: tasks, groups: r.groups });
        label.style.cursor = "pointer";
        label.addEventListener("click", () => {
          row.classList.toggle("is-open");
          tasks.classList.toggle("is-open");
        });
      }
    });

    wrap.appendChild(gantt);
    runAnim();
    return wrap;
  },

  /* -------- Animated "vision" diagram (Highlights stage 1): text on the
     left, consumers dropping onto the provider plate on the right. */
  vision(b) {
    const wrap = el("div", "vision");
    wrap.dataset.anim = "vision";
    const left = el("div");
    left.appendChild(el("h2", "vision__h rv", accentize(b.heading)));
    (Array.isArray(b.body) ? b.body : [b.body]).forEach(p => {
      if (p) left.appendChild(el("p", "vision__p rv", esc(p)));
    });
    if (b.keywords && b.keywords.length) {
      const kws = el("div", "vision__kws");
      b.keywords.forEach(k => kws.appendChild(el("span", "kw rv", esc(k))));
      left.appendChild(kws);
    }
    wrap.appendChild(left);

    const prov = el("div", "provider");
    prov.appendChild(el("div", "prov-lbl", esc(b.consumersLabel || "Platform consumers & users")));
    const cons = el("div", "consumers");
    (b.consumers || []).forEach(c => cons.appendChild(el("div", "consumer", esc(c))));
    prov.appendChild(cons);
    const drop = el("div", "dropzone");
    for (let i = 0; i < 4; i++) {
      const d = el("div", "dl");
      d.style.animationDelay = (i * 0.4) + "s";
      drop.appendChild(d);
    }
    prov.appendChild(drop);
    const plate = el("div", "platebox");
    plate.appendChild(el("div", "platebox__title", esc(b.plateTitle)));
    const fronts = el("div", "fronts");
    (b.fronts || []).forEach(f => fronts.appendChild(el("div", "front", esc(f))));
    plate.appendChild(fronts);
    const rails = el("div", "railbars");
    (b.rails || []).forEach(r => rails.appendChild(el("div", "railbar", esc(r))));
    plate.appendChild(rails);
    if (b.estates && b.estates.length) {
      plate.appendChild(el("div", "estates-lbl", esc(b.estatesLabel || "Infrastructure — your existing estates")));
      const est = el("div", "estates");
      b.estates.forEach(x => est.appendChild(el("div", "estate", esc(x))));
      plate.appendChild(est);
    }
    prov.appendChild(plate);
    wrap.appendChild(prov);
    return wrap;
  },

  /* -------- AI-in-SDLC orbit ring + capability list (Highlights stage 3). */
  sdlc(b) {
    const outer = el("div");
    if (b.heading) outer.appendChild(el("h2", "stage-h", accentize(b.heading)));
    const wrap = el("div", "ainative");
    wrap.dataset.anim = "sdlc";
    const sw = el("div", "sdlc-wrap");
    const s = el("div", "sdlc");
    s.innerHTML = '<svg viewBox="0 0 200 200"><circle class="ring" cx="100" cy="100" r="86"/><circle class="ring-hot" cx="100" cy="100" r="86" transform="rotate(-90 100 100)"/></svg>';
    const core = el("div", "sdlc__core");
    core.appendChild(el("b", null, esc(b.coreTitle || "AI-in-SDLC")));
    core.appendChild(el("span", null, esc(b.coreSub || "Agents at every phase")));
    s.appendChild(core);
    const phases = b.phases || [];
    phases.forEach((p, k) => {
      const ang = (k / phases.length) * Math.PI * 2 - Math.PI / 2;
      const ph = el("div", "ph", esc(p));
      ph.style.left = (50 + 43 * Math.cos(ang)) + "%";
      ph.style.top = (50 + 43 * Math.sin(ang)) + "%";
      s.appendChild(ph);
    });
    s.appendChild(el("div", "agent"));
    sw.appendChild(s);
    wrap.appendChild(sw);
    const caps = el("div", "caps");
    (b.caps || []).forEach(c => {
      const cap = el("div", "cap");
      cap.appendChild(el("div", "cap__cat", esc(c.cat)));
      const d = el("div");
      d.appendChild(el("b", null, esc(c.title)));
      d.appendChild(el("p", null, esc(c.body)));
      cap.appendChild(d);
      cap.addEventListener("click", () => cap.classList.toggle("is-lit"));
      caps.appendChild(cap);
    });
    wrap.appendChild(caps);
    outer.appendChild(wrap);
    return outer;
  },

  /* -------- Accelerator cards with taglines and impact chips (stage 4). */
  accel(b) {
    const wrap = el("div", "accel");
    wrap.dataset.anim = "accel";
    if (b.heading) wrap.appendChild(el("h2", "stage-h", accentize(b.heading)));
    const grid = el("div", "accel-grid");
    (b.items || []).forEach(a => {
      const card = el("div", "acc");
      const media = el("div", "acc__media" + (a.image ? "" : " is-empty"));
      if (a.image) media.style.backgroundImage = "url('" + a.image + "')";
      else media.textContent = "Image placeholder";
      card.appendChild(media);
      if (a.tagline) card.appendChild(el("div", "acc__tag", esc(a.tagline)));
      card.appendChild(el("b", null, esc(a.title)));
      card.appendChild(el("p", null, esc(a.body)));
      if (a.impact) card.appendChild(el("div", "acc__impact", esc(a.impact)));
      grid.appendChild(card);
    });
    wrap.appendChild(grid);
    if (b.closing) wrap.appendChild(el("p", "accel-close", accentize(b.closing)));
    return wrap;
  },

  /* -------- Accordion: numbered rows that expand on click. */
  accordion(b) {
    const wrap = el("div", "acc-list");
    (b.items || []).forEach((it, i) => {
      const item = el("div", "acc-item");
      const head = el("button", "acc-item__head");
      head.appendChild(el("span", "acc-item__num", esc(it.num || String(i + 1).padStart(2, "0"))));
      head.appendChild(el("span", "acc-item__title", esc(it.title)));
      head.appendChild(el("span", "acc-item__chev", "▾"));
      const body = el("div", "band__body");
      const inner = el("div", "band__inner");
      inner.appendChild(el("p", "acc-item__body", esc(it.body)));
      body.appendChild(inner);
      item.appendChild(head);
      item.appendChild(body);
      head.addEventListener("click", () => item.classList.toggle("is-open"));
      if (it.open) item.classList.add("is-open");
      wrap.appendChild(item);
    });
    return wrap;
  },

  /* -------- Full-width call-to-action panel linking to another page. */
  cta(b) {
    const a = el("a", "cta");
    a.href = b.href || "#";
    const t = el("div", "cta__txt");
    t.appendChild(el("div", "cta__title", accentize(b.title)));
    if (b.body) t.appendChild(el("p", "cta__body", esc(b.body)));
    a.appendChild(t);
    a.appendChild(el("span", "cta__btn", esc(b.label || "Continue →")));
    return a;
  },

  /* -------- Escalation tiers (Operate): connected support levels with a
     level badge, description and chips. Colours: maroon/dark/blue/teal/
     gold/slate. Animates in on load. */
  tiers(b) {
    const wrap = el("div", "tiers");
    (b.items || []).forEach((t, i) => {
      const row = el("div", "tier tier--" + (t.color || "maroon"));
      row.style.animationDelay = (0.1 + i * 0.14) + "s";
      row.appendChild(el("div", "tier__badge", esc(t.level)));
      const card = el("div", "tier__card");
      const head = el("div", "tier__head");
      head.appendChild(el("h3", "tier__title", esc(t.title)));
      if (t.tag) head.appendChild(el("span", "tier__tag", esc(t.tag)));
      card.appendChild(head);
      if (t.body) card.appendChild(el("p", "tier__body", esc(t.body)));
      if (t.chips && t.chips.length) {
        const chips = el("div", "tier__chips");
        t.chips.forEach(x => chips.appendChild(el("span", "chip", esc(x))));
        card.appendChild(chips);
      }
      row.appendChild(card);
      wrap.appendChild(row);
    });
    return wrap;
  },

  /* -------- Clickable pod boxes (Team): each opens a pop-up with the pod's
     member list. Items: { color, title, sub, members: [{ name, role }] }. */
  pods(b) {
    const wrap = el("div", gridCls(b.cols || 4));
    (b.items || []).forEach(p => {
      const card = el("article", "deck-card deck-card--" + (p.color || "maroon") + " pod");
      const head = el("div", "deck-card__head");
      head.appendChild(el("div", "deck-card__title", esc(p.title)));
      if (p.sub) head.appendChild(el("div", "deck-card__sub", esc(p.sub)));
      card.appendChild(head);
      const foot = el("div", "pod__foot");
      foot.appendChild(el("span", "pod__count", (p.members || []).length + " member" + ((p.members || []).length === 1 ? "" : "s")));
      foot.appendChild(el("span", "pod__go", "View team →"));
      card.appendChild(foot);
      card.addEventListener("click", () => openPodModal(p));
      wrap.appendChild(card);
    });
    return wrap;
  },

  /* -------- Animated provisioning journey (Solution). Three switchable
     visualizations of the same steps: Architecture (blocks light up),
     Flow (a metro-style path), Orbit (a radial dial). */
  journey(b) {
    const wrap = el("div", "journey");
    const steps = b.steps || [];
    let cur = -1, playing = null, viz = null;
    let vizMode = b.defaultViz || "arch";

    const controls = el("div", "jour-controls");
    const btnBack = el("button", "jbtn", "‹ Back");
    const btnStep = el("button", "jbtn", "Step ›");
    const btnPlay = el("button", "jbtn jbtn--play", "▶&nbsp; Play the journey");
    const btnReset = el("button", "jbtn", "Reset");
    [btnBack, btnStep, btnPlay, btnReset].forEach(x => controls.appendChild(x));
    const vizBar = el("div", "jviz");
    vizBar.appendChild(el("span", "jviz__lbl", "View:"));
    [["video", "Video"], ["network", "Network"], ["arch", "Architecture"], ["flow", "Flow map"], ["orbit", "Orbit"]].forEach(v => {
      const btn = el("button", "jviz__btn" + (v[0] === vizMode ? " is-active" : ""), esc(v[1]));
      btn.addEventListener("click", () => {
        vizMode = v[0];
        vizBar.querySelectorAll(".jviz__btn").forEach(x => x.classList.toggle("is-active", x === btn));
        buildViz();
        btn.blur();
      });
      vizBar.appendChild(btn);
    });
    controls.appendChild(vizBar);
    wrap.appendChild(controls);

    const prog = el("div", "jour-progress");
    const progFill = el("div", "jour-progress__fill");
    prog.appendChild(progFill);
    wrap.appendChild(prog);

    const body = el("div", "jour-body");
    const canvas = el("div", "jcanvas");
    body.appendChild(canvas);
    const log = el("div", "jlog");
    log.appendChild(el("h4", null, esc(b.logTitle || "Activation sequence")));
    const entries = el("div", "jlog__entries");
    log.appendChild(entries);
    const fin = el("div", "jlog__final", "✓ " + esc(b.final || "Journey complete."));
    log.appendChild(fin);
    body.appendChild(log);
    wrap.appendChild(body);
    wrap.appendChild(el("p", "gantt__hint",
      "← / → step the journey · P plays / pauses · switch the View to try a different visualization. In Network view you can drag nodes and click a numbered step to jump there."));

    function buildViz() {
      canvas.innerHTML = "";
      if (vizMode === "video") viz = videoViz(canvas, b.video);
      else if (vizMode === "flow") viz = flowViz(canvas, steps);
      else if (vizMode === "orbit") viz = orbitViz(canvas, steps);
      else if (vizMode === "network") viz = networkViz(canvas, steps, k => { stopPlay(); cur = k; render(); });
      else viz = archViz(canvas, b, steps);
      viz.update(cur);
    }

    function render() {
      entries.innerHTML = "";
      steps.slice(0, cur + 1).forEach((s, k) => {
        const e = el("div", "jentry" + (k === cur ? " is-now" : ""));
        e.appendChild(el("span", "jentry__t", esc(s.tag)));
        e.appendChild(el("b", null, esc(s.title)));
        e.appendChild(el("p", null, esc(s.body)));
        entries.appendChild(e);
      });
      entries.scrollTop = entries.scrollHeight;
      fin.classList.toggle("in", cur === steps.length - 1);
      progFill.style.width = ((cur + 1) / Math.max(1, steps.length) * 100) + "%";
      viz.update(cur);
    }
    function stopPlay() {
      if (playing) { clearInterval(playing); playing = null; }
      btnPlay.innerHTML = "▶&nbsp; Play the journey";
    }
    function step(d) {
      stopPlay();
      cur = Math.max(-1, Math.min(steps.length - 1, cur + d));
      render();
    }
    function play() {
      if (playing) { stopPlay(); return; }
      if (cur >= steps.length - 1) cur = -1;
      btnPlay.innerHTML = "❚❚&nbsp; Pause";
      playing = setInterval(() => {
        cur++;
        render();
        if (cur >= steps.length - 1) stopPlay();
      }, 2000);
    }
    btnBack.onclick = () => step(-1);
    btnStep.onclick = () => step(1);
    btnPlay.onclick = play;
    btnReset.onclick = () => { stopPlay(); cur = -1; render(); };

    window.__journeyKeys = { step, play };
    window.__journeyStop = stopPlay;
    buildViz();
    render();
    return wrap;
  },
};

/* Inline icons for journey architecture blocks (set icon: "<name>" on a
   block). Stroke-style, inherit currentColor — no external files needed. */
const JICONS = {
  portal:    '<rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>',
  approve:   '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>',
  gateway:   '<path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>',
  shield:    '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  stream:    '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>',
  ticket:    '<path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/>',
  eye:       '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>',
  coins:     '<line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>',
  plan:      '<rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/>',
  git:       '<line x1="6" y1="3" x2="6" y2="15"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/>',
  ai:        '<rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/>',
  gear:      '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4.9 4.9l2.2 2.2M16.9 16.9l2.2 2.2M2 12h3M19 12h3M4.9 19.1l2.2-2.2M16.9 7.1l2.2-2.2"/>',
  container: '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>',
  key:       '<circle cx="7.5" cy="15.5" r="4.5"/><path d="M10.7 12.3L21 2M14 6l3 3M18 4l2 2"/>',
};
function jicon(name) {
  if (!JICONS[name]) return null;
  const span = el("span", "jblk__icon");
  span.innerHTML = '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' + JICONS[name] + "</svg>";
  return span;
}

/* ===== journey visualization: embedded video (e.g. exported Figma flow) === */
function videoViz(canvas, v) {
  const holder = el("div", "jvideo");
  if (v && v.src) {
    const stage = el("div", "jvideo__stage");
    const vid = document.createElement("video");
    vid.controls = true; vid.preload = "metadata";
    if (v.poster) vid.poster = v.poster;
    const s = document.createElement("source");
    s.src = v.src; vid.appendChild(s);
    stage.appendChild(vid);
    const goFull = () => {
      if (document.fullscreenElement) return;
      if (vid.requestFullscreen) vid.requestFullscreen();
      else if (vid.webkitRequestFullscreen) vid.webkitRequestFullscreen();
      if (vid.paused) { const p = vid.play(); if (p && p.catch) p.catch(() => {}); }
    };
    vid.addEventListener("click", goFull);
    const fs = el("button", "video-card__fs", "⛶");
    fs.title = "Full screen";
    fs.addEventListener("click", goFull);
    stage.appendChild(fs);
    holder.appendChild(stage);
    if (v.caption) holder.appendChild(el("p", "jvideo__cap", esc(v.caption)));
  } else {
    holder.appendChild(el("div", "jvideo__empty",
      "▶ " + esc((v && v.caption) || "Software stack flow video") +
      "<br><small>Export your Figma flow to a video (.mp4), drop it in assets/video, " +
      "and set its path in content/solution.js — journey block, <code>video.src</code>.</small>"));
  }
  canvas.appendChild(holder);
  return { update() {} };
}

/* ===== journey visualization A: layered architecture ================== */
function archViz(canvas, b, steps) {
  const arch = el("div", "jarch");
  const blockEls = {};
  (b.layers || []).forEach(L => {
    const lay = el("div", "jlayer jlayer--" + (L.color || "maroon"));
    lay.dataset.layer = L.id;
    lay.appendChild(el("div", "jlayer__h", esc(L.label)));
    const row = el("div", "jlayer__blocks");
    (L.blocks || []).forEach(blk => {
      const bl = el("div", "jblk");
      const ic = blk.icon && jicon(blk.icon);
      if (ic) bl.appendChild(ic);
      const txt = el("div", "jblk__txt");
      txt.appendChild(el("b", null, esc(blk.title)));
      if (blk.sub) txt.appendChild(el("span", null, esc(blk.sub)));
      bl.appendChild(txt);
      if (blk.badge) bl.appendChild(el("i", "jblk__badge", esc(blk.badge)));
      row.appendChild(bl);
      blockEls[blk.id] = bl;
    });
    lay.appendChild(row);
    arch.appendChild(lay);
  });
  const orb = el("div", "jorb");
  arch.appendChild(orb);
  canvas.appendChild(arch);

  function moveOrb(target) {
    const a = arch.getBoundingClientRect(), t = target.getBoundingClientRect();
    orb.style.left = (t.left - a.left + t.width / 2) + "px";
    orb.style.top = (t.top - a.top + t.height / 2) + "px";
    orb.classList.add("is-on");
    orb.classList.remove("is-ping");
    void orb.offsetWidth;
    orb.classList.add("is-ping");
  }
  return {
    update(cur) {
      Object.keys(blockEls).forEach(k => blockEls[k].classList.remove("is-active", "is-done"));
      steps.forEach((s, k) => {
        const x = blockEls[s.block];
        if (!x) return;
        if (k < cur) x.classList.add("is-done");
        if (k === cur) x.classList.add("is-active");
      });
      const activeLayer = cur >= 0 ? steps[cur].layer : null;
      arch.querySelectorAll(".jlayer").forEach(l =>
        l.classList.toggle("is-dim", activeLayer !== null && l.dataset.layer !== activeLayer));
      arch.classList.toggle("is-complete", cur === steps.length - 1);
      if (cur >= 0 && blockEls[steps[cur].block]) moveOrb(blockEls[steps[cur].block]);
      else orb.classList.remove("is-on");
    },
  };
}

/* ===== journey visualization B: metro-style flow map ================== */
function flowViz(canvas, steps) {
  const N = steps.length, cols = 4;
  const rows = Math.ceil(N / cols);
  const W = 1000, rowH = 170, H = rows * rowH + 40;
  const xs = [140, 380, 620, 860];
  const pts = steps.map((s, k) => {
    const r = Math.floor(k / cols);
    let c = k % cols;
    if (r % 2) c = cols - 1 - c;
    return { x: xs[c], y: 80 + r * rowH };
  });
  const seg = [0];
  for (let i = 1; i < N; i++) {
    seg.push(seg[i - 1] + Math.hypot(pts[i].x - pts[i - 1].x, pts[i].y - pts[i - 1].y));
  }
  const total = seg[N - 1] || 1;
  const d = pts.map((p, i) => (i ? "L" : "M") + p.x + " " + p.y).join(" ");

  const holder = el("div", "jflow");
  holder.innerHTML =
    '<svg viewBox="0 0 ' + W + " " + H + '" preserveAspectRatio="xMidYMid meet">' +
    '<path class="jflow__track" d="' + d + '"/>' +
    '<path class="jflow__prog" d="' + d + '"/></svg>';
  const progPath = holder.querySelector(".jflow__prog");
  progPath.style.strokeDasharray = "0 " + (total + 10);

  const nodes = steps.map((s, k) => {
    const n = el("div", "jfnode");
    n.style.left = (pts[k].x / W * 100) + "%";
    n.style.top = (pts[k].y / H * 100) + "%";
    n.appendChild(el("span", "jfnode__num", String(k + 1)));
    n.appendChild(el("span", "jfnode__lbl", esc(s.title)));
    holder.appendChild(n);
    return n;
  });
  const dot = el("div", "jorb");
  holder.appendChild(dot);
  canvas.appendChild(holder);

  return {
    update(cur) {
      nodes.forEach((n, k) => {
        n.classList.toggle("is-done", k < cur);
        n.classList.toggle("is-active", k === cur);
      });
      progPath.style.strokeDasharray = (cur >= 0 ? seg[cur] : 0) + " " + (total + 10);
      holder.classList.toggle("is-complete", cur === steps.length - 1);
      if (cur >= 0) {
        dot.classList.add("is-on");
        dot.style.left = (pts[cur].x / W * 100) + "%";
        dot.style.top = (pts[cur].y / H * 100) + "%";
        dot.classList.remove("is-ping");
        void dot.offsetWidth;
        dot.classList.add("is-ping");
      } else dot.classList.remove("is-on");
    },
  };
}

/* ===== journey visualization C: orbital dial ========================== */
function orbitViz(canvas, steps) {
  const N = steps.length;
  const holder = el("div", "jorbit");
  holder.innerHTML =
    '<svg viewBox="0 0 200 200"><circle class="ring" cx="100" cy="100" r="86"/>' +
    '<circle class="ring-hot jorbit__arc" cx="100" cy="100" r="86" transform="rotate(-90 100 100)"/></svg>';
  const arc = holder.querySelector(".jorbit__arc");
  const C = 2 * Math.PI * 86;
  arc.style.strokeDasharray = "0 " + C;

  const center = el("div", "jorbit__center");
  const cTag = el("div", "jorbit__tag");
  const cTitle = el("div", "jorbit__title", "Press ▶ Play");
  const cBody = el("p", "jorbit__body");
  center.appendChild(cTag);
  center.appendChild(cTitle);
  center.appendChild(cBody);
  holder.appendChild(center);

  const nodes = steps.map((s, k) => {
    const ang = (k / N) * Math.PI * 2 - Math.PI / 2;
    const n = el("div", "jonode", String(k + 1));
    n.style.left = (50 + 45 * Math.cos(ang)) + "%";
    n.style.top = (50 + 45 * Math.sin(ang)) + "%";
    n.title = applyParams(s.title);
    holder.appendChild(n);
    return n;
  });
  const dot = el("div", "jorb");
  holder.appendChild(dot);
  canvas.appendChild(holder);

  return {
    update(cur) {
      nodes.forEach((n, k) => {
        n.classList.toggle("is-done", k < cur);
        n.classList.toggle("is-active", k === cur);
      });
      arc.style.strokeDasharray = (C * Math.max(0, cur + 1) / N) + " " + C;
      holder.classList.toggle("is-complete", cur === steps.length - 1);
      if (cur >= 0) {
        const ang = (cur / N) * Math.PI * 2 - Math.PI / 2;
        dot.classList.add("is-on");
        dot.style.left = (50 + 45 * Math.cos(ang)) + "%";
        dot.style.top = (50 + 45 * Math.sin(ang)) + "%";
        cTag.textContent = applyParams(steps[cur].tag);
        cTitle.textContent = applyParams(steps[cur].title);
        cBody.textContent = applyParams(steps[cur].body);
        center.classList.add("is-on");
      } else {
        dot.classList.remove("is-on");
        center.classList.remove("is-on");
        cTag.textContent = "";
        cTitle.textContent = "Press ▶ Play";
        cBody.textContent = "";
      }
    },
  };
}

/* Resolve a click target: `detail` opens a modal, `view` switches view. */
function linkAction(obj) {
  if (obj.detail) return () => openDetail(obj.detail);
  if (obj.view) return () => { location.hash = obj.view; };
  return null;
}
function linkLabel(obj) {
  return obj.detail ? "Expand ⤢" : "Open detail →";
}

/* Header for a band or group. Clicking the header collapses / expands the
   section; a `detail` / `view` link renders as its own chip on the right. */
function bandHead(obj, cls) {
  const h = el("div", cls + (obj.color ? " " + cls + "--" + obj.color : ""));
  h.appendChild(el("span", "band__chev", "▾"));
  h.appendChild(el("span", null, esc(obj.title)));
  const act = linkAction(obj);
  if (act) {
    h.classList.add("is-link");
    const chip = el("span", "band__go", linkLabel(obj));
    chip.addEventListener("click", e => { e.stopPropagation(); act(); });
    h.appendChild(chip);
  }
  return h;
}

/* Wire a header to collapse/expand its container. Set `collapsed: true` on
   a band or group in the content file to start it closed. */
function collapsible(container, head, startCollapsed) {
  head.classList.add("is-toggle");
  head.addEventListener("click", () => container.classList.toggle("is-collapsed"));
  if (startCollapsed) container.classList.add("is-collapsed");
}

/* A single cell inside an architecture band. */
function bandCell(cell) {
  const act = linkAction(cell);
  const c = el("div", "bandcell" + (act ? " is-link" : "")
    + (cell.core ? " bandcell--core" : "")
    /* `lead` brings a cell forward, `muted` pushes it back — for a row where
       one item is in scope for this programme and the others are context. */
    + (cell.lead ? " bandcell--lead" : "")
    + (cell.muted ? " bandcell--muted" : ""));
  if (act) c.addEventListener("click", act);
  const title = el("div", "bandcell__title");
  title.appendChild(el("span", null, esc(cell.title)));
  if (cell.badges && cell.badges.length) {
    const bd = el("span", "bandcell__badges");
    cell.badges.forEach(x => {
      const bdg = el("span", "badge badge--" + String(x).toLowerCase(), esc(x));
      if ((window.__badgeTips || {})[String(x)]) bdg.setAttribute("data-tipkey", String(x));
      bd.appendChild(bdg);
    });
    title.appendChild(bd);
  }
  c.appendChild(title);
  if (cell.sub) c.appendChild(el("div", "bandcell__sub", esc(cell.sub)));
  if (cell.core) c.appendChild(el("div", "bandcell__core", "IBM Sovereign Core"));
  if (cell.ref) c.appendChild(el("div", "bandcell__ref", esc(cell.ref)));
  if (act) c.appendChild(el("div", "bandcell__go", linkLabel(cell)));
  return c;
}

/* ===== journey visualization D: force-directed technology network =====
   D3-style force simulation implemented inline (no external libraries, so
   the site keeps working fully offline). Workflow steps form a backbone
   chain; each step's technologies fly in and connect as it activates.
   Technologies shared by several steps become shared nodes — by the end
   the full technology network is on screen. Nodes are draggable; clicking
   a numbered step node jumps the journey to that step. */
function networkViz(canvas, steps, onSelect) {
  const W = 1000, H = 620;
  const NS = "http://www.w3.org/2000/svg";
  const holder = el("div", "jnet");
  const svg = document.createElementNS(NS, "svg");
  svg.setAttribute("viewBox", "0 0 " + W + " " + H);
  holder.appendChild(svg);
  const gLinks = document.createElementNS(NS, "g");
  const gNodes = document.createElementNS(NS, "g");
  svg.appendChild(gLinks);
  svg.appendChild(gNodes);
  canvas.appendChild(holder);

  /* loose serpentine anchors keep the workflow readable while techs float */
  const cols = 4, xs = [150, 385, 615, 850];
  const anchorFor = k => {
    const r = Math.floor(k / cols);
    let c = k % cols;
    if (r % 2) c = cols - 1 - c;
    return { x: xs[c], y: 115 + r * 195 };
  };

  const nodes = [], links = [], techMap = {};
  steps.forEach((s, k) => {
    const a = anchorFor(k);
    nodes.push({
      id: "s" + k, type: "step", k,
      label: applyParams(s.title).split(" — ")[0],
      x: a.x + (Math.random() * 40 - 20), y: a.y + (Math.random() * 40 - 20),
      vx: 0, vy: 0, ax: a.x, ay: a.y,
    });
  });
  steps.forEach((s, k) => {
    const a = anchorFor(k);
    (s.tech || []).forEach(name => {
      const nm = applyParams(name);
      let t = techMap[nm];
      if (!t) {
        t = {
          id: "t:" + nm, type: "tech", firstK: k, label: nm,
          x: a.x + (Math.random() * 140 - 70), y: a.y + (Math.random() * 140 - 70),
          vx: 0, vy: 0,
        };
        techMap[nm] = t;
        nodes.push(t);
      } else if (k < t.firstK) t.firstK = k;
      links.push({ s: "s" + k, t: t.id, k });
    });
    if (k > 0) links.push({ s: "s" + (k - 1), t: "s" + k, k, backbone: true });
  });
  const byId = {};
  nodes.forEach(n => { byId[n.id] = n; });

  links.forEach(l => {
    const ln = document.createElementNS(NS, "line");
    ln.setAttribute("class", "jnlink" + (l.backbone ? " is-backbone" : ""));
    gLinks.appendChild(ln);
    l.el = ln;
  });
  nodes.forEach(n => {
    const g = document.createElementNS(NS, "g");
    g.setAttribute("class", "jnnode jnnode--" + n.type);
    g.appendChild(document.createElementNS(NS, "circle"));
    if (n.type === "step") {
      const num = document.createElementNS(NS, "text");
      num.setAttribute("class", "jnnum");
      num.setAttribute("dy", "4.5");
      num.textContent = n.k + 1;
      g.appendChild(num);
    }
    const lbl = document.createElementNS(NS, "text");
    lbl.setAttribute("class", "jnlbl");
    lbl.setAttribute("dy", n.type === "step" ? "33" : "23");
    lbl.textContent = n.label;
    g.appendChild(lbl);
    const tip = document.createElementNS(NS, "title");
    tip.textContent = n.label;
    g.appendChild(tip);
    if (n.type === "step" && onSelect) g.addEventListener("click", () => { if (!n._dragged) onSelect(n.k); });
    gNodes.appendChild(g);
    n.el = g;
  });

  /* dragging */
  let drag = null;
  const toLocal = e => {
    const r = svg.getBoundingClientRect();
    return { x: (e.clientX - r.left) / r.width * W, y: (e.clientY - r.top) / r.height * H };
  };
  gNodes.addEventListener("pointerdown", e => {
    const g = e.target.closest(".jnnode");
    if (!g) return;
    const n = nodes.find(x => x.el === g);
    if (!n || !n.visible) return;
    drag = n;
    n.fixed = true;
    n._dragged = false;
    e.preventDefault();
  });
  window.addEventListener("pointermove", e => {
    if (!drag) return;
    const p = toLocal(e);
    drag._dragged = true;
    drag.x = p.x;
    drag.y = p.y;
    drag.vx = 0;
    drag.vy = 0;
  });
  window.addEventListener("pointerup", () => {
    if (drag) { drag.fixed = false; drag = null; }
  });

  /* force simulation: charge repulsion + link springs + step anchors */
  function tick() {
    const vis = nodes.filter(n => n.visible);
    for (let i = 0; i < vis.length; i++) {
      for (let j = i + 1; j < vis.length; j++) {
        const a = vis[i], b = vis[j];
        let dx = b.x - a.x, dy = b.y - a.y;
        let d2 = dx * dx + dy * dy;
        if (d2 < 120) d2 = 120;
        const d = Math.sqrt(d2);
        const q = (a.type === "step" ? 1500 : 650) + (b.type === "step" ? 1500 : 650);
        const f = q / d2;
        dx /= d; dy /= d;
        if (!a.fixed) { a.vx -= dx * f; a.vy -= dy * f; }
        if (!b.fixed) { b.vx += dx * f; b.vy += dy * f; }
      }
    }
    links.forEach(l => {
      if (!l.visible) return;
      const a = byId[l.s], b = byId[l.t];
      let dx = b.x - a.x, dy = b.y - a.y;
      const d = Math.max(1, Math.hypot(dx, dy));
      const rest = l.backbone ? 180 : 76;
      const f = (d - rest) * 0.018;
      dx /= d; dy /= d;
      if (!a.fixed) { a.vx += dx * f; a.vy += dy * f; }
      if (!b.fixed) { b.vx -= dx * f; b.vy -= dy * f; }
    });
    vis.forEach(n => {
      if (!n.fixed) {
        if (n.type === "step") { n.vx += (n.ax - n.x) * 0.03; n.vy += (n.ay - n.y) * 0.03; }
        n.vx *= 0.82; n.vy *= 0.82;
        n.x += n.vx; n.y += n.vy;
      }
      n.x = Math.max(45, Math.min(W - 45, n.x));
      n.y = Math.max(40, Math.min(H - 34, n.y));
      n.el.setAttribute("transform", "translate(" + n.x + " " + n.y + ")");
    });
    links.forEach(l => {
      if (!l.visible) return;
      const a = byId[l.s], b = byId[l.t];
      l.el.setAttribute("x1", a.x); l.el.setAttribute("y1", a.y);
      l.el.setAttribute("x2", b.x); l.el.setAttribute("y2", b.y);
    });
  }
  /* run the simulation only while the canvas is on the page; the view is
     built before it is attached, so wait for the first connection and stop
     for good once it is removed (view switch / page change) */
  let everConnected = false;
  (function loop() {
    if (holder.isConnected) {
      everConnected = true;
      tick();
    } else if (everConnected) {
      return;                       /* detached after use — stop the loop */
    }
    requestAnimationFrame(loop);
  })();

  return {
    update(cur) {
      nodes.forEach(n => {
        n.visible = n.type === "step" ? n.k <= cur : n.firstK <= cur;
        n.el.classList.toggle("is-in", !!n.visible);
        if (n.visible && !n._seen) {          /* fresh nodes get a nudge */
          n._seen = true;
          n.vx += Math.random() * 8 - 4;
          n.vy += Math.random() * 8 - 4;
        }
      });
      if (cur < 0) nodes.forEach(n => { n._seen = false; });
      links.forEach(l => {
        l.visible = l.k <= cur;
        l.el.classList.toggle("is-in", !!l.visible);
        l.el.classList.toggle("is-active", l.k === cur);
      });
      const activeTechs = cur >= 0 ? (steps[cur].tech || []).map(t => applyParams(t)) : [];
      nodes.forEach(n => {
        const on = (n.type === "step" && n.k === cur) ||
                   (n.type === "tech" && activeTechs.indexOf(n.label) !== -1);
        n.el.classList.toggle("is-active", on);
      });
      holder.classList.toggle("is-complete", cur === steps.length - 1);
    },
  };
}

/* A "section" groups an optional heading/lead with one block. */
function renderSection(sec) {
  const frag = document.createDocumentFragment();
  if (sec.section) frag.appendChild(el("h2", "section-title" + (sec.plain ? " plain" : ""), esc(sec.section)));
  if (sec.lead) frag.appendChild(el("p", "section-lead", esc(sec.lead)));
  const fn = BLOCKS[sec.type];
  if (fn) frag.appendChild(fn(sec));
  else frag.appendChild(el("p", "section-lead", "[Unknown block type: " + esc(sec.type) + "]"));
  return frag;
}

/* ---- page render ---------------------------------------------------- */
function renderPage() {
  const activeId = document.body.getAttribute("data-page");
  document.body.prepend(buildNav(activeId));

  const c = window.CONTENT || {};
  const main = el("main", "page");

  if (c.cover) {
    const cover = el("div", "cover");
    if (c.eyebrow) cover.appendChild(el("p", "cover__eyebrow", esc(c.eyebrow)));
    cover.appendChild(el("h1", "cover__title", accentize(c.title || "")));
    if (c.subtitle) cover.appendChild(el("p", "cover__subtitle", esc(c.subtitle)));
    main.appendChild(cover);
  } else {
    const hero = el("div", "hero");
    hero.appendChild(el("h1", "hero__title", accentize(c.title || "")));
    if (c.subtitle) hero.appendChild(el("p", "hero__subtitle", esc(c.subtitle)));
    main.appendChild(hero);
  }

  (c.sections || []).forEach(sec => main.appendChild(renderSection(sec)));

  if (c.stages && c.stages.length) renderStages(c.stages, main);
  if (c.views && c.views.length) renderViews(c.views, main);

  document.body.appendChild(main);

  const foot = el("footer", "site-footer",
    esc(c.footer || "Confidential — prepared for client presentation."));
  document.body.appendChild(foot);
}

/* ---- staged experience (e.g. Highlights) ----------------------------
   CONTENT.stages: [{ n, label, lede, sections }]. Renders a stepper and
   one stage at a time, with entry animations. ← / → move stages, Space
   replays the current stage's animation. */
function renderStages(stages, main) {
  const stepper = el("div", "stepper");
  const box = el("div", "stagebox");
  const stageEls = [];
  let cur = 0, timers = [];
  const clearT = () => { timers.forEach(clearTimeout); timers = []; };
  const T = (fn, ms) => timers.push(setTimeout(fn, ms));

  stages.forEach((s, i) => {
    const pill = el("div", "step-pill");
    pill.appendChild(el("span", "step-pill__n", esc(s.n || String(i + 1).padStart(2, "0"))));
    pill.appendChild(el("span", "step-pill__t", esc(s.label)));
    pill.addEventListener("click", () => go(i));
    stepper.appendChild(pill);
    if (i < stages.length - 1) stepper.appendChild(el("span", "step-arrow", "›"));

    const stg = el("div", "stg");
    (s.sections || []).forEach(sec => stg.appendChild(renderSection(sec)));
    box.appendChild(stg);
    stageEls.push({ pill, stg });
  });

  function animateStage(stg) {
    clearT();
    /* auto-stagger plain card grids too */
    stg.querySelectorAll(".grid > .card, .grid > .deck-card").forEach(c => c.classList.add("rv"));
    const rvs = Array.prototype.slice.call(stg.querySelectorAll(".rv"));
    rvs.forEach(r => r.classList.remove("in"));
    rvs.forEach((r, k) => T(() => r.classList.add("in"), 150 + 130 * k));

    const vis = stg.querySelector('[data-anim="vision"]');
    if (vis) {
      const cons = Array.prototype.slice.call(vis.querySelectorAll(".consumer"));
      const drop = vis.querySelector(".dropzone"), plate = vis.querySelector(".platebox");
      cons.forEach(c => c.classList.remove("in"));
      drop.classList.remove("go");
      plate.classList.remove("in");
      plate.querySelectorAll(".front,.railbar").forEach(f => f.classList.remove("in"));
      cons.forEach((c, k) => T(() => c.classList.add("in"), 300 + 150 * k));
      T(() => drop.classList.add("go"), 1000);
      T(() => plate.classList.add("in"), 1250);
      Array.prototype.forEach.call(plate.querySelectorAll(".front"),
        (f, k) => T(() => f.classList.add("in"), 1500 + 110 * k));
      Array.prototype.forEach.call(plate.querySelectorAll(".railbar"),
        (f, k) => T(() => f.classList.add("in"), 2150 + 200 * k));
      plate.querySelectorAll(".estate").forEach(f => f.classList.remove("in"));
      Array.prototype.forEach.call(plate.querySelectorAll(".estate"),
        (f, k) => T(() => f.classList.add("in"), 2700 + 120 * k));
    }

    const sd = stg.querySelector('[data-anim="sdlc"]');
    if (sd) {
      const ring = sd.querySelector(".ring-hot"), dot = sd.querySelector(".agent");
      const phs = Array.prototype.slice.call(sd.querySelectorAll(".ph"));
      const caps = Array.prototype.slice.call(sd.querySelectorAll(".cap"));
      const C = 2 * Math.PI * 86;
      ring.style.strokeDasharray = "0 " + C;
      caps.forEach(c => c.classList.remove("is-lit"));
      phs.forEach(p => p.classList.remove("is-hot"));
      phs.forEach((p, k) => T(() => {
        phs.forEach(x => x.classList.remove("is-hot"));
        p.classList.add("is-hot");
        ring.style.strokeDasharray = (C * (k + 1) / phs.length) + " " + C;
        const ang = (k / phs.length) * Math.PI * 2 - Math.PI / 2;
        dot.style.left = (50 + 43 * Math.cos(ang)) + "%";
        dot.style.top = (50 + 43 * Math.sin(ang)) + "%";
      }, 400 + 900 * k));
      caps.forEach((c, k) => T(() => c.classList.add("is-lit"), 1200 + 1150 * k));
    }

    const ac = stg.querySelector('[data-anim="accel"]');
    if (ac) {
      const cards = Array.prototype.slice.call(ac.querySelectorAll(".acc"));
      const close = ac.querySelector(".accel-close");
      cards.forEach(c => c.classList.remove("in"));
      if (close) close.classList.remove("in");
      cards.forEach((c, k) => T(() => c.classList.add("in"), 250 + 140 * k));
      if (close) T(() => close.classList.add("in"), 250 + 140 * cards.length + 300);
    }
  }

  function go(i) {
    cur = Math.max(0, Math.min(stages.length - 1, i));
    stageEls.forEach((se, k) => {
      se.pill.classList.toggle("is-on", k === cur);
      se.pill.classList.toggle("is-done", k < cur);
      se.stg.classList.toggle("is-on", k === cur);
    });
    const sub = document.querySelector(".hero__subtitle");
    if (sub && stages[cur].lede) sub.textContent = applyParams(stages[cur].lede);
    animateStage(stageEls[cur].stg);
  }

  document.addEventListener("keydown", e => {
    if (e.key === "ArrowRight") go(cur + 1);
    if (e.key === "ArrowLeft") go(cur - 1);
    if (e.key === " " && e.target === document.body) {
      e.preventDefault();
      animateStage(stageEls[cur].stg);
    }
  });

  main.appendChild(stepper);
  main.appendChild(box);
  main.appendChild(el("p", "keyhint", "← / → move between stages · Space replays the animation."));
  go(0);
}

/* ---- multi-view pages (e.g. Solution) ------------------------------- */
function renderViews(views, main) {
  const subnav = el("div", "subnav");
  const holder = el("div", "view-holder");

  views.forEach(v => {
    const btn = el("button", "subnav__btn", esc(v.label));
    btn.dataset.view = v.id;
    btn.addEventListener("click", () => { location.hash = v.id; });
    subnav.appendChild(btn);
  });

  function show(id, doScroll) {
    const v = views.find(x => x.id === id) || views[0];
    Array.prototype.forEach.call(subnav.children,
      a => a.classList.toggle("is-active", a.dataset.view === v.id));
    /* stop any running journey before its DOM is torn down */
    if (window.__journeyStop) { window.__journeyStop(); }
    window.__journeyKeys = null;
    window.__journeyStop = null;
    holder.innerHTML = "";
    if (v.title) holder.appendChild(el("h2", "view-title", accentize(v.title)));
    if (v.lead) holder.appendChild(el("p", "section-lead", esc(v.lead)));
    /* A placement section flagged `revealsRest` acts as a gate: the sections
       after it are tucked into a hidden box that animates open when the
       sovereign control plane is clicked. */
    let revealBox = null;
    (v.sections || []).forEach(sec => {
      if (revealBox) { revealBox.appendChild(renderSection(sec)); return; }
      holder.appendChild(renderSection(sec));
      if (sec.type === "placement" && sec.revealsRest) {
        revealBox = el("div", "plc-reveal");
        holder.appendChild(revealBox);
        const cp = holder.querySelector(".plc__cp--btn");
        if (cp) cp.addEventListener("click", () => {
          const open = revealBox.classList.toggle("is-open");
          cp.classList.toggle("is-armed", open);
        });
      }
    });
    /* stagger-reveal accelerator cards when used inside a view */
    Array.prototype.forEach.call(holder.querySelectorAll(".acc, .accel-close"),
      (a, k) => setTimeout(() => a.classList.add("in"), 180 + 130 * k));
    if (doScroll) subnav.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  main.appendChild(subnav);
  main.appendChild(holder);
  window.addEventListener("hashchange", () => show(location.hash.slice(1), true));
  show(location.hash.slice(1) || views[0].id, false);
}

/* ---- pop-up detail views (modals) ----------------------------------- */
function buildModal() {
  const overlay = el("div", "modal-overlay");
  overlay.id = "modal-overlay";
  const panel = el("div", "modal");
  const bar = el("div", "modal__bar");
  const close = el("button", "modal__close", "✕");
  close.setAttribute("aria-label", "Close");
  close.addEventListener("click", closeDetail);
  bar.appendChild(close);
  const body = el("div", "modal__body");
  panel.appendChild(bar);
  panel.appendChild(body);
  overlay.appendChild(panel);
  overlay.addEventListener("click", e => { if (e.target === overlay) closeDetail(); });
  document.body.appendChild(overlay);
  return overlay;
}
function openDetail(id) {
  const d = ((window.CONTENT || {}).details || {})[id];
  if (!d) return;
  const overlay = document.getElementById("modal-overlay") || buildModal();
  const body = overlay.querySelector(".modal__body");
  body.innerHTML = "";
  if (d.title) body.appendChild(el("h2", "view-title", accentize(d.title)));
  if (d.lead) body.appendChild(el("p", "section-lead", esc(d.lead)));
  (d.sections || []).forEach(sec => body.appendChild(renderSection(sec)));
  body.scrollTop = 0;
  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}
function closeDetail() {
  const o = document.getElementById("modal-overlay");
  if (o) { o.classList.remove("is-open"); document.body.style.overflow = ""; }
}
/* Pop-up listing a pod's members (Team page). */
function openPodModal(p) {
  const overlay = document.getElementById("modal-overlay") || buildModal();
  const body = overlay.querySelector(".modal__body");
  body.innerHTML = "";
  body.appendChild(el("h2", "view-title", accentize(p.title)));
  if (p.sub) body.appendChild(el("p", "section-lead", esc(p.sub)));
  const list = el("div", "podlist");
  (p.members || []).forEach(m => {
    const row = el("div", "podlist__row");
    const av = el("div", "podlist__avatar");
    const initial = (applyParams(m.name) || "").replace(/[^A-Za-z0-9]/g, "").charAt(0);
    av.textContent = initial ? initial.toUpperCase() : "•";
    row.appendChild(av);
    const t = el("div");
    t.appendChild(el("div", "podlist__name", esc(m.name)));
    if (m.role) t.appendChild(el("div", "podlist__role", esc(m.role)));
    row.appendChild(t);
    list.appendChild(row);
  });
  if (!(p.members || []).length) list.appendChild(el("p", "section-lead", "[Add members to this pod in the content file.]"));
  body.appendChild(list);
  body.scrollTop = 0;
  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}
document.addEventListener("keydown", e => { if (e.key === "Escape") closeDetail(); });

/* ---- rich hover card for reuse-level badges ([data-tipkey]) ---------- */
let tipBox = null;
document.addEventListener("mouseover", e => {
  const t = e.target.closest ? e.target.closest("[data-tipkey]") : null;
  if (!t) { if (tipBox) tipBox.classList.remove("is-on"); return; }
  const key = t.getAttribute("data-tipkey");
  const info = (window.__badgeTips || {})[key];
  if (!info) return;
  if (!tipBox) { tipBox = el("div", "tipbox"); document.body.appendChild(tipBox); }
  tipBox.innerHTML =
    '<div class="tipbox__head"><span class="badge badge--' + key.toLowerCase() + '">' +
    esc(key) + "</span><b>" + esc(info.label) + "</b></div>" +
    '<p class="tipbox__body">' + esc(info.tip) + "</p>";
  const r = t.getBoundingClientRect();
  tipBox.style.left = Math.max(10, Math.min(window.innerWidth - 300, r.left + r.width / 2 - 145)) + "px";
  tipBox.classList.add("is-on");
  const h = tipBox.offsetHeight;
  tipBox.style.top = (r.top - h - 12 > 8 ? r.top - h - 12 : r.bottom + 12) + "px";
});
document.addEventListener("mouseout", e => {
  if (tipBox && e.target.closest && e.target.closest("[data-tipkey]")) tipBox.classList.remove("is-on");
});

/* journey keyboard control (active while a journey view is rendered) */
document.addEventListener("keydown", e => {
  const j = window.__journeyKeys;
  if (!j || /INPUT|TEXTAREA|SELECT/.test(e.target.tagName)) return;
  if (e.key === "ArrowRight") j.step(1);
  if (e.key === "ArrowLeft") j.step(-1);
  if (e.key.toLowerCase() === "p") j.play();
});

document.addEventListener("DOMContentLoaded", renderPage);
