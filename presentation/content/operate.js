/* =====================================================================
   OPERATE  —  Hypercare and Managed Services approach.
   The support model renders as an animated escalation ladder (tiers).
   Colours per tier: dark / maroon / blue / teal / gold / slate.
   ===================================================================== */
window.CONTENT = {
  title: "Our Hypercare and *Managed Services* Approach",
  subtitle: "Our managed services model covers the Hypercare and Operate phases, offering a single scalable customer channel for {{PLATFORM}} & Platform OS. We are fully flexible, but we believe this structure delivers the highest quality experience for {{YYYY}} and {{XXXX}}.",
  sections: [
    {
      section: "The Support Model",
      lead: "One front door — a Support Ticket via the ServiceNow portal — escalating through four tiers of resolution.",
      type: "tiers",
      items: [
        {
          level: "L0",
          color: "gold",
          title: "Self-Service",
          tag: "Deflect & fulfil automatically",
          body: "Platform OS catalogue & Knowledge Hub assistant — automated fulfilment and deflection before a ticket is ever raised.",
          chips: ["Catalogue automation", "Knowledge Hub assistant"],
        },
        {
          level: "L1",
          color: "maroon",
          title: "Front End Service Desk",
          tag: "First human contact",
          body: "Leverage Level 1 support infrastructure from {{YYYY}}, with our team onboarded in partnership with a selected {{COUNTRY}} provider for bilingual AR/EN coverage — consistent support 24/7 through the ServiceNow portal. Note: NOC/SOC support is out of scope; we integrate with the existing NOC/SOC setup of {{YYYY}} / {{XXXX}}.",
          chips: ["Bilingual AR/EN", "24/7", "{{COUNTRY}} partner"],
        },
        {
          level: "L2",
          color: "blue",
          title: "Technical Investigation & Resolution",
          tag: "Deep diagnosis",
          body: "Deloitte provides Level 2 and Level 3 support services, focused on {{PLATFORM}} and Platform OS — not the underlying infrastructure. In-depth technical investigation and resolution of complex problems.",
          chips: ["16/5 + on-call", "Aggressive SLAs", "Critical-issue support"],
        },
        {
          level: "L3",
          color: "slate",
          title: "Expert Resolution & Vendor Co-ordination",
          tag: "Specialists & OEMs",
          body: "Specialized subject-matter experts and vendor escalation — including Oracle, IBM, Red Hat and other OEMs.",
          chips: ["Oracle", "IBM", "Red Hat", "Other OEMs"],
        },
      ],
    },
    {
      section: "How each level works",
      type: "columns",
      cols: 2,
      items: [
        {
          title: "Level 1",
          body: "Our Level 1 support leverages {{YYYY}}'s existing service desk infrastructure, with support services and team onboarded by Deloitte in partnership with one of our partners in {{COUNTRY}} — providing users consistent support through tickets submitted in the ServiceNow portal, 24/7.",
        },
        {
          title: "Level 2 & 3",
          body: "For Level 2, our team provides in-depth technical investigation and resolution to diagnose and fix complex problems, maintaining 16/5 availability with on-call support for critical issues to meet aggressive SLAs. For Level 3, we engage specialized subject matter experts and vendors — including Oracle, IBM, Red Hat and other OEMs.",
        },
      ],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
