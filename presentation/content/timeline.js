/* =====================================================================
   TIMELINE  —  two sub-tabs, one per source slide:

     Our Approach      the six phases, the two releases, and what
                       Discovery & Design has to settle first
     Project Timeline  the 18-month delivery chart and the 36-month
                       operations chart

   In a `gantt` block, `months` is how many columns the chart has and
   `scale` is the label under each one. A row's `start` and `end` are
   column numbers counting from 0, so start: 3, end: 8 draws a bar from
   the 4th column to the 8th. `milestone` puts a star at that column.

   {{PLATFORM}}, {{COUNTRY}}, {{LEGACY}} come from content/config.js.
   ===================================================================== */
window.CONTENT = {
  title: "Approach & *Timelines*",
  subtitle:
    "Six focused phases shaping two major releases — an internal release at month 8, full production at month 12, then operations for 36 months.",

  views: [
    /* ============================================================== 01 */
    {
      id: "approach",
      label: "Our Approach",
      title: "Our *Approach*",
      lead:
        "The timeline below represents the development of the solution and the go-live of the solution.",

      sections: [
        {
          section: "Structured as six focused phases",
          lead:
            "We have structured program delivery across six focused phases, each one enabling the next. Where possible, activities that can run in parallel across different phases have been structured to do so.",
          type: "tags",
          items: [
            "Phase 1 — Initiation & Planning",
            "Phase 2 — Discovery & Design",
            "Phase 3 — R1: Platform Foundation, Toolkit & MLOps",
            "Phase 4 — R2: Marketplace & Full-scope Go-Live",
            "Phase 5 — Hypercare & Enhancements",
            "Phase 6 — Operate",
          ],
        },

        {
          section: "Establishing the platform baseline before phased delivery",
          type: "supportbar",
          title: "Enabled by Discovery & Design — M3",
          body: "Everything the two releases depend on is settled here first.",
        },

        {
          type: "text",
          body: [
            "The Discovery & Design phase lays the foundation for successful delivery by aligning key decisions upfront and confirming critical milestones, including Solution Architecture approval, the connection point analysis and portability strategy, and pilot entity selection.",
          ],
          bullets: [
            "Align scope, architecture and implementation priorities across the three sub-offerings",
            "Validate key dependencies and {{LEGACY}} integration requirements",
            "Confirm strategic design, hyperscaler and technology decisions",
            "Confirm the portability position through the connection point analysis",
            "Define the delivery roadmap for Release 1 and Release 2",
            "Establish the basis for pilot execution and successful delivery",
          ],
        },
        {
          section: "…that shape delivery across two major releases",
          lead:
            "The Cognitive AI release roadmap is structured across Release 1 and Release 2. The Discovery & Design phase establishes the foundation by confirming scope, architecture, the portability strategy and the pilot entity for early onboarding.",
          type: "columns",
          cols: 2,
          items: [
            {
              title: "Release 1 (Internal) — M8",
              body:
                "Release 1 will establish the internal foundation, toolkit and MLOps, including the GCP landing zone, GKE, Entra ID federation, Azure Arc, the orchestration core with standardised connection points, and the Marketplace core.",
            },
            {
              title: "Release 2 (Full Scope + Marketplace MVP) — M12",
              body:
                "Release 2 delivers the Marketplace MVP and full go-live scope, including catalogue, publishing, validation and certification, search, sandbox testing, metering, agent packaging, self-service workspaces and sovereign routing, alongside the pilot, UAT and security and performance testing.",
            },
          ],
        },

        {
          type: "supportbar",
          title: "Pilot Entity Onboarded — M8",
          body: "The pilot entity comes on board with Release 1, not after it.",
        },

      ],
    },

    /* ============================================================== 02 */
    {
      id: "timeline",
      label: "Project Timeline",
      title: "Project *Timeline*",
      lead:
        "Eighteen months to go-live and hypercare, then thirty-six months of operations.",

      sections: [
        {
          section: "Delivery across 18 months",
          lead:
            "This program is a complex 18-month transformation initiative across five phases and demands disciplined execution and continuous alignment to succeed. Each phase builds on the previous one, meaning excellence in Phase 1 planning, Phase 2 architecture and Phase 3 development directly enables successful testing, release, knowledge transfer and go-live in the later phases.",
          type: "gantt",
          months: 14,
          scale: ["M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8",
                  "M9", "M10", "M11", "M12", "→", "M18"],
          rows: [
            {
              label: "Phase 1 — Initiation & Planning",
              color: "dark", phase: true, start: 0, end: 1, text: "Initiation",
            },
            {
              label: "Phase 2 — Discovery & Design",
              color: "blue", phase: true, start: 1, end: 3,
              text: "Discovery & Design",
            },
            {
              label: "Phase 3 — R1: Platform Foundation, Toolkit & MLOps",
              color: "maroon", phase: true, start: 3, end: 8,
              text: "Build · Test · Deploy", milestone: 8,
            },
            {
              label: "Phase 4 — R2: Marketplace & Full-scope Go-Live",
              color: "teal", phase: true, start: 8, end: 12,
              text: "Build · Test · Go-Live", milestone: 12,
            },
            {
              label: "Phase 5 — Hypercare & Enhancements",
              color: "gold", phase: true, start: 12, end: 14, text: "Hypercare",
            },
          ],
        },

        {
          section: "Operations for 36 months",
          lead:
            "Support & Maintenance extends 36 months beyond go-live to ensure the solution operates reliably, performs optimally, and evolves to meet changing business needs. During this critical period, intensive hypercare support, post-deployment monitoring and performance optimisation address real-world issues, stabilise the system, and build operational confidence across the organisation.",
          type: "gantt",
          months: 8,
          scale: ["M12", "→", "M43", "M44", "M45", "M46", "M47", "M48"],
          rows: [
            {
              label: "Phase 6 — Operate",
              color: "slate", phase: true, start: 0, end: 8,
              text: "Support & Maintenance — 36 months beyond go-live",
            },
          ],
        },
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
