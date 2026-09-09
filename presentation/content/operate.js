/* =====================================================================
   OPERATE  —  Hypercare and Managed Services approach.
   The support model renders as an animated escalation ladder (tiers).
   Colours per tier: dark / maroon / blue / teal / gold / slate.
   ===================================================================== */
window.CONTENT = {
  title: "Our Hypercare and *Managed Services* Approach",
  subtitle:
    "Our managed services model covers the Hypercare and Operate phases, offering a single scalable customer channel for Cognitive AI. We are fully flexible, but we believe this structure delivers the highest quality experience for {{YYYY}} and {{XXXX}}.",
  sections: [
    {
      section: "The Support Model",
      lead:
        "One front door — a support ticket via the ServiceNow portal — escalating through three tiers of resolution.",
      type: "tiers",
      items: [
        {
          level: "L1",
          color: "maroon",
          title: "Front End Service Desk",
          tag: "First human contact",
          body:
            "Leverage Level 1 support infrastructure from {{YYYY}} and onboard our team in partnership with a selected {{COUNTRY}} provider for bilingual AR/EN L1 coverage. Note: NOC/SOC support is out of scope, and we will integrate with the existing NOC/SOC setup of {{YYYY}} / {{XXXX}}.",
          chips: ["Bilingual AR/EN", "24/7", "{{COUNTRY}} partner"],
        },
        {
          level: "L2",
          color: "blue",
          title: "Technical Investigation & Resolution",
          tag: "9/5 + on-call",
          body:
            "Deloitte provides level 2 and level 3 support services. This service is focused on Cognitive AI support, not support of underlying infrastructure.",
          chips: ["9/5 availability", "On-call for critical", "Aggressive SLAs"],
        },
        {
          level: "L3",
          color: "slate",
          title: "Expert Resolution & Google Co-ordination",
          tag: "Specialists & Google",
          body: "Leverage existing level 3 support with Google.",
          chips: ["Subject-matter experts", "Google support"],
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
          body:
            "Our Level 1 support leverages {{YYYY}}'s existing service desk infrastructure, with support services and team onboarded by Deloitte in partnership with one of our partners in {{COUNTRY}} — providing users with consistent support through tickets submitted in the ServiceNow portal, 24/7.",
        },
        {
          title: "Level 2 & 3",
          body:
            "For Level 2 support, our team provides in-depth technical investigation and resolution to diagnose and fix complex problems. We maintain 9/5 availability with on-call support for critical issues to meet aggressive SLAs and minimise operational impact. For Level 3 support, we engage specialised subject matter experts and Google support.",
        },
      ],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
