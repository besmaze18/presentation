/* =====================================================================
   OUR VISION

   Each slide of the Vision story is a SUB-TAB (the pill buttons that
   appear under the page title). Click one, or link straight to it with
   a hash in the address bar, e.g.  highlights.html#principles

   To add another slide, copy one whole { id, label, title, lead,
   sections } block inside `views` below and give it a new `id`.
   To reorder the slides, move the blocks. To rename a sub-tab, change
   its `label`.

   Placeholder words like {{PLATFORM}}, {{COUNTRY}}, {{LEGACY}} and
   {{XXXX}} are set once in content/config.js and expand everywhere.

   Text style notes:
     - In a `title`, *stars* colour a word in the brand accent.
     - `lead` and body copy are plain text — no markdown.
   ===================================================================== */
window.CONTENT = {
  title: "Our *Vision*",
  subtitle:
    "A custom-built solution, designed and developed for {{COUNTRY}}'s specific requirements.",

  views: [
    /* ============================================================ 01 */
    {
      id: "entry",
      label: "Vision Entry",
      title: "We design, build and transfer a *purpose-built AI platform*",
      lead:
        "We propose a custom-built solution for {{COUNTRY}}, engineered to our QORE reference architecture and assembled from leading open-source AI, ML and agentic frameworks.",

      sections: [
        /* The stack carries the argument, so it is the whole slide. Cognitive
           AI is what this programme delivers; Digital Twin and iCCC are the
           other two hubs the same foundation will serve, so they are drawn
           back rather than removed. The positioning copy sits inside the QORE
           band, where the claim it supports is. */
        {
          type: "bands",
          bands: [
            {
              title: "Platform OS — unified entry for all {{LEGACY}} 2.0 offerings",
              cells: [
                {
                  title: "Cognitive AI",
                  sub: "Capability Hub",
                  ref: "In scope for this programme",
                  lead: true,
                },
                { title: "Digital Twin", sub: "Capability Hub", muted: true },
                { title: "iCCC", sub: "Capability Hub", muted: true },
              ],
            },
            {
              title: "QORE — the portable orchestration layer",
              color: "dark",
              note:
                "Built once. Serves as the flexible product foundation of every {{LEGACY}} 2.0 offering.",
              cells: [
                {
                  title: "A purpose-built Cognitive AI platform",
                  sub:
                    "Our solution is a purpose-built Cognitive AI platform, engineered to our QORE reference architecture — a portable, open-source orchestration layer assembled from leading AI, ML and agentic frameworks.",
                },
                {
                  title: "Open standards, no lock-in",
                  sub:
                    "Built from the ground up for {{COUNTRY}}'s requirements rather than adapted from a commercial product, it runs on open standards and standard Kubernetes, so it is tied to no hyperscaler and no vendor roadmap.",
                },
                {
                  title: "Owned by {{XXXX}}",
                  sub:
                    "Source code and IP vest in {{XXXX}} in full. Component selections are validated with {{XXXX}} during Discovery & Design against jointly agreed criteria.",
                },
              ],
            },
            {
              title: "{{PLATFORM}} — the foundation",
              color: "gold",
              cells: [
                {
                  title: "Infrastructure, services and connectivity",
                  sub:
                    "Supports QORE. QORE is deployed to maximise use of {{LEGACY}} functionality where possible, and builds up from this solid foundation.",
                },
              ],
            },
          ],
        },
      ],
    },

    /* ============================================================ 02 */
    {
      id: "principles",
      label: "Solution Principles",
      title: "Our solution rests on four *principles*",
      lead:
        "Everything that follows — the choice of hyperscaler, the platform layer, the architecture — is a direct consequence of these four commitments. Each answers an explicit Cognitive AI Toolkit business objective.",

      sections: [
        {
          type: "cards",
          variant: "filled",
          cols: 2,
          items: [
            {
              num: null,
              title: "Reuse First, Build Once",
              body:
                "We extend what {{LEGACY}} already owns rather than rebuild it. Azure remains the control plane — Entra ID, Policy, Defender, Sentinel and Arc — and {{LEGACY}} 1.0 shared services are federated, not duplicated.",
              bullets: [
                "Protect existing investment",
                "Reduce delivery risk",
                "Accelerate time-to-value",
              ],
            },
            {
              num: null,
              title: "Best-in-Class AI, Fully Leveraged",
              body:
                "We use the full power of Google's managed AI capabilities. The RFP explicitly invites proprietary managed AI services — we take that invitation, because the AI engine should be the strongest available.",
              bullets: [
                "Frontier models",
                "Elastic GPU / TPU scale",
                "Managed, not assembled",
              ],
            },
            {
              num: null,
              title: "Portable by Design, Sovereign by Default",
              body:
                "Every proprietary service is consumed through an open standard interface, so the AI engine can change without touching the application — and classified workloads follow the same route into sovereign infrastructure.",
              bullets: [
                "No hyperscaler lock-in",
                "One unified interface",
                "Sovereign routing built in",
              ],
            },
            {
              num: null,
              title: "One Ecosystem, Not Three Silos",
              body:
                "QORE is the same open-source SDK we propose for iCCC and Digital Twin, now extended to Cognitive AI as the orchestration and portability layer. Platform OS is the single front door to all of it.",
              bullets: [
                "Platform OS is the entry point",
                "One SDK across three offerings",
                "Enablers built once, reused",
              ],
            },
          ],
        },
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
