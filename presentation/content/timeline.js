/* =====================================================================
   TIMELINE  —  phases & releases overview first, then the interactive
   Gantt. Months are numbers (M1 = 1); the axis runs past M12 to 2030
   for the Operate phase. Click a release row (▸) to expand its tasks.
   Bar colours: maroon / blue / teal / gold / dark / slate.
   ===================================================================== */
window.CONTENT = {
  title: "Approach & *Timelines*",
  subtitle: "An accelerated delivery approach that delivers an internal release by month 8 and a full production release by month 12 — then operates through 2030.",
  sections: [
    /* ------------------------------------------------ overview first */
    {
      section: "Structured as five focused phases",
      lead: "We have structured program delivery across five (5) focused phases, each one enabling the next; where possible, activities that can run in parallel have been structured to do so.",
      type: "tags",
      items: [
        "Phase 1 · Discovery & Design",
        "Phase 2 · Release 1 Development, Testing & Deployment",
        "Phase 3 · Release 2 Development, Testing & Deployment",
        "Phase 4 · Hypercare & Knowledge Transfer",
        "Phase 5 · Operate",
      ],
    },
    {
      section: "Establishing the platform baseline before phased delivery",
      type: "supportbar",
      icon: "🚀",
      title: "Enabled by Discovery & Design — M3",
      body: "The Discovery & Design phase establishes the foundation for successful platform delivery by aligning key decisions early and designing the platform around clear scope, architecture and implementation priorities.",
    },
    {
      type: "text",
      heading: "Discovery & Design objectives",
      bullets: [
        "Finalise solution design and target architecture (C1 and C2 elaboration).",
        "Finalise the technology stack for {{PLATFORM}} and Platform OS, including {{LEGACY}} reuse components.",
        "Define Epics for the entire solution and User Stories for the initial development sprints.",
        "Lock R1 and R2 scope in alignment with the needs of ICCC, DT & CAI streams.",
        "Confirm the pilot entity to onboard during early development.",
      ],
    },
    {
      section: "…that shape delivery across two major releases",
      type: "columns",
      cols: 2,
      items: [
        {
          title: "Release 1 (Internal) — M8",
          body: "Release 1 focuses on Foundation Platform deployment on DRCC — including Discovery, Self-Service and Admin portals of Platform OS — to enable developers for {{LEGACY}} offerings, with IAAS self-service components provisioning & deployment. Pilot Entity onboarded at M6.",
        },
        {
          title: "Release 2 (Full Scope) — M12",
          body: "Release 2 focuses on the full deployment of {{PLATFORM}} and Platform OS capabilities for all entities.",
        },
      ],
    },

    /* --------------------------------------------------- the timeline */
    {
      section: "The timeline",
      lead: "Click a release row (▸) to expand its tasks. Filter by release above the chart. Operate runs beyond the axis — through August 2030.",
      type: "gantt",
      months: 14,
      hint: "Click a release row (▸) to expand its tasks.",
      scale: ["M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10", "M11", "M12", "…", "2030"],
      filters: [
        { id: "all", label: "All" },
        { id: "r1", label: "Release 1" },
        { id: "r2", label: "Release 2" },
      ],
      rows: [
        {
          label: "Mobilization & Discover / Design", phase: true, color: "dark",
          start: 0, end: 3, text: "Discover & Design", groups: ["r1", "r2"],
        },
        {
          label: "{{PLATFORM}} Release 1", phase: true, color: "maroon",
          start: 3, end: 8, text: "Build · Test · Deploy", milestone: 8, groups: ["r1"],
          tasks: [
            { label: "Build · Test · Deploy (sprints)", color: "maroon", start: 3, end: 6.5, text: "Sprints", icon: "sprint" },
            { label: "Prod. & Op. Readiness Testing", color: "blue", start: 6.5, end: 8 },
            { label: "Internal Go-Live", milestone: 8, milestoneLabel: "Go-Live" },
          ],
        },
        {
          label: "{{PLATFORM}} Release 2", phase: true, color: "maroon",
          start: 7, end: 12, text: "Build · Test · Deploy", milestone: 12, groups: ["r2"],
          tasks: [
            { label: "Build · Test · Deploy (sprints)", color: "maroon", start: 7, end: 10.5, text: "Sprints", icon: "sprint" },
            { label: "Prod. & Op. Readiness Testing", color: "blue", start: 10.5, end: 12 },
            { label: "Go-Live", milestone: 12, milestoneLabel: "Go-Live" },
          ],
        },
        {
          label: "Platform OS Release 1", phase: true, color: "blue",
          start: 3, end: 8, text: "Build · Test · Deploy", milestone: 8, groups: ["r1"],
          tasks: [
            { label: "Build · Test · Deploy (sprints)", color: "blue", start: 3, end: 6.5, text: "Sprints", icon: "sprint" },
            { label: "Prod. & Op. Readiness Testing", color: "teal", start: 6.5, end: 8 },
            { label: "Internal Go-Live", milestone: 8, milestoneLabel: "Go-Live" },
          ],
        },
        {
          label: "Platform OS Release 2", phase: true, color: "blue",
          start: 7, end: 12, text: "Build · Test · Deploy", milestone: 12, groups: ["r2"],
          tasks: [
            { label: "Build · Test · Deploy (sprints)", color: "blue", start: 7, end: 10.5, text: "Sprints", icon: "sprint" },
            { label: "Prod. & Op. Readiness Testing", color: "teal", start: 10.5, end: 12 },
            { label: "Go-Live", milestone: 12, milestoneLabel: "Go-Live" },
          ],
        },
        { label: "Pilot Users & Entity Engagement", color: "gold", start: 3, end: 8, text: "Pilot", groups: ["r1", "r2"] },
        { label: "Hypercare & Knowledge Transfer", color: "teal", start: 8, end: 12, text: "Hypercare", groups: ["r1", "r2"] },
        { label: "Operate — through 2030", color: "slate", start: 12, end: 14, text: "Operate → Aug 2030", groups: ["r1", "r2"] },
      ],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
