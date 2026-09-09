/* =====================================================================
   DEMO  —  proof, not promises.

   HOW TO ADD YOUR VIDEOS:
   1. Put your video files (.mp4 works everywhere) in the  assets/video  folder.
   2. In each slot below, set  src: "assets/video/your-file.mp4".
   3. To swap a video during the demo, just change the src (or reorder the
      slots) and refresh the page. Empty slots show a placeholder.
   ===================================================================== */
window.CONTENT = {
  title: "Proof, not *promises*",
  subtitle:
    "The architecture you just saw isn't just coloured blocks. Three demos: the Cognitive AI journey end to end, Marvin, and AI Core.",
  sections: [
    {
      type: "video",
      items: [
        {
          caption: "The Cognitive AI Journey",
          body:
            "The journey end to end — discover a model or agent in the Marketplace, build and fine-tune it in the Toolkit & Sandbox, then deploy and monitor it through MLOps as a Service, all behind one front door.",
          src: "" /* "assets/video/cognitive-ai-journey.mp4" */,
          poster: "",
        },
        {
          caption: "Marvin",
          body:
            "[One or two sentences on what Marvin is and what this recording shows.]",
          src: "" /* "assets/video/marvin.mp4" */,
          poster: "",
        },
        {
          caption: "AI Core",
          body:
            "Enterprise AI platform with pre-built AI, Data & Vector Catalogs and built-in governance.",
          src: "" /* "assets/video/ai-core.mp4" */,
          poster: "",
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
