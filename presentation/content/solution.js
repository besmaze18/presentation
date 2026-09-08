/* =====================================================================
   SOLUTION  —  an interactive, multi-view section.

   The pill buttons under the page title are the SUB-TABS. Each one is a
   slide; click it, or link straight to it with a hash in the address bar,
   e.g.  solution.html#proposed

     1. The Architecture       the four layers, before any product names
     2. GCP at the Heart       why Google Cloud anchors the engine layer
     3. QORE                   what is built once and reused three times
     4. Proposed Architecture  the same stack with every component named

   In the architecture views, click any coloured layer header to collapse
   or expand that layer — useful for taking one layer at a time in the room.

   To add a slide, copy a whole { id, label, title, lead, sections } block
   below and give it a new `id`. To reorder the slides, move the blocks.
   To rename a sub-tab, change its `label`.

   Placeholder words like {{PLATFORM}}, {{COUNTRY}}, {{LEGACY}}, {{XXXX}}
   and {{YYYY}} are set once in content/config.js.
   ===================================================================== */

window.CONTENT = {
  title: "High Level *Solution*",
  subtitle: "From the four architectural layers and the engine beneath them, through what QORE reuses across all three offerings, to the full component-level architecture.",

  /* -------------------------------------------------- top-level views */
  views: [
    /* ------------------------------------------------------------------ 1
       The architecture in four layers — the shape of the whole solution
       before any component names appear. */
    {
      id: "architecture",
      label: "The Architecture",
      title: "The *Architecture*",
      lead:
        "Our proposed solution is built for {{COUNTRY}}, owned by {{COUNTRY}}, and seamlessly integrates with every {{LEGACY}} 2.0 offering — portable by design. Click a layer header to collapse it.",
      sections: [
        {
          type: "bands",
          bands: [
            {
              title: "Platform OS — unified front door for {{LEGACY}} 2.0 offerings",
              cells: [
                { title: "Cognitive AI", sub: "Capability Hub", ref: "In scope for this programme" },
                { title: "Integrated Command & Control Centre", sub: "Consumes" },
                { title: "Digital Twin", sub: "Consumes" },
              ],
            },
            {
              title: "Layer 1 — three sub-offerings, one consistent experience",
              color: "teal",
              cells: [
                { title: "AI Marketplace", sub: "Publish · discover · license · monetise" },
                { title: "AI Toolkit & Sandbox", sub: "Build · train · fine-tune · experiment safely" },
                { title: "ML Ops as a Service", sub: "Deploy · monitor · drift · retrain" },
              ],
            },
            {
              title: "Layer 2 — QORE, the portable layer",
              color: "dark",
              note:
                "The layer between the infrastructure beneath and the offerings above. It keeps the AI engine swappable, routes sensitive work to sovereign infrastructure, and applies one set of controls everywhere.",
              cells: [
                { title: "QORE", sub: "Open-source SDK, standard Kubernetes", ref: "Owned by {{COUNTRY}}" },
              ],
            },
            {
              title: "Layer 3 — the AI engine",
              color: "blue",
              note:
                "Best-in-class models and compute, used to the full — and replaceable, because nothing above it is wired to it.",
              cells: [
                { title: "Google Cloud", sub: "Gemini, Model Garden, GPU / TPU" },
              ],
            },
            {
              title: "Layer 4 — governance and control",
              color: "slate",
              note:
                "Identity, policy, security and cost management you already own — extended across every cloud, not rebuilt.",
              cells: [
                { title: "Microsoft Azure", sub: "Entra ID, Policy, Defender, Sentinel, Arc" },
              ],
            },
            {
              title: "What that buys {{COUNTRY}}",
              color: "gold",
              cells: [
                { title: "Unified", sub: "One front door" },
                { title: "Portable", sub: "Swappable AI engine" },
                {
                  title: "Owned by {{XXXX}} & {{YYYY}}",
                  sub: "Source, IP and direction transfer in full",
                },
              ],
            },
          ],
        },
      ],
    },

    /* ------------------------------------------------------------------ 2
       Why Google Cloud sits at the bottom of that stack. */
    {
      id: "gcp",
      label: "GCP at the Heart",
      title: "GCP is at the heart of our *solution*",
      lead:
        "Google Cloud anchors the infrastructure layer, and Gemini is our AI provider of choice.",
      sections: [
        {
          type: "columns",
          cols: 3,
          items: [
            { title: "Gemini", body: "Frontier models" },
            { title: "200+", body: "Models in Model Garden" },
            { title: "GPU · TPU", body: "Elastic AI compute" },
          ],
        },
        {
          type: "cards",
          variant: "filled",
          cols: 2,
          items: [
            {
              num: null,
              title: "Gemini + Model Garden",
              body:
                "Frontier reasoning, multimodal and agentic models, alongside 200+ third-party and open-weight models — all reachable through one governed catalogue.",
            },
            {
              num: null,
              title: "Agent Platform Managed AI Services",
              body:
                "Managed training, serving, pipelines and vector search. We consume these rather than hand-build equivalents — Principle 2 in action.",
            },
            {
              num: null,
              title: "GPU & TPU at Scale",
              body:
                "Elastic accelerator capacity for training, fine-tuning and high-volume inference — sized to real AI economics, not fixed capacity.",
            },
            {
              num: null,
              title: "Native RAG & Data Services",
              body:
                "Vector search and object storage services sit next to the models, keeping retrieval fast and grounded.",
            },
          ],
        },
        {
          type: "panel",
          title: "So why add anything above it?",
          subtitle:
            "Because the core should be our strongest AI engine, while the experience and orchestration must stay portable, sovereign and unified. Principle 3 exists precisely so that Principle 2 can be pursued without hesitation: we can adopt Google's proprietary services aggressively, because they sit behind interfaces that keep us free.",
        },
      ],
    },

    /* ------------------------------------------------------------------ 3
       QORE — what is built once and reused across all three offerings. */
    {
      id: "qore",
      label: "QORE",
      title: "QORE — purpose-built across *three offerings*",
      lead:
        "We propose a custom-built solution, designed and developed for {{COUNTRY}}'s specific requirements. We call it QORE — an open-source, extensible platform already proposed for iCCC and Digital Twin, now extended to Cognitive AI as the orchestration and portability layer.",
      sections: [
        {
          section: "What QORE contributes as reusable enablers",
          type: "tags",
          items: [
            "Model Gateway",
            "Agent Runtime & MCP",
            "Workflow Canvas & Engine",
            "RAG & Vector Services",
            "ML Ops Pipelines",
            "Responsible AI & Audit",
          ],
        },
        {
          type: "bands",
          bands: [
            {
              title: "QORE — the common SDK & orchestration layer",
              color: "dark",
              note:
                "Designed from the ground up on open standards — deploys on any Kubernetes — owned by {{COUNTRY}}.",
              cells: [
                { title: "Cognitive AI Toolkit", sub: "Capability built here", ref: "Built once" },
                { title: "Integrated Command & Control Centre", sub: "Consumes" },
                { title: "Digital Twin", sub: "Consumes" },
              ],
            },
          ],
        },
        {
          type: "text",
          body: [
            "We are proposing to build QORE for your offerings. The interfaces between them are a design decision we would need to make and validate during the Discovery & Design phase.",
            "Capability is built once in Cognitive AI and consumed by iCCC and Digital Twin, and Platform OS Discovery is the single entry point to the Marketplace, Toolkit & Sandbox and ML Ops for every user across the programme.",
          ],
        },
      ],
    },

    /* ------------------------------------------------------------------ 4
       The same architecture with every component named. Layer headers
       collapse, so you can open one layer at a time in the room. */
    {
      id: "proposed",
      label: "Proposed Architecture",
      title: "Proposed Solution *Architecture*",
      lead:
        "Open-source platform, Google-powered engine, Azure-governed — behind one portability boundary. Click a layer header to collapse or expand it.",
      sections: [
        {
          type: "bands",
          bands: [
            {
              title: "Front Door — Platform OS Discovery Portal",
              color: "teal",
              cells: [
                {
                  title: "Single entry point",
                  sub: "Marketplace, Toolkit & Sandbox and ML Ops for every user across the programme",
                },
              ],
            },
            {
              title: "Layer 1 Application — QORE Experience",
              cells: [
                { title: "AI Marketplace", sub: "Catalog, publishing, entitlements, subscribe", ref: "React · Next.js" },
                { title: "Notebooks / IDE", sub: "Experiment, prototype, test", ref: "JupyterHub · code-server" },
                { title: "ML Ops Console", sub: "Runs, deployments, drifts, usage", ref: "React · Grafana" },
                { title: "Self-service Portal", sub: "Projects, datasets, training, deploy", ref: "React · Entra OIDC" },
                { title: "Workflow & Agent Canvas", sub: "Visual build for models & agents", ref: "React Flow" },
                { title: "Prompt & App Builder", sub: "Prompt engineering and app assembly", ref: "Custom build" },
                { title: "API Hub — Programmatic Access", sub: "Everything the portal does, callable", ref: "REST, SDK, CLI, MCP Server" },
              ],
            },
            {
              title: "Standard APIs — decoupling platform services from the consumers",
              color: "gold",
              note: "Apigee for C0–C3; Kong for C4.",
              cells: [
                { title: "MLflow API" },
                { title: "KServe OIP", sub: "OpenAI-compatible" },
                { title: "MCP · A2A" },
                { title: "OpenAPI" },
              ],
            },
            {
              title: "Layer 2 Orchestration — QORE Platform Services",
              color: "teal",
              note: "Open-source components on Kubernetes.",
              cells: [
                { title: "Model Gateway", ref: "LiteLLM" },
                { title: "Inference Serving", ref: "KServe · vLLM / Triton" },
                { title: "Agent Builder & Runtime", ref: "LangGraph / ADK" },
                { title: "Workflows & Training", ref: "Kubeflow · Argo · AutoGluon" },
                { title: "Model Registry & Experiment", ref: "MLflow" },
                { title: "RAG & Vector", ref: "LlamaIndex / LangChain" },
                { title: "MCP Server", ref: "FastMCP" },
                { title: "AgentOps & Observability", ref: "Langfuse" },
                { title: "Model Monitoring", ref: "Evidently · NannyML" },
                { title: "Marketplace & Catalogue Services", ref: "Custom build" },
                { title: "Telemetry & FinOps", ref: "OTel Collector · Prometheus · OpenCost · Kueue" },
                { title: "Responsible AI", sub: "Evals & guardrails", ref: "Ragas · NeMo Guardrails" },
                { title: "Policy Enforcement", ref: "OPA / Kyverno" },
                { title: "Supply Chain & GitOps Delivery", ref: "cosign · Trivy · Argo CD" },
                { title: "Explainability & Bias", ref: "SHAP · LIME · Fairlearn · DiCE" },
              ],
            },
            {
              title: "Layer 3 AI Engine & Infrastructure",
              color: "blue",
              note: "Consumed via LiteLLM and KServe only.",
              groups: [
                {
                  title: "Google Cloud — C0–C3 workloads",
                  color: "blue",
                  note: "Portable to Azure, Oracle and on-premises.",
                  cells: [
                    { title: "Gemini · Model Garden", sub: "200+ models" },
                    { title: "Google Agent Platform", sub: "Training, serving" },
                    { title: "GPU / TPU", sub: "Elastic AI compute" },
                    { title: "Storage & Data", sub: "Cloud SQL (PostgreSQL), BigQuery, Vector Search" },
                    { title: "Network & Security", sub: "Cloud LB, DNS, CDN, VPC, Model Armor" },
                    { title: "Kubernetes Runtime", sub: "GKE" },
                  ],
                },
                {
                  title: "Sovereign — DRCC / on-premises / edge HPC — C4 workloads",
                  color: "dark",
                  cells: [
                    { title: "vLLM", sub: "e.g. Gemma / Llama" },
                    { title: "OCIR", sub: "Sovereign Qdrant" },
                    { title: "Kubernetes Runtime" },
                    { title: "GPU" },
                  ],
                },
                {
                  title: "Open artifact formats",
                  color: "gold",
                  note: "The formats that make the portability boundary real.",
                  cells: [
                    { title: "OCI / ModelPack" },
                    { title: "Sigstore · SPDX" },
                    { title: "OpenTelemetry · OTLP" },
                  ],
                },
              ],
            },
            {
              title: "Layer 4 Control Plane — Azure",
              color: "slate",
              note: "Reused from {{LEGACY}} 1.0 where applicable.",
              cells: [
                { title: "Identity & Access", ref: "Entra ID" },
                { title: "Policy", ref: "Azure Policy" },
                { title: "Posture & SIEM", ref: "Defender · Sentinel" },
                { title: "Fleet, Inventory & Policy", ref: "Azure Arc" },
                { title: "Monitoring & ITSM", ref: "Azure Monitor" },
                { title: "FinOps", ref: "GCP Billing Export, chargeback" },
              ],
            },
          ],
        },
        {
          type: "text",
          bullets: [
            "Where multiple technology options are shown, the preferred option will be confirmed with {{XXXX}} & {{YYYY}} during Discovery & Design against jointly agreed evaluation criteria.",
            "The sovereign infrastructure runs the full QORE serving and training components — gateway, LLM, KServe/vLLM, LangGraph and MCP server, MLflow/Postgres registry, OPA, OTel, Argo CD and the Kubeflow/Argo training stack. Commerce and discovery stay central in GCP with metadata-only federation.",
          ],
        },
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
