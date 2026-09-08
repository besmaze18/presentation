/* =====================================================================
   OPEN  —  the opening / title slide.
   Fill in the four presenters below (names + roles are placeholders).
   Wrap a word in *stars* to colour it maroon.
   ===================================================================== */
window.CONTENT = {
  cover: true,                       // full-height centred title slide
  eyebrow: "Client Presentation",
  title: "Sovereign Multi-Cloud Orchestration Platform for *{{COUNTRY}}*",
  subtitle: "{{PLATFORM}} and Platform OS — presented to {{CLIENT}}.",
  sections: [
    {
      section: "Presented by",
      /* PHOTOS — IMPORTANT:
         1. Put each photo file inside the  assets/img  folder (square images
            look best, e.g. 400x400).
         2. Type the path BETWEEN THE QUOTES on the photo line, like this:
                photo: "assets/img/partner.jpg"
            Do NOT leave it as  photo: ""  — an empty value shows the initial
            in a circle instead of the picture. (Anything after // or between
            comment marks is ignored.)                                        */
      type: "team",
      items: [
        { name: "[Presenter One]",   role: "[Role / Title]", photo: "" },
        { name: "[Presenter Two]",   role: "[Role / Title]", photo: "" },
        { name: "[Presenter Three]", role: "[Role / Title]", photo: "" },
        { name: "[Presenter Four]",  role: "[Role / Title]", photo: "" },
      ],
    },
  ],
  footer: "Confidential — prepared for {{CLIENT}}.",
};
