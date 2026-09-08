/* =====================================================================
   STRUCTURE  —  delivery structure / program organization (org chart).
   Edit any box label below; {{XXXX}} / {{YYYY}} come from content/config.js.
   ===================================================================== */
window.CONTENT = {
  title: "Delivery *Structure*",
  subtitle: "How the program is organised — governance, leadership, the SME panel and the delivery pods.",
  sections: [
    {
      type: "orgchart",
      governance: {
        label: "Client Governance",
        boxes: ["{{XXXX}} Steering Committee", "{{YYYY}} Steering Committee", "Program Design Authority"],
      },
      sme: {
        label: "SME Panel",
        boxes: ["Industry SMEs", "Technology SMEs", "Delivery SMEs"],
        note: "This panel includes Global and GCC advisors from our Engineering, AI and Data practice. They will provide guidance and support to you and the team.",
      },
      leadership: {
        label: "Program Leadership Pod",
        boxes: ["GCC Engagement Partner", "GCC Engagement Partner", "QA Partner", "Account Relationship Partners"],
      },
      governancePods: {
        label: "Governance Pods",
        desc: "Manages program governance, scheduling, and team coordination. Oversees project delivery, risk management, and stakeholder alignment.",
        boxes: ["Program Management Pod", "Design Authority Pod"],
      },
      delivery: {
        label: "Delivery Pods",
        streamAligned: {
          label: "Stream-aligned Pods",
          boxes: ["AI & Data", "Workload Manager", "Control Centre", "Platform OS"],
        },
        enabling: { label: "Enabling Pods", boxes: ["Security", "QA"] },
        foundation: { label: "Platform Foundation", boxes: ["Cloud & Infra", "Integration"] },
      },
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
