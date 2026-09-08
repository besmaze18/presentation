/* =====================================================================
   DEMO  —  proof, not promises: four demos of what we already run.

   HOW TO ADD YOUR VIDEOS:
   1. Put your video files (.mp4 works everywhere) in the  assets/video  folder.
   2. In each slot below, set  src: "assets/video/your-file.mp4".
   3. To swap a video during the demo, just change the src (or reorder the
      slots) and refresh the page. Empty slots show a placeholder.
   ===================================================================== */
window.CONTENT = {
  title: "Proof, not *promises*",
  subtitle: "The architecture you just saw isn't just coloured blocks — we have built bits and pieces of it at scale, for ourselves. Four demos: the Engineering Platform, OpenCloud, AI Core — and our vision of the {{PLATFORM}} journey, with AI embedded in everything.",
  sections: [
    {
      type: "video",
      items: [
        {
          caption: "{{PLATFORM}} Journey",
          body: "Our vision of the {{PLATFORM}} journey — AI embedded in everything.",
          src: "" /* "assets/video/platform-journey.mp4" */, poster: "",
        },
        {
          caption: "Engineering Platform & OpenCloud",
          body: "Standardised, AI-enabled engineering stack (GitHub Copilot & Claude Code across the SDLC), running on secure-by-default multi-cloud landing zones across AWS, Azure and GCP.",
          src: "" /* "assets/video/engineering-opencloud.mp4" */, poster: "",
        },
        {
          caption: "AI Core",
          body: "Enterprise AI platform with pre-built AI, Data & Vector Catalogs and built-in governance.",
          src: "" /* "assets/video/ai-core.mp4" */, poster: "",
        },
      ],
    },
    {
      type: "text",
      heading: "Talking points / backup notes",
      body: ["[Add notes here in case the live environment or a recording is unavailable.]"],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
