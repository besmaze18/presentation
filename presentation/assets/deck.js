/* ---------------------------------------------------------------------------
   The whole runtime of the deck: a small Markdown renderer, the block
   directives (cards, stats, steps, timeline, people, ...), the tab bar and the
   keyboard navigation.

   No build step, no framework, no network. Every page is a plain .html file
   that loads content/site.js, its own content/<slug>.js, and this file. That
   is deliberate: the deck opens by double-clicking a file, on any laptop, with
   no internet in the room.

   You should not need to edit this file to give the presentation. The words
   live in content/.
   --------------------------------------------------------------------------- */
(function () {
  'use strict';

  var SITE = { brand: 'Presentation', tagline: '', footer: '', pages: [] };
  var PAGES = {};

  /* The two functions the content files call. */
  window.site = function (config) { Object.assign(SITE, config || {}); };
  window.page = function (def) { if (def && def.slug) PAGES[def.slug] = def; };

  /* ------------------------------- utilities ------------------------------ */

  function esc(s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  /* A URL is safe to put in href/src only if it is relative or http(s)/mailto.
     Content is written by the presenter, but a stray "javascript:" pasted in
     from somewhere should not become a live link. */
  function safeUrl(url) {
    var u = String(url || '').trim();
    if (/^(https?:|mailto:|tel:|#|\/|\.|data:image\/)/i.test(u)) return esc(u);
    if (/^[\w.-]+(\.\w{2,}|\/)/.test(u)) return esc(u);
    return '#';
  }

  function initials(name) {
    var parts = String(name || '').trim().split(/\s+/).filter(Boolean);
    if (!parts.length) return '?';
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  /* Strips the common leading indentation, so content can be written indented
     inside the template literal and still render as Markdown. */
  function dedent(text) {
    var lines = String(text || '').replace(/\t/g, '  ').split('\n');
    var indent = null;
    lines.forEach(function (line) {
      if (!line.trim()) return;
      var m = line.match(/^ */)[0].length;
      if (indent === null || m < indent) indent = m;
    });
    if (!indent) return lines.join('\n');
    return lines.map(function (l) { return l.slice(indent); }).join('\n');
  }

  /* -------------------------------- inline -------------------------------- */

  var SENTINEL = '\u0001';

  function inline(text) {
    var code = [];
    var out = esc(text);

    /* Code spans are lifted out first so ** and * inside them stay literal. */
    out = out.replace(/`([^`]+)`/g, function (_, c) {
      code.push('<code>' + c + '</code>');
      return SENTINEL + (code.length - 1) + SENTINEL;
    });

    out = out
      .replace(/!\[([^\]]*)\]\(([^)\s]+)\)/g, function (_, alt, src) {
        return '<img src="' + safeUrl(src) + '" alt="' + alt + '">';
      })
      .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, function (_, label, url) {
        var external = /^https?:/i.test(url);
        return '<a href="' + safeUrl(url) + '"' +
          (external ? ' target="_blank" rel="noopener noreferrer"' : '') + '>' + label + '</a>';
      })
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>')
      .replace(/ {2,}$/gm, '<br>');

    return out.replace(new RegExp(SENTINEL + '(\\d+)' + SENTINEL, 'g'), function (_, i) {
      return code[+i];
    });
  }

  /* -------------------------------- blocks -------------------------------- */

  function renderBlocks(src) {
    var lines = dedent(src).split('\n');
    var html = [];
    var i = 0;

    function isBlockStart(line) {
      return !line.trim() || /^(#{1,4}\s|[-*+]\s|\d+\.\s|>\s|\||```|(-{3,}|\*{3,})\s*$)/.test(line);
    }

    while (i < lines.length) {
      var line = lines[i];

      if (!line.trim()) { i++; continue; }

      /* fenced code */
      if (/^```/.test(line)) {
        var buf = [];
        i++;
        while (i < lines.length && !/^```/.test(lines[i])) { buf.push(lines[i]); i++; }
        i++;
        html.push('<pre><code>' + esc(buf.join('\n')) + '</code></pre>');
        continue;
      }

      /* Headings start at h2: the hero already owns the page's only h1, so a
         top-level "#" and a "##" both render as the section heading that gets
         the divider rule above it. */
      var h = line.match(/^(#{1,4})\s+(.*)$/);
      if (h) {
        var level = Math.max(h[1].length, 2);
        html.push('<h' + level + '>' + inline(h[2]) + '</h' + level + '>');
        i++;
        continue;
      }

      /* horizontal rule */
      if (/^(-{3,}|\*{3,})\s*$/.test(line)) { html.push('<hr>'); i++; continue; }

      /* blockquote */
      if (/^>\s?/.test(line)) {
        var quote = [];
        while (i < lines.length && /^>\s?/.test(lines[i])) { quote.push(lines[i].replace(/^>\s?/, '')); i++; }
        html.push('<blockquote>' + renderBlocks(quote.join('\n')) + '</blockquote>');
        continue;
      }

      /* table */
      if (/^\|/.test(line)) {
        var rows = [];
        while (i < lines.length && /^\|/.test(lines[i])) { rows.push(lines[i]); i++; }
        html.push(renderTable(rows));
        continue;
      }

      /* list */
      if (/^([-*+]\s|\d+\.\s)/.test(line)) {
        var ordered = /^\d+\.\s/.test(line);
        var items = [];
        while (i < lines.length) {
          var m = lines[i].match(/^([-*+]|\d+\.)\s+(.*)$/);
          if (!m) break;
          if (/^\d+\.$/.test(m[1]) !== ordered) break;
          var item = [m[2]];
          i++;
          /* continuation lines: plain text before the next marker or blank line */
          while (i < lines.length && lines[i].trim() &&
                 !/^([-*+]\s|\d+\.\s|#{1,4}\s|>\s|\||```|:::)/.test(lines[i])) {
            item.push(lines[i].trim());
            i++;
          }
          items.push(inline(item.join(' ')));
        }
        var tag = ordered ? 'ol' : 'ul';
        html.push('<' + tag + '><li>' + items.join('</li><li>') + '</li></' + tag + '>');
        continue;
      }

      /* paragraph */
      var para = [line];
      i++;
      while (i < lines.length && lines[i].trim() && !isBlockStart(lines[i])) { para.push(lines[i]); i++; }
      var joined = para.join('\n').trim();
      var lone = joined.match(/^!\[([^\]]*)\]\(([^)\s]+)\)$/);
      if (lone) {
        html.push('<figure style="margin:0">' + inline(joined) +
          (lone[1] ? '<figcaption>' + esc(lone[1]) + '</figcaption>' : '') + '</figure>');
      } else {
        html.push('<p>' + inline(joined) + '</p>');
      }
    }

    return html.join('\n');
  }

  function renderTable(rows) {
    var cells = rows.map(function (r) {
      return r.replace(/^\||\|$/g, '').split('|').map(function (c) { return c.trim(); });
    });
    var head = cells[0];
    var body = cells.slice(1).filter(function (r) {
      return !r.every(function (c) { return /^:?-{2,}:?$/.test(c) || !c; });
    });
    var html = '<div class="table-wrap"><table><thead><tr>';
    head.forEach(function (c) { html += '<th>' + inline(c) + '</th>'; });
    html += '</tr></thead><tbody>';
    body.forEach(function (r) {
      html += '<tr>';
      head.forEach(function (_, idx) { html += '<td>' + inline(r[idx] || '') + '</td>'; });
      html += '</tr>';
    });
    return html + '</tbody></table></div>';
  }

  /* ------------------------------ directives ------------------------------ */

  /* Splits a directive body on "### " headings. "### Left | Right" puts the
     first half in meta and the second in title; each block below decides what
     those two mean (a date and a phase, a name and a role, ...). */
  function sections(src) {
    var lines = dedent(src).split('\n');
    var out = [];
    var current = null;
    lines.forEach(function (line) {
      var m = line.match(/^###\s+(.*)$/);
      if (m) {
        var parts = m[1].split('|').map(function (p) { return p.trim(); });
        current = {
          title: parts.length > 1 ? parts.slice(1).join(' | ') : parts[0],
          meta: parts.length > 1 ? parts[0] : '',
          body: []
        };
        out.push(current);
      } else if (current) {
        current.body.push(line);
      }
    });
    return out.map(function (s) {
      return { title: s.title, meta: s.meta, body: s.body.join('\n').trim() };
    });
  }

  var DIRECTIVES = {
    cards: function (body, mod) {
      var cls = 'p-card' + (mod === 'accent' ? ' p-card--accent' : '');
      return '<div class="p-cards">' + sections(body).map(function (s) {
        return '<div class="' + cls + '"><div class="p-card__title">' + inline(s.title) + '</div>' +
          renderBlocks(s.body) + '</div>';
      }).join('') + '</div>';
    },

    stats: function (body) {
      var items = dedent(body).split('\n').filter(function (l) { return l.trim(); }).map(function (l) {
        var p = l.split('|').map(function (x) { return x.trim(); });
        return '<div class="p-stat"><div class="p-stat__value">' + inline(p[0] || '') + '</div>' +
          '<div class="p-stat__label">' + inline(p[1] || '') + '</div>' +
          (p[2] ? '<div class="p-stat__hint">' + inline(p[2]) + '</div>' : '') + '</div>';
      });
      return '<div class="p-stats">' + items.join('') + '</div>';
    },

    steps: function (body) {
      return '<div class="p-steps">' + sections(body).map(function (s, idx) {
        return '<div class="p-step"><div class="p-step__num">' + (idx + 1) + '</div>' +
          '<div class="p-step__body"><div class="p-step__title">' + inline(s.title) + '</div>' +
          renderBlocks(s.body) + '</div></div>';
      }).join('') + '</div>';
    },

    timeline: function (body) {
      return '<div class="p-timeline">' + sections(body).map(function (s) {
        return '<div class="p-phase">' +
          (s.meta ? '<div class="p-phase__meta">' + inline(s.meta) + '</div>' : '') +
          '<div class="p-phase__title">' + inline(s.title) + '</div>' +
          '<div class="p-phase__body">' + renderBlocks(s.body) + '</div></div>';
      }).join('') + '</div>';
    },

    people: function (body) {
      return '<div class="p-people">' + sections(body).map(function (s) {
        /* "### Name | Role" */
        var name = s.meta || s.title;
        var role = s.meta ? s.title : '';
        return '<div class="p-person"><div class="p-person__avatar">' + esc(initials(name)) + '</div>' +
          '<div class="p-person__name">' + inline(name) + '</div>' +
          (role ? '<div class="p-person__role">' + inline(role) + '</div>' : '') +
          '<div class="p-person__body">' + renderBlocks(s.body) + '</div></div>';
      }).join('') + '</div>';
    },

    columns: function (body) {
      return '<div class="p-columns">' + sections(body).map(function (s) {
        return '<div><h3>' + inline(s.title) + '</h3>' + renderBlocks(s.body) + '</div>';
      }).join('') + '</div>';
    },

    note: function (body, mod, label) {
      var cls = 'p-note' + (mod ? ' p-note--' + mod : ' p-note--accent');
      return '<div class="' + cls + '">' +
        (label ? '<div class="p-note__label">' + inline(label) + '</div>' : '') +
        renderBlocks(body) + '</div>';
    },

    quote: function (body) {
      var lines = dedent(body).split('\n');
      var cite = '';
      while (lines.length && !lines[lines.length - 1].trim()) lines.pop();
      var last = lines[lines.length - 1] || '';
      if (/^(—|--)\s/.test(last.trim())) {
        cite = last.trim().replace(/^(—|--)\s*/, '');
        lines.pop();
      }
      return '<div class="p-quote">' + renderBlocks(lines.join('\n')) +
        (cite ? '<span class="p-quote__cite">' + inline(cite) + '</span>' : '') + '</div>';
    },

    chips: function (body, mod) {
      var cls = 'p-chip' + (mod === 'accent' ? ' p-chip--accent' : '');
      var items = dedent(body).split(/[\n,]/).map(function (c) { return c.trim(); }).filter(Boolean);
      return '<div class="p-chips">' + items.map(function (c) {
        return '<span class="' + cls + '">' + inline(c) + '</span>';
      }).join('') + '</div>';
    }
  };

  /* Splits the body into plain-Markdown runs and ::: directive blocks. */
  function render(src) {
    var lines = dedent(src).split('\n');
    var html = [];
    var plain = [];
    var i = 0;

    function flush() {
      if (plain.length) { html.push(renderBlocks(plain.join('\n'))); plain = []; }
    }

    while (i < lines.length) {
      var open = lines[i].match(/^:::\s*([a-z]+)(?:\.([a-z]+))?\s*(.*)$/i);
      if (open && DIRECTIVES[open[1].toLowerCase()]) {
        flush();
        var buf = [];
        var depth = 1;
        i++;
        while (i < lines.length) {
          if (/^:::\s*[a-z]+/i.test(lines[i])) depth++;
          else if (/^:::\s*$/.test(lines[i])) { depth--; if (!depth) break; }
          buf.push(lines[i]);
          i++;
        }
        i++;
        html.push(DIRECTIVES[open[1].toLowerCase()](
          buf.join('\n'), (open[2] || '').toLowerCase(), (open[3] || '').trim()));
        continue;
      }
      plain.push(lines[i]);
      i++;
    }
    flush();
    return html.join('\n');
  }

  /* -------------------------------- chrome -------------------------------- */

  function pageIndex(slug) {
    for (var i = 0; i < SITE.pages.length; i++) if (SITE.pages[i].slug === slug) return i;
    return -1;
  }

  function href(p) { return (p && (p.href || p.slug + '.html')) || '#'; }

  function buildHeader(slug) {
    var idx = pageIndex(slug);
    var tabs = SITE.pages.map(function (p, i) {
      var current = p.slug === slug;
      return '<a class="deck-tab" href="' + esc(href(p)) + '"' + (current ? ' aria-current="page"' : '') + '>' +
        '<span class="deck-tab__num">' + ('0' + (i + 1)).slice(-2) + '</span>' +
        '<span>' + esc(p.label) + '</span></a>';
    }).join('');

    var pct = SITE.pages.length ? ((idx + 1) / SITE.pages.length) * 100 : 0;

    return '<header class="deck-header">' +
      '<div class="deck-header__inner">' +
      '<a class="deck-brand" href="' + esc(href(SITE.pages[0])) + '">' +
      '<span class="deck-brand__mark">' + esc(SITE.mark || initials(SITE.brand)) + '</span>' +
      '<span>' + esc(SITE.brand) + '</span>' +
      (SITE.tagline ? '<span class="deck-brand__sub">' + esc(SITE.tagline) + '</span>' : '') +
      '</a>' +
      '<nav class="deck-tabs" aria-label="Presentation sections">' + tabs + '</nav>' +
      '</div>' +
      '<div class="deck-progress"><div class="deck-progress__bar" style="width:' +
      pct.toFixed(1) + '%"></div></div>' +
      '</header>';
  }

  function buildFooter(slug) {
    var idx = pageIndex(slug);
    var prev = idx > 0 ? SITE.pages[idx - 1] : null;
    var next = idx > -1 && idx < SITE.pages.length - 1 ? SITE.pages[idx + 1] : null;

    var html = '<footer class="deck-footer"><div class="deck-footer__inner">';
    if (prev) {
      html += '<a class="deck-nav-btn" href="' + esc(href(prev)) + '">' +
        '<span class="deck-nav-btn__hint">← Previous</span>' +
        '<span class="deck-nav-btn__label">' + esc(prev.label) + '</span></a>';
    }
    if (next) {
      html += '<a class="deck-nav-btn deck-nav-btn--next" href="' + esc(href(next)) + '">' +
        '<span class="deck-nav-btn__hint">Next →</span>' +
        '<span class="deck-nav-btn__label">' + esc(next.label) + '</span></a>';
    }
    html += '</div>';
    html += '<p class="deck-hint">' +
      (SITE.footer ? esc(SITE.footer) + ' &middot; ' : '') +
      'Use <kbd>←</kbd> <kbd>→</kbd> to move between sections</p>';
    return html + '</footer>';
  }

  function keyboard(slug) {
    document.addEventListener('keydown', function (e) {
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      var t = e.target;
      if (t && /^(INPUT|TEXTAREA|SELECT)$/.test(t.tagName)) return;
      var idx = pageIndex(slug);
      if (idx < 0) return;
      var target = null;
      if (e.key === 'ArrowRight' || e.key === 'PageDown') target = SITE.pages[idx + 1];
      else if (e.key === 'ArrowLeft' || e.key === 'PageUp') target = SITE.pages[idx - 1];
      if (target) { e.preventDefault(); window.location.href = href(target); }
    });
  }

  /* --------------------------------- boot --------------------------------- */

  function boot() {
    var slug = document.body.getAttribute('data-page');
    var def = PAGES[slug];
    var entry = SITE.pages[pageIndex(slug)] || {};

    document.title = ((def && def.title) || entry.label || 'Presentation') +
      (SITE.brand ? ' · ' + SITE.brand : '');

    var hero = '';
    if (def) {
      hero = '<section class="hero' + (def.centred ? ' hero--centred' : '') + '">' +
        (def.eyebrow ? '<div class="hero__eyebrow">' + esc(def.eyebrow) + '</div>' : '') +
        '<h1 class="hero__title">' + inline(def.title || entry.label || '') + '</h1>' +
        (def.lead ? '<p class="hero__lead">' + inline(def.lead) + '</p>' : '') +
        '</section>';
    }

    var body = def && def.body
      ? '<div class="prose">' + render(def.body) + '</div>'
      : '<div class="deck-missing"><p>No content yet for this section.</p>' +
        '<p>Write it in <code>content/' + esc(slug) + '.js</code>.</p></div>';

    document.body.insertAdjacentHTML('afterbegin', buildHeader(slug));

    var main = document.createElement('main');
    main.className = 'deck-main';
    main.innerHTML = hero + body;
    document.body.appendChild(main);

    document.body.insertAdjacentHTML('beforeend', buildFooter(slug));

    var current = document.querySelector('.deck-tab[aria-current="page"]');
    if (current && current.scrollIntoView) {
      current.scrollIntoView({ block: 'nearest', inline: 'center' });
    }

    keyboard(slug);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();
})();
