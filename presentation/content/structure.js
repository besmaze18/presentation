/* =====================================================================
   STRUCTURE  —  program organization (the org chart).

   Edit any box label below. {{XXXX}} / {{YYYY}} come from content/config.js.

   The chart has five regions, all optional:
     governance      the client steering committees across the top
     sme             the SME panel down the left, with its note
     leadership      the program leadership pod
     governancePods  the governance pods, with a description
     delivery        the delivery pods — streamAligned and enabling
   ===================================================================== */
window.CONTENT = {
  title: "Program *organization*",
  subtitle:
    "We believe that building a world class platform like this requires a team that is experienced with modern engineering ways-of-working.",
  sections: [
    {
      type: "orgchart",
      governance: {
        label: "Client Governance",
        boxes: [
          "{{XXXX}} Steering Committee",
          "{{YYYY}} Steering Committee",
          "Program Design Authority",
        ],
      },
      sme: {
        label: "SME Panel",
        boxes: ["Industry SMEs", "Technology SMEs", "Delivery SMEs"],
        note:
          "This panel includes Global and GCC advisors from our Engineering, AI and Data practice. They will provide guidance and support to you and the team.",
      },
      leadership: {
        label: "Program Leadership Pod",
        boxes: ["GCC Engagement Partner", "QA Partner", "Account Relationship Partners"],
      },
      governancePods: {
        label: "Governance Pods",
        desc:
          "Manages program governance, scheduling, and team coordination. Oversees project delivery, risk management, and stakeholder alignment.",
        boxes: ["Program Management Pod", "Design Authority Pod"],
      },
      delivery: {
        label: "Delivery Pods",
        streamAligned: {
          label: "Stream-aligned Pods",
          boxes: ["AI Marketplace", "AI Toolkits & Sandbox", "MLOps as Service"],
        },
        enabling: { label: "Enabling Pods", boxes: ["Security", "QA", "XOps"] },
      },
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
