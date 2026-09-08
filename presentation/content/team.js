/* =====================================================================
   TEAM  —  pod boxes mirroring the delivery structure.
   Click any pod to open its member list in a pop-up.
   Fill in the [Full Name] placeholders (and add/remove members freely).
   Colours: maroon / dark / blue / teal / gold / slate.
   ===================================================================== */
window.CONTENT = {
  title: "Our *Team*",
  subtitle: "The pods that deliver the program — click any pod to meet its members.",
  sections: [
    {
      section: "Leadership & Governance",
      type: "pods",
      cols: 3,
      items: [
        {
          color: "dark", title: "Program Leadership Pod", sub: "Engagement leadership",
          members: [
            { name: "[Full Name]", role: "GCC Engagement Partner" },
            { name: "[Full Name]", role: "GCC Engagement Partner" },
            { name: "[Full Name]", role: "QA Partner" },
            { name: "[Full Name]", role: "Account Relationship Partner" },
          ],
        },
        {
          color: "maroon", title: "Program Management Pod", sub: "Governance · Scheduling · Risk",
          members: [
            { name: "[Full Name]", role: "Program Director" },
            { name: "[Full Name]", role: "Program Manager" },
            { name: "[Full Name]", role: "PMO Lead" },
            { name: "[Full Name]", role: "Risk & Quality Manager" },
          ],
        },
        {
          color: "blue", title: "Design Authority Pod", sub: "Design · Engineering · Standards",
          members: [
            { name: "[Full Name]", role: "Chief Architect" },
            { name: "[Full Name]", role: "Design Authority Lead" },
            { name: "[Full Name]", role: "Technical Standards Lead" },
          ],
        },
      ],
    },
    {
      section: "SME Panel",
      lead: "Global and GCC advisors from our Engineering, AI and Data practice — guidance and support to you and the team. Add each advisor's name and a short description below.",
      type: "deck",
      cols: 3,
      items: [
        { color: "gold", header: "[Full Name]", sub: "Industry SME", body: "[Short description — sector focus / experience.]" },
        { color: "gold", header: "[Full Name]", sub: "Industry SME", body: "[Short description — sector focus / experience.]" },
        { color: "gold", header: "[Full Name]", sub: "Technology SME", body: "[Short description — platform / cloud / architecture.]" },
        { color: "gold", header: "[Full Name]", sub: "Technology SME", body: "[Short description — data / AI / engineering.]" },
        { color: "gold", header: "[Full Name]", sub: "Delivery SME", body: "[Short description — delivery / ways-of-working.]" },
        { color: "gold", header: "[Full Name]", sub: "Delivery SME", body: "[Short description — programme / assurance.]" },
      ],
    },
    {
      section: "Stream-aligned Pods",
      type: "pods",
      cols: 4,
      items: [
        {
          color: "maroon", title: "AI & Data", sub: "Stream-aligned pod",
          members: [
            { name: "[Full Name]", role: "Pod Lead" },
            { name: "[Full Name]", role: "Data Engineer" },
            { name: "[Full Name]", role: "ML Engineer" },
            { name: "[Full Name]", role: "Data Governance Specialist" },
          ],
        },
        {
          color: "blue", title: "Workload Manager", sub: "Stream-aligned pod",
          members: [
            { name: "[Full Name]", role: "Pod Lead" },
            { name: "[Full Name]", role: "Platform Engineer" },
            { name: "[Full Name]", role: "DevOps Engineer" },
            { name: "[Full Name]", role: "SRE" },
          ],
        },
        {
          color: "teal", title: "Control Centre", sub: "Stream-aligned pod",
          members: [
            { name: "[Full Name]", role: "Pod Lead" },
            { name: "[Full Name]", role: "ITSM Engineer" },
            { name: "[Full Name]", role: "Observability Engineer" },
            { name: "[Full Name]", role: "FinOps Analyst" },
          ],
        },
        {
          color: "gold", title: "Platform OS", sub: "Stream-aligned pod",
          members: [
            { name: "[Full Name]", role: "Pod Lead" },
            { name: "[Full Name]", role: "Frontend Engineer" },
            { name: "[Full Name]", role: "Backend Engineer" },
            { name: "[Full Name]", role: "UX Designer" },
          ],
        },
      ],
    },
    {
      section: "Enabling & Foundation Pods",
      type: "pods",
      cols: 4,
      items: [
        {
          color: "dark", title: "Security", sub: "Enabling pod",
          members: [
            { name: "[Full Name]", role: "Security Lead" },
            { name: "[Full Name]", role: "Security / Cyber Engineer" },
          ],
        },
        {
          color: "dark", title: "QA", sub: "Enabling pod",
          members: [
            { name: "[Full Name]", role: "QA Lead" },
            { name: "[Full Name]", role: "Test Automation Engineer" },
          ],
        },
        {
          color: "slate", title: "Cloud & Infra", sub: "Platform Foundation",
          members: [
            { name: "[Full Name]", role: "Cloud & Infra Lead" },
            { name: "[Full Name]", role: "Cloud Engineer" },
            { name: "[Full Name]", role: "Network Engineer" },
          ],
        },
        {
          color: "slate", title: "Integration", sub: "Platform Foundation",
          members: [
            { name: "[Full Name]", role: "Integration Lead" },
            { name: "[Full Name]", role: "API / Middleware Engineer" },
          ],
        },
      ],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
