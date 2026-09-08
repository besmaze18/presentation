/* =====================================================================
   SOLUTION  —  an interactive, multi-view section.

   Top buttons switch the main narrative:
     1. Architecture     (high-level solution architecture — hover the reuse
                          badges to see what each reusability level means)
     2. The Journey      (animated provisioning walkthrough, ending on the
                          "built at scale" bridge into the demos)

   In the Architecture view, sections marked "Expand ⤢" open a POP-UP with
   the detailed L2 view:
     - Platform OS        -> Platform OS L2
     - Integration Layer  -> Integration + Cluster & Workload Manager L2
     - Data Manager       -> Data Manager L2
     - AI Manager         -> AI Manager L2
     - Workload Manager   -> Integration + Cluster & Workload Manager L2
   Close a pop-up with the ✕ button, the Esc key, or by clicking outside it.

   Legend badges:  N = New build,  D = Existing DRCC,  T = {{LEGACY}} reuse,
   and reuse levels 1 = Software, 2 = Config, 3 = Federate, 4 = Via Platform OS
   (all reuse indicators share one colour — they are levels of {{LEGACY}} reuse).
   ===================================================================== */

var LEGEND = [
  { key: "N", label: "New build", tip: "Newly built for this program." },
  { key: "D", label: "Existing DRCC", tip: "Already running on the existing DRCC estate." },
  { key: "T", label: "{{LEGACY}} reuse", tip: "Reused from the {{LEGACY}} estate." },
  { key: "1", label: "Software", tip: "Reuse level 1 — Software Reuse: keep the {{LEGACY}} software running; same product, extended scope with refactored configuration." },
  { key: "2", label: "Config", tip: "Reuse level 2 — Configuration Reuse: refactor the requirements and configuration and apply them on the new sovereign software product." },
  { key: "3", label: "Federate", tip: "Reuse level 3 — Federation: reach into {{LEGACY}} Azure & GCP — don't move what doesn't need to move." },
  { key: "4", label: "Via Platform OS", tip: "Reuse level 4 — Expose through Platform OS: components that can't or shouldn't be sovereign, surfaced as APIs / services." },
];

window.CONTENT = {
  title: "High Level *Solution*",
  subtitle: "From the sovereign foundation, through a layered reuse strategy, to the full solution architecture — with pop-up drill-downs into each capability layer.",

  /* -------------------------------------------------- top-level views */
  views: [
    {
      id: "provider",
      label: "Platform Provider",
      title: "Being a platform provider isn't just about the *technology*",
      lead: "The relationship model behind the platform — click the Operating Model bar to expand the full operating model Deloitte will invest in.",
      sections: [
        {
          type: "providermodel",
          diagram: {
            consumers: { title: "Service Consumers", boxes: ["Gov", "Devs"] },
            facing: "Customer Facing Services & Platform OS",
            provider: { title: "Platform Provider", layers: ["Operating Model", "{{PLATFORM}}"] },
            external: [
              { title: "External Service Providers", box: "Services" },
              { title: "External Service Providers", box: "Services" },
            ],
          },
          notes: [
            "**Service Consumers** maintain a business relationship with, and utilise the services provided by, the **Platform Provider**.",
            "As the number of involved parties in delivering services to a consumer increases, so does the necessity for collaboration. The focus is no longer solely on the one-way relationship between a service provider and consumer, but rather on a network of interconnected relationships that must collaborate to deliver customer service.",
            "**External service providers** operate with their own commercial interests and drivers, which may conflict with the objectives of the consumer and other service providers.",
            "Deloitte's framework establishes distinct roles and responsibilities, as well as effective governance and control, which are essential in a multi-service-provider environment. Without these, a culture may emerge where issues lack clear ownership, potentially leading to client dissatisfaction.",
            "The role of the **Platform Provider** is critical in the delivery of these services, and the implementation of this framework.",
          ],
          operatingModel: {
            title: "Deloitte will invest in developing the *operating model*",
            rows: [
              {
                panels: [
                  { title: "End Users", w: 3, cells: ["Government Entities", "Government Affiliates", "Education & Research"] },
                  { title: "Enablers", w: 3, cells: ["Data Center Providers", "Hyperscalers and CSPs", "Strategic Partners"] },
                  { title: "Regulators", w: 1.3, cells: ["Regulatory Bodies"] },
                ],
              },
              {
                panels: [
                  { title: "Advisory Services", w: 2, cells: ["Architecture and Design Services", "Ideation and Proof of Concept Service"] },
                  { title: "Build Services", w: 2, cells: ["Development & Integration Services", "Model Acceleration and Optimization"] },
                  { title: "X-As-A-Service", w: 3, cells: ["IaaS, PaaS & SaaS Services", "Model Vault", "Telemetry & Insights"] },
                ],
              },
              {
                tag: "Services",
                panels: [
                  { title: "Plan & Manage", w: 2, cells: ["Demand & Execution Management", "Financial Planning & Resource Mgt.", "Innovation & Talent"] },
                  { title: "Engineering", w: 2, cells: ["Solution Development", "Model Deployment & MLOps", "Infrastructure, Platform & DevOps Engineering"] },
                  { title: "Operate & Optimize", w: 2, cells: ["Model Performance & Risk Management", "Infrastructure Operations", "Service Management & Security"] },
                ],
              },
              {
                tag: "Functions",
                panels: [
                  { title: "Plan & Manage", w: 2, cells: ["Demand & Execution Management", "Financial Planning & Resource Mgt.", "Strategy & governance", "Innovation & Talent"] },
                  { title: "Engineering", w: 2, cells: ["Solution Development", "Model Deployment & MLOps", "Infrastructure, Platform & DevOps Engineering"] },
                  { title: "Operate & Optimize", w: 2, cells: ["Model Performance & Risk Management", "Infrastructure Operations", "Monitoring & System Reliability", "Service Management & Security"] },
                ],
              },
            ],
            footer: ["Governance & Business Operations", "Service Management", "Infrastructure Operations", "Platform & Solution Engineering"],
          },
        },
      ],
    },
    {
      id: "architecture",
      label: "Architecture",
      title: "Solution *Architecture*",
      lead: "The high-level architecture, layer by layer. Click a section header to expand it; “Expand ⤢” opens the detailed view in a pop-up.",
      sections: [
        {
          section: "High-Level Component Placement",
          lead: "One sovereign control plane, deployed on {{XXXX}}'s Oracle DRCC, governing workloads across every cloud estate. Each estate carries the same layered stack.",
          type: "placement",
          revealsRest: true,
          controlPlane: {
            title: "{{PLATFORM}} — DRCC Sovereign Control Plane",
            boxes: ["Platform OS", "Integration", "Control Center", "Orchestration & Workloads", "Security and Governance"],
          },
          layers: ["Security", "Network & Integration", "Landing Zone"],
          workloadLabel: "Workload & Data",
          clouds: [
            { name: "Oracle DRCC", hi: true },
            { name: "Microsoft Azure" },
            { name: "Google Cloud Platform (GCP)" },
          ],
        },
        {
          type: "bands",
          legend: LEGEND,
          bands: [
            {
              title: "Platform OS — unified digital front", detail: "platform-os", collapsed: true,
              cells: [
                { title: "Red Hat Developer Suite (RHDS)", badges: ["N"], sub: "Backstage-based internal developer platform" },
                { title: "Custom Next.js", badges: ["N"], sub: "{{LEGACY}} 2.0 Discovery front end" },
                { title: "Strapi", badges: ["N"], sub: "Headless CMS for content & offerings" },
              ],
            },
            {
              title: "Integration Layer & APIs", detail: "integration-cwm", color: "blue", collapsed: true,
              cells: [
                { title: "API Gateway", badges: ["3", "N"], sub: "IBM Connect" },
                { title: "Service Mesh", badges: ["N"], sub: "OpenShift Service Mesh" },
                { title: "Event Streaming", badges: ["D", "N"], sub: "IBM Streaming / OCI streaming" },
              ],
            },
            {
              title: "Control Centre", color: "teal", collapsed: true,
              cells: [
                { title: "ITSM", badges: ["1", "N"], sub: "ServiceNow / Symphony" },
                { title: "Observability", badges: ["N"], sub: "Dynatrace" },
                { title: "CRM", badges: ["1", "N"], sub: "Dynamics 365 / Custom" },
                { title: "FinOps", badges: ["3", "N"], sub: "IBM Turbonomic" },
              ],
            },
            {
              title: "Orchestration & Manager Layer", color: "dark", collapsed: true,
              groups: [
                {
                  title: "Data Manager", detail: "data-manager",
                  cells: [
                    { title: "Backup / Replication / DR", badges: ["N"], sub: "Argo + ACM + Ansible" },
                    { title: "Data Governance", badges: ["D"], sub: "IDMC (CDGC, CDQ, CDMM, MDM), ArcGIS" },
                    { title: "Streaming", badges: ["3", "D", "N"], sub: "OCI Streaming + Spark/Flink, IDMC CMI + Debezium (CDC)" },
                    { title: "Federation / Virtualization", badges: ["3", "N"], sub: "Starburst / IBM watsonx.data" },
                    { title: "Data Ingestion", badges: ["N"], sub: "IDMC CAI + CDI" },
                    { title: "Lakehouse / ACID Storage", badges: ["3", "D", "N"], sub: "Apache Iceberg + OCI Obj Storage" },
                  ],
                },
                {
                  title: "AI Manager", detail: "ai-manager",
                  cells: [
                    { title: "Model Lifecycle", badges: ["3", "D", "N"], sub: "Kubeflow + KServe + MLflow + Sigstore" },
                    { title: "GPU / Runtime", badges: ["3", "N"], sub: "IBM Turbonomic" },
                    { title: "Inference Runtime", badges: ["3", "D", "N"], sub: "vLLM + KServe" },
                  ],
                },
                {
                  title: "Workload Manager", detail: "integration-cwm",
                  cells: [
                    { title: "Central Cluster Mgmt", badges: ["3", "N"], sub: "Red Hat ACM + OpenShift" },
                    { title: "Workload Scheduling", badges: ["N"], sub: "OpenShift" },
                    { title: "OS & Software Upgrades", badges: ["N"], sub: "Ansible + OpenShift" },
                    { title: "Multi-Cloud Orch", badges: ["3", "N"], sub: "Ansible + Red Hat ACM" },
                    { title: "Performance Optimization", badges: ["N"], sub: "IBM Turbonomic + Dynatrace" },
                    { title: "Infra Provisioning / CI/CD", badges: ["3", "N"], sub: "Terraform + Harbor" },
                  ],
                },
              ],
            },
            {
              title: "Security & Governance", color: "slate", collapsed: true,
              cells: [
                { title: "Identity & Access", badges: ["D", "N"], sub: "Ping Identity, OCI IAM" },
                { title: "Data Security", badges: ["D", "N"], sub: "Palo Alto, Forcepoint DLP, OCI Logging" },
                { title: "Infrastructure Security", badges: ["1", "D", "N"], sub: "Palo Alto, ManageEngine, OpenShift, OCI Native" },
                { title: "Application Security", badges: ["1", "D", "N"], sub: "CycloneDX, Prisma, Checkmarx, Snyk, mabl" },
                { title: "GRC", badges: ["N"], sub: "ServiceNow IRM / GRC" },
              ],
            },
            {
              title: "Adapters & Connectors", collapsed: true,
              cells: [
                { title: "Crossplane providers · Terraform OCI/Azure/GCP · IBM Connect · Service Operators (DB, Cache, MQ)" },
              ],
            },
            {
              title: "Cloud & Infrastructure", color: "gold", collapsed: true,
              cells: [
                { title: "Oracle DRCC", badges: ["N"] },
                { title: "GCP", badges: ["2"] },
                { title: "Azure", badges: ["2"] },
                { title: "Private DC / Edge" },
                { title: "{{LEGACY}} 1.0", badges: ["T"] },
              ],
              groups: [
                {
                  title: "Foundation Services",
                  cells: [
                    { title: "Resource Domains", badges: ["D", "N"], sub: "OCI DRCC compartments + Azure & GCP Native" },
                    { title: "Compute", badges: ["D", "N"], sub: "OCI DRCC compute + OpenShift + GPU, Azure & GCP Native" },
                    { title: "Network", badges: ["N"], sub: "OCI VCN + Palo Alto" },
                    { title: "Storage", badges: ["D", "N"], sub: "OCI Object + File Storage + Azure & GCP Native" },
                    { title: "Baselines & Telemetry", badges: ["N"], sub: "Ansible + OS baselines + Cloud Guard + Cloud Native Monitoring" },
                  ],
                },
              ],
            },
          ],
        },
        {
          type: "text",
          body: ["Disaster Recovery & Backup (FR18-21, T126) — spans all clouds and estates."],
        },
      ],
    },

    /* ---------------------------------------------------------------- 4
       The provisioning & deployment journey — an animated walkthrough of a
       real request travelling through the platform. Press Play, or step
       with ← / →. Edit the steps and blocks below to change the story. */
    {
      id: "journey",
      label: "The Journey",
      title: "The Provisioning *Journey*",
      lead: "Watch a real provisioning request travel through the platform — every block lights up as its tool activates, from self-service request to a live workload inside the sovereign boundary.",
      sections: [
        {
          type: "journey",
          /* Default visualization: "video" (the embedded software-stack flow
             video), "network", "arch", "flow" or "orbit". */
          defaultViz: "video",
          /* VIDEO tab — export your Figma "Software Stack" flow to a video and
             drop it in assets/video, then set the path between the quotes:
                 src: "assets/video/software-stack.mp4"
             Leave src empty to show a placeholder. */
          video: {
            src: "" /* "assets/video/software-stack.mp4" */,
            poster: "" /* optional still image, e.g. "assets/img/stack-poster.jpg" */,
            caption: "The software-stack provisioning flow — from request to a live, sovereign workload.",
          },
          final: "Environment live — monitored, cost-tagged, fully inside the sovereign boundary.",
          /* Each block can carry an icon. Available names: portal, approve,
             gateway, shield, stream, ticket, eye, coins, plan, git, ai,
             gear, container, key. */
          layers: [
            {
              id: "os", label: "Platform OS — unified digital front", color: "maroon",
              blocks: [
                { id: "b-self", icon: "portal", title: "Self-Service Portal", sub: "RHDS / Backstage — catalog & blueprints" },
                { id: "b-approve", icon: "approve", title: "Approval Workflow", sub: "Request → entitlement → approve" },
              ],
            },
            {
              id: "int", label: "Integration Layer & APIs", color: "blue",
              blocks: [
                { id: "b-gw", icon: "gateway", title: "API Gateway", sub: "IBM Connect — identity & routing" },
                { id: "b-policy", icon: "shield", title: "Policy Enforcement", sub: "OPA — residency & guardrails" },
                { id: "b-events", icon: "stream", title: "Event Streaming", sub: "OCI Streaming — provisioning events" },
              ],
            },
            {
              id: "cc", label: "Control Centre", color: "teal",
              blocks: [
                { id: "b-itsm", icon: "ticket", title: "ITSM", sub: "Symphony — change record" },
                { id: "b-obs", icon: "eye", title: "Observability", sub: "Dynatrace — attach monitoring" },
                { id: "b-finops", icon: "coins", title: "FinOps", sub: "IBM Turbonomic — cost tagging" },
              ],
            },
            {
              id: "orch", label: "Orchestration & Manager Layer", color: "plum",
              blocks: [
                { id: "b-wm", icon: "plan", title: "Workload Manager", sub: "Blueprint → deployment plan" },
                { id: "b-gitops", icon: "git", title: "GitOps", sub: "Argo — declarative commit & sync" },
                { id: "b-aim", icon: "ai", title: "AI Manager", sub: "Model registry & guardrails" },
              ],
            },
            {
              id: "core", label: "IBM Sovereign Core — on Oracle DRCC", color: "navy",
              blocks: [
                { id: "b-acm", icon: "gear", title: "ACM + Ansible", sub: "Cluster mgmt & automation" },
                { id: "b-ocp", icon: "container", title: "OpenShift", sub: "Workload runs inside the boundary" },
                { id: "b-evd", icon: "key", title: "Identity · Keys · Evidence", sub: "Audit-grade logging" },
              ],
            },
          ],
          /* `tech` lists the technologies each step activates — shown as
             satellite nodes in the Network view. Use the SAME name for a
             technology used by several steps and it becomes one shared node
             linking those steps together. */
          steps: [
            { block: "b-self", layer: "os", tag: "Step 1 · Request", title: "Self-Service Portal", tech: ["RHDS (Backstage)", "Software Catalog", "Golden-Path Templates"], body: "A ministry user picks the \"GPU AI Environment\" blueprint from the catalog — sovereign-only, dev tier." },
            { block: "b-approve", layer: "os", tag: "Step 2 · Approve", title: "Approval Workflow", tech: ["Backstage RBAC", "Okta + Ping", "ServiceNow"], body: "Entitlement validated; the tenant owner approves in one click. Guardrails already narrowed choices to compliant options." },
            { block: "b-gw", layer: "int", tag: "Step 3 · Route", title: "API Gateway — IBM Connect", tech: ["IBM API Connect", "OpenShift Service Mesh"], body: "The request enters the platform: authenticated, authorized, routed. Identity enforced at the edge." },
            { block: "b-policy", layer: "int", tag: "Step 4 · Enforce", title: "Policy — OPA", tech: ["OPA / Gatekeeper", "OCI IAM"], body: "Residency and security policy evaluated as code. DRCC-only placement confirmed before anything is built." },
            { block: "b-itsm", layer: "cc", tag: "Step 5 · Record", title: "ITSM — Symphony", tech: ["ServiceNow", "Symphony AI"], body: "A change record opens automatically — full traceability, no human ticketing." },
            { block: "b-wm", layer: "orch", tag: "Step 6 · Plan", title: "Workload Manager", tech: ["Red Hat ACM", "KAI Scheduler", "Kueue"], body: "The blueprint becomes a concrete deployment plan: clusters, namespaces, GPU pool, quotas." },
            { block: "b-gitops", layer: "orch", tag: "Step 7 · Commit", title: "GitOps — Argo", tech: ["Argo CD", "GitLab", "Harbor"], body: "The plan is committed declaratively. Argo syncs desired state — every change versioned and auditable." },
            { block: "b-acm", layer: "core", tag: "Step 8 · Automate", title: "ACM + Ansible", tech: ["Ansible", "Red Hat ACM", "Terraform"], body: "Sovereign Core takes over: cluster management and automation execute the deployment on DRCC." },
            { block: "b-ocp", layer: "core", tag: "Step 9 · Run", title: "OpenShift", tech: ["OpenShift", "Oracle DRCC", "OCI Vault"], body: "The workload starts inside the sovereign boundary. It never leaves. Evidence and keys logged natively." },
            { block: "b-events", layer: "int", tag: "Step 10 · Notify", title: "Event Streaming", tech: ["OCI Streaming", "IBM Streaming"], body: "Provisioning events stream back through the platform — the user watches progress live in the portal." },
            { block: "b-obs", layer: "cc", tag: "Step 11 · Observe", title: "Observability — Dynatrace", tech: ["Dynatrace", "OpenTelemetry"], body: "Monitoring attaches automatically. Tenant-scoped dashboards from second one." },
            { block: "b-finops", layer: "cc", tag: "Step 12 · Meter", title: "FinOps — Turbonomic", tech: ["IBM Turbonomic", "KubeTurbo"], body: "Cost tagging and optimization active — transparent pricing back to the consuming entity." },
          ],
        },
        {
          type: "cta",
          href: "demo.html",
          title: "This architecture isn't just coloured blocks — we have *built it at scale*, for ourselves.",
          body: "The Engineering Platform, OpenCloud and AI Core already run in production today — and our vision of the {{PLATFORM}} journey has AI embedded in everything. See them working.",
          label: "Watch the demos →",
        },
      ],
    },
  ],

  /* -------------------------------------------------- pop-up detail views */
  details: {
    "platform-os": {
      title: "Platform OS — *L2*",
      lead: "The detailed Platform OS build. One front door for every persona, on a Red Hat Developer Suite (Backstage) internal developer platform.",
      sections: [
        {
          type: "bands",
          legend: LEGEND,
          bands: [
            {
              title: "Portal Experiences — One Front Door for Every Persona",
              cells: [
                { title: "Self-Service Portal", badges: ["N"], sub: "End-user catalog + provisioning", ref: "FR-EX-02,04,05,14,15 · TR-EX-14-17" },
                { title: "Administrative Portal", badges: ["N"], sub: "Tenant + governance config", ref: "FR-EX-03,07-12,18-20, T41 · TR-EX-38" },
                { title: "{{LEGACY}} 2.0 Discovery", badges: ["2", "N", "4"], sub: "Offerings, Products, CMS (Next.js + Strapi)", ref: "FR-DP-01-32 · TR-DP-01-40" },
                { title: "Developer Portal + Knowledge Hub", badges: ["N"], sub: "API catalogue, sandbox, RAG assistant", ref: "FR-INT-15 · TR-INT-27,51 · TR-DevX-01-10 · TR-KH-01-10" },
              ],
            },
            {
              title: "Red Hat Developer Suite (Backstage) — Central Internal Developer Platform (IDP)",
              note: "Single, opinionated developer interface that unifies catalog, templates, docs, search, plugins, and policy.",
              cells: [
                { title: "Software Catalog", badges: ["N"], sub: "Entities, ownership, dependencies", ref: "FR-EX-04, T156 · TR-EX-14 · TR-INT-24" },
                { title: "Software Templates", badges: ["N"], sub: "Scaffolders, golden paths, blueprints", ref: "FR-EX-04 · TR-EX-15,16 · TR-DevX-01" },
                { title: "TechDocs", badges: ["N"], sub: "Docs-as-code, versioned, in-context", ref: "TR-KH-01,06,09 · TR-DP-23-28" },
                { title: "Search", badges: ["N"], sub: "Federated across catalog + docs + APIs", ref: "FR-DP-07,08 · TR-INT-04" },
                { title: "Plugin Framework", badges: ["N"], sub: "React UI + Node backend plugins; SPI", ref: "TR-EX-40 · TR-INT-29" },
                { title: "Auth + Permissions", badges: ["N"], sub: "OIDC/SAML to Okta + Ping; Backstage RBAC", ref: "FR-EX-01 · TR-EX-03-07 · TR-DP-16-18" },
                { title: "GitOps + Delivery", badges: ["N"], sub: "Argo CD + GitLab plugins; PR/pipeline/artifact", ref: "FR-EX-15 · TR-EX-30,31" },
                { title: "Theming + i18n", badges: ["N"], sub: "AR/EN, RTL, WCAG 2.1 AA, design tokens", ref: "FR-EX-13, FR-DP-03,06, T18 · TR-EX-08-10" },
              ],
            },
            {
              title: "RHDS Backstage Plugins — Surfacing {{PLATFORM}} Capabilities as One Experience",
              cells: [
                { title: "AI Manager Plugin", badges: ["N"], sub: "Models, GPU pools, KAI quotas, RAI gates", ref: "FR-EX-07,20 · TR-EX-22,23" },
                { title: "Data Manager Plugin", badges: ["N"], sub: "Catalog, lineage, DQ, residency, backup", ref: "FR-EX-06 · TR-EX-21 · T152, T156, T160" },
                { title: "Workload Plugin", badges: ["N"], sub: "Scheduling, multi-cloud, drift, inventory", ref: "FR-EX-05 · TR-EX-18-20" },
                { title: "FinOps Plugin", badges: ["N"], sub: "Quotas, budgets, showback, forecasting", ref: "FR-EX-11 · TR-EX-35" },
                { title: "Observability Plugin", badges: ["N"], sub: "Dynatrace + OTel; SLO/SLA, alerts, traces", ref: "FR-EX-10,16 · TR-EX-32,33" },
                { title: "Policy Plugin", badges: ["N"], sub: "OPA / Styra; guardrails, violations, exemptions", ref: "FR-EX-14,17 · TR-EX-26-29,34" },
              ],
            },
            {
              title: "Specialized Capabilities (RHDS Backstage-integrated)",
              cells: [
                { title: "Strapi", badges: ["N"], sub: "CMS for {{LEGACY}} 2.0 content; AR/EN workflows", ref: "FR-DP-20-28 · TR-DP-23-34" },
                { title: "SDK Catalogue + Sandbox", badges: ["N"], sub: "OpenAPI SDKs; container sandboxes; GPU on-demand", ref: "TR-DevX-01,02,04,06" },
                { title: "License Hub", badges: ["N"], sub: "Centralized tool licensing + usage tracking", ref: "TR-DevX-03" },
                { title: "RAG Knowledge Assistant", badges: ["N"], sub: "RAG over TechDocs + APIs", ref: "TR-KH-02,03,07" },
                { title: "Community + Peer Review", badges: ["N"], sub: "Forums, contributions, peer review, moderation", ref: "TR-KH-04,10" },
              ],
            },
          ],
        },
      ],
    },

    "integration-cwm": {
      title: "Integration + *CWM* — L2",
      lead: "DevOps, the DRCC control plane on IBM Sovereign Core (highlighted in gold), tool usage and multi-cloud fleet management.",
      sections: [
        {
          type: "bands",
          legend: LEGEND,
          bands: [
            {
              title: "DRCC DevOps (OpenShift Cluster – IBM Sovereign Core)",
              note: "DevOps Cluster — existing Azure Pipelines continue to function for non-production capabilities; Azure Pipelines trigger promotion to production, directly or via webhooks from GCP or OCI.",
              cells: [
                { title: "Azure Pipelines Agent", sub: "DRCC Pipeline Runner" },
                { title: "Buildah", sub: "OCI Container Builder" },
                { title: "Trivy", sub: "Vulnerability Scanning" },
                { title: "Cosign", sub: "Cryptographic Signing" },
                { title: "Skopeo", sub: "Production Promotion" },
              ],
            },
            {
              title: "DRCC Control Plane",
              note: "API Management — all inbound traffic is routed through IBM API Connect: security & governance enforcement, API exposure and standardization, analytics and observability. Cluster Provisioning — initial cluster provisioning performed utilising Terraform modules and provisioned via Ansible Automation Platform Playbooks (Terraform, IBM Sovereign Core).",
              cells: [
                { title: "OCI Streaming", badges: ["D"] },
                { title: "Harbor", badges: ["N"] },
                { title: "FluxCD", badges: ["N"] },
                { title: "IBM API Connect", badges: ["N"] },
                { title: "Dynatrace", badges: ["1"] },
                { title: "Red Hat Ansible", badges: ["N"], core: true },
                { title: "Red Hat ACM", badges: ["N"], core: true },
                { title: "IBM Turbonomic", badges: ["N"] },
              ],
            },
            {
              title: "Tool Usage",
              cells: [
                { title: "ACM", sub: "Policy enforcement, configuration sync, upgrades" },
                { title: "Ansible", sub: "Workflow, cluster & workload deployment, post-provisioning tasks" },
                { title: "ACM", sub: "Cluster health, metrics, inventory reporting" },
                { title: "Harbor", sub: "Pull containers and image models" },
                { title: "Dynatrace", sub: "Telemetry, logs, traces" },
                { title: "IBM Turbonomic", sub: "Resource usage, GPU metrics, optimization data" },
              ],
            },
            {
              title: "Cloud Components",
              cells: [
                { title: "KubeTurbo", badges: ["N"], sub: "Resource monitoring, workload analysis, optimisation, action execution" },
                { title: "Dynatrace / ActiveGate", badges: ["N"], sub: "Observability" },
                { title: "FluxCD", badges: ["N"], sub: "Config drift, bootstrap" },
                { title: "Service Mesh (Istio)", badges: ["N"], sub: "Traffic management, encryption, resilience, observability", core: true },
                { title: "Advanced Cluster Management", badges: ["N"], sub: "Policy, ALM, config sync, metrics" },
              ],
            },
            {
              title: "Fleets — ACM Logical Fleet Management",
              cells: [
                { title: "{{LEGACY}} (Azure)", badges: ["3"], sub: "ACM Logical Fleet Management" },
                { title: "Public Cloud", badges: ["N"], sub: "ACM Logical Fleet Management" },
                { title: "Sovereign Cloud", badges: ["N"], sub: "ACM Logical Fleet Management" },
              ],
            },
          ],
        },
      ],
    },

    "data-manager": {
      title: "Data Manager — *L2*",
      lead: "Ingestion, an open-table-format lakehouse, one federated query engine across clouds, and full data management.",
      sections: [
        {
          type: "bands",
          legend: LEGEND,
          bands: [
            {
              title: "Data Movement & Ingestion",
              cells: [
                { title: "Data Integration", badges: ["N", "3", "D"], sub: "IDMC CAI + CDI (Informatica)", ref: "FR22, FR24, T163" },
                { title: "Batch Ingestion", badges: ["N", "3", "D"], sub: "IDMC CDI (Informatica)", ref: "FR22, FR23, FR24, T163, T162" },
                { title: "Streaming", badges: ["N", "3", "D"], sub: "OCI Streaming + Spark Structured Streaming / Apache Flink", ref: "FR22, FR23, FR24, T166" },
                { title: "Change Data Capture", badges: ["N", "D"], sub: "IDMC CMI + Debezium (low-latency CDC, optional)", ref: "FR24, T163, T166" },
              ],
            },
            {
              title: "Data Lakehouse — Open Table Format",
              cells: [
                { title: "Storage", badges: ["N", "D", "3"], sub: "Apache Iceberg + OCI Object Storage (primary) + {{LEGACY}} ADLS Federation", ref: "T165" },
                { title: "Table Management", badges: ["N"], sub: "Iceberg Catalog (Open REST) + OCI Streaming (streams land as iceberg tables)", ref: "FR29, T152, T159, T167, T168" },
                { title: "Distributed Processing", badges: ["N"], sub: "Apache Spark", ref: "T170" },
              ],
            },
            {
              title: "Federated Query — One Engine Across DRCC + Azure + GCP",
              note: "Primary recommendation Starburst: open Trino core (no lock-in), proven at enterprise scale, IDMC-catalog-aware, 50+ connectors, Databricks Unity Catalog supported, OPA supported. Alternative: IBM watsonx.data.",
              cells: [
                { title: "Federated Query Engine", badges: ["N", "3"], sub: "Starburst / watsonx.data", ref: "FR22, FR27, FR28, T163, T164, T165, T168, T169" },
              ],
            },
            {
              title: "Data Management — Catalog · Dictionary · MDM · Lineage · DQ · Audit",
              cells: [
                { title: "Catalog", badges: ["N", "D"], sub: "Informatica", ref: "FR26, FR29, T159-T161" },
                { title: "Dictionary", badges: ["N", "D"], sub: "Informatica", ref: "FR26, FR29, T152, T162" },
                { title: "MDM", badges: ["N", "D"], sub: "Informatica", ref: "T161, T152" },
                { title: "Lineage", badges: ["N", "D"], sub: "Informatica", ref: "FR26, T155" },
                { title: "Data Quality", badges: ["N", "D"], sub: "Informatica", ref: "T153, T154" },
                { title: "Audit Logging", badges: ["N", "D"], sub: "Informatica", ref: "FR26, T160" },
                { title: "3D Catalog", badges: ["N", "D"], sub: "ArcGIS", ref: "T158" },
              ],
            },
          ],
        },
      ],
    },

    "ai-manager": {
      title: "AI Manager — *L2*",
      lead: "From AI workload requesters, through governance and key integrity, to GPU runtime, resource management and provisioning.",
      sections: [
        {
          type: "bands",
          legend: LEGEND,
          bands: [
            {
              title: "AI Workload Requesters",
              cells: [
                { title: "Data Scientists", sub: "Notebooks" },
                { title: "ML Engineers", sub: "Training Jobs" },
                { title: "Developers", sub: "Inference APIs" },
                { title: "RAG / Agents", sub: "LLM consumers" },
              ],
            },
            {
              title: "Governance & Policy (FR11, FR13, FR15) — Quotas, RBAC, RAI, Approvals",
              cells: [
                { title: "Identity & RBAC", badges: ["N", "D"], sub: "OCI IAM, Ping Identity" },
                { title: "Policy Engine", badges: ["N"], sub: "OPA + Gatekeeper" },
                { title: "Quota Mgmt", badges: ["N"], sub: "IBM Turbonomic / KAI Scheduler" },
                { title: "RAI Approval", badges: ["N"], sub: "ServiceNow IRM / GRC" },
                { title: "Pre-deploy Bias Check", badges: ["N"], sub: "IBM AIF 360 / Fairlearn" },
              ],
            },
            {
              title: "Model Key Lifecycle & Integrity (FR07–FR11)",
              cells: [
                { title: "Key Mgmt (FR07)", badges: ["N", "D"], sub: "OCI Vault" },
                { title: "Key Lifecycle (FR08)", badges: ["N", "D"], sub: "OCI Vault rotation" },
                { title: "Runtime Binding (FR09)", badges: ["N", "D"], sub: "OCI Vault & envelope encryption" },
                { title: "Artifact Integrity (FR10)", badges: ["N", "D"], sub: "Sigstore (cosign) + Harbor" },
                { title: "Key Policy (FR11)", badges: ["N", "D"], sub: "OPA + OCI Vault IAM policies" },
              ],
            },
            {
              title: "GPU Runtime, Resource Management & Provisioning (FR12–FR17)",
              cells: [
                { title: "GPU Pool (FR12)", badges: ["N"], sub: "KAI Scheduler / HAMi (OSS)" },
                { title: "Quota (FR13)", badges: ["N"], sub: "K8s ResourceQuota / Kueue" },
                { title: "Capacity Monitoring (FR14)", badges: ["3", "N"], sub: "Dynatrace + Prometheus + OpenTelemetry" },
                { title: "Placement (FR16)", badges: ["N"], sub: "KAI Scheduler" },
                { title: "Provisioning (FR17)", badges: ["N"], sub: "Crossplane + Argo Workflows" },
                { title: "Training Pipelines", badges: ["N"], sub: "Kubeflow + Ray" },
                { title: "Inference Serving", badges: ["3", "N"], sub: "KServe + vLLM" },
                { title: "MLOps Tracking (FR14)", badges: ["3", "N"], sub: "MLflow / OpenTelemetry" },
                { title: "Capacity Optimization", badges: ["N"], sub: "IBM Turbonomic / HAMi (OSS)" },
              ],
            },
          ],
        },
      ],
    },
  },

  footer: "Confidential — prepared for {{CLIENT}}.",
};
