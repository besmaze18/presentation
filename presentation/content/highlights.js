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
    "A custom-built solution, designed and developed for {{COUNTRY}}'s specific requirements — told in three moves.",

  views: [
    /* ============================================================ 01 */
    {
      id: "entry",
      label: "Vision Entry",
      title: "We design, build and transfer a *purpose-built AI platform*",
      lead:
        "We propose a custom-built solution, designed and developed for {{COUNTRY}}'s specific requirements — engineered to our QORE reference architecture and assembled from leading open-source AI, ML and agentic frameworks.",

      sections: [
        {
          section: "What {{COUNTRY}} gets",
          type: "tags",
          items: [
            "Built for {{COUNTRY}}",
            "Pay as you grow",
            "No licence fee",
            "Open source",
            "Secure & scalable",
            "Requirements compliant",
          ],
        },

        {
          type: "text",
          body: [
            "Our solution is a purpose-built Cognitive AI platform, engineered to our QORE reference architecture — a portable, open-source orchestration layer assembled from leading AI, ML and agentic frameworks.",
            "Built from the ground up for {{COUNTRY}}'s requirements rather than adapted from a commercial product, it runs on open standards and standard Kubernetes, so it is tied to no hyperscaler and no vendor roadmap.",
            "Source code and IP vest in {{XXXX}} in full. Component selections are validated with {{XXXX}} during Discovery & Design against jointly agreed criteria.",
          ],
        },

        {
          section: "Built once, reused across every {{LEGACY}} 2.0 offering",
          lead:
            "Platform OS is the unified entry point. QORE is the foundation SDK beneath it, and {{PLATFORM}} is the infrastructure beneath that — one foundation serving three capability hubs.",
          type: "stack",
          provider: {
            title: "Platform OS — unified entry for all {{LEGACY}} 2.0 offerings",
            rows: [
              {
                items: [
                  { title: "Cognitive AI", sub: "Capability Hub — in scope" },
                  { title: "Digital Twin", sub: "Capability Hub" },
                  { title: "iCCC", sub: "Capability Hub" },
                ],
              },
              {
                type: "bar",
                title: "QORE",
                sub:
                  "Foundation SDK that fills the portable orchestration layer — built for Cognitive AI, iCCC and Digital Twin, integrated with {{LEGACY}}, and extended as new capability hubs and products arrive.",
              },
              {
                type: "bar",
                gold: true,
                title: "{{PLATFORM}}",
                sub:
                  "Foundation infrastructure, services and connectivity to support QORE. QORE is deployed to maximise use of {{LEGACY}} functionality where possible and builds up from this solid foundation.",
              },
            ],
          },
        },

        {
          type: "panel",
          title: "Built once.",
          subtitle:
            "QORE serves as the flexible product foundation of every {{LEGACY}} 2.0 offering — so the second and third capability hubs cost a fraction of the first.",
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

    /* ============================================================ 03 */
    {
      id: "architecture",
      label: "The Architecture",
      title: "The *Architecture*",
      lead:
        "Our proposed solution is built for {{COUNTRY}}, owned by {{COUNTRY}}, and seamlessly integrates with every {{LEGACY}} 2.0 offering — portable by design.",

      sections: [
        {
          type: "stack",
          provider: {
            title: "Platform OS — unified front door for {{LEGACY}} 2.0 offerings",
            rows: [
              {
                items: [
                  { title: "Cognitive AI", sub: "Capability Hub" },
                  { title: "Digital Twin", sub: "Capability Hub" },
                  { title: "iCCC", sub: "Capability Hub" },
                ],
              },
              {
                type: "bar",
                title: "QORE — portable orchestration layer",
                sub: "Open-source SDK, standard Kubernetes, no hyperscaler lock-in.",
              },
              {
                type: "bar",
                gold: true,
                title: "{{PLATFORM}} — sovereign foundation",
                sub: "Infrastructure, services and connectivity, maximising reuse of {{LEGACY}}.",
              },
            ],
          },
        },

        {
          section: "Still to add",
          type: "text",
          body: [
            "The source slide for this section was cut off in the material supplied, so only its title, opening statement and the Platform OS band above could be transcribed.",
            "Send the rest of the architecture slide and this sub-tab will be completed. Everything below the Platform OS band — the layer detail, the reuse badges and the component names — is missing.",
          ],
        },
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
