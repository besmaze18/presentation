/* =====================================================================
   OUR EXPERIENCE  —  three sub-tabs, one per source slide:

     Case Studies        four engagements, with results
     Google Partnership  the 360 partnership and what it gives this bid
     Worldwide Leaders   analyst recognition

   In the `credentials` block, `body` accepts **double stars** around a
   phrase to bold it — that is where the numbers should go, so the eye
   lands on the result rather than the narrative.

   `image` is optional: put a file in assets/img/ and reference it as
   "assets/img/name.jpg". Left empty, the card shows a placeholder panel.

   Deloitte is the delivery partner name, set once in content/config.js.
   ===================================================================== */
window.CONTENT = {
  title: "We have done this *before*",
  subtitle:
    "A select group of examples are showcased here — we will be happy to share more.",

  views: [
    /* ============================================================== 01 */
    {
      id: "cases",
      label: "Case Studies",
      /* The page heading above already says "We have done this before", so this
         sub-tab heading says something the audience does not already have. */
      title: "Selected *engagements*",
      lead:
        "Four examples across government, sovereign funds, industry and insurance — we will be happy to share more.",

      sections: [
        {
          type: "credentials",
          items: [
            {
              image: "" /* e.g. "assets/img/uae-authority.jpg" */,
              title: "UAE Government Authority",
              subtitle: "Internal Enterprise Agentic AI Platform",
              tags: ["AI Toolkit & Sandbox", "MLOps"],
              body:
                "Deloitte designed and is implementing an **internal enterprise agentic AI platform** for a UAE government entity to enable employees to embed AI into their daily work. Deloitte defined the platform vision, governance model and implementation roadmap, and developed a cloud-integrated solution that **enables employees to access purpose-built AI agents and create custom agents using internal data, approved LLM models and internal templates**. The first version of the platform has reached launch stage, with **400+ signups** and a planned scale-up to **more than 2,000 employees**.",
            },
            {
              image: "" /* e.g. "assets/img/sovereign-fund.jpg" */,
              title: "Sovereign Wealth Fund",
              subtitle: "Enterprise MLOps Enablement",
              tags: ["MLOps"],
              body:
                "Deloitte migrated a client's open-source machine learning models (Python and R) onto an **enterprise data science platform**, introduced stronger **MLOps practices** to replace manual model management, and ran **enablement workshops to build business-user trust and adoption**. Across forecasting, investment and HR analytics use cases, the initiative **improved development and deployment turn-around times** and delivered a **17% reduction in employee churn** and a **23% reduction in forecasting margin of error** for raw materials planning.",
            },
            {
              image: "" /* e.g. "assets/img/smart-township.jpg" */,
              title: "Industrial Manufacturing Company",
              subtitle: "Enterprise AI and data platform for a smart township",
              tags: ["MLOps"],
              body:
                "Supported establishing a Google Cloud-based data and AI foundation to enable Malaysia's first fully integrated smart and sustainable green township. We enabled the development of **smart-city use cases, including smart parking, cashless payments, energy management and AI-driven security**, while implementing scalable cloud-native architecture to accelerate rollout of new digital services and support the company's data-driven transformation.",
            },
            {
              image: "" /* e.g. "assets/img/insurance-group.jpg" */,
              title: "Leading European Insurance Group",
              subtitle: "Agentic AI powered Customer Service Transformation",
              tags: ["AI Toolkit & Sandbox"],
              body:
                "Deloitte and Google helped improve customer service after a post-pandemic surge in inquiries and claims. Using Google Document AI, Gemini and Vertex AI, they **built an intelligent assistant that helps agents answer complex health insurance questions** faster and more accurately. The result was a **30% faster information search**, a **drop in escalations from 27% to 3%**, and a **doubling of NPS within one year**.",
            },
          ],
        },
      ],
    },

    /* ============================================================== 02 */
    {
      id: "partnership",
      label: "Google Partnership",
      title: "Deloitte & Google *Partnership*",
      lead: "",

      sections: [
        {
          type: "lockup",
          left: "Deloitte.",
          logo: "assets/img/google-g.svg",
          logoAlt: "Google",
          right: "Google",
          title: "360° Partnership",
          tagline: "We go farther, faster, together",
        },

        {
          type: "statrow",
          items: [
            { value: "1,000 +", label: "Engagements over the past 5+ years" },
            { value: "44K +", label: "Global Data & AI specialists" },
            { value: "10K +", label: "Global certifications" },
            { value: "$2B", label: "Investment in AI, Cloud & Cyber" },
            { value: "100 +", label: "Unique global clients" },
            { value: "330 +", label: "AI & agentic AI assets in agent fleet" },
            { value: "47 +", label: "Countries served" },
          ],
        },

        {
          section: "Deloitte: AI market leader, Google AI stack expertise",
          type: "cards",
          cols: 3,
          items: [
            {
              num: "01",
              title: "Expertise on the Google AI Stack",
              body:
                "330+ agents and 15+ Gemini Enterprise connectors built through deep co-innovation with Google.",
            },
            {
              num: "02",
              title: "Scaled Implementation Experience",
              body:
                "Implemented AI-enabled solutions for 100+ clients using Google services and tailored offerings.",
            },
            {
              num: "03",
              title: "Value-Driven Transformation",
              body:
                "A structured, value-led approach backed by agentic rollout accelerators that turn agents from cost centres into value drivers.",
            },
            {
              num: "04",
              title: "Deep Industry & Domain Expertise",
              body:
                "Sector-specific expertise, combined with targeted domain depth across sales, marketing, IT, HR and Finance.",
            },
            {
              num: "05",
              title: "Grounded in Ethical & Trustworthy AI",
              body:
                "Deloitte's Trustworthy AI Framework embeds technical guardrails, risk mitigation, explainability and security into every deployment.",
            },
          ],
        },
      ],
    },

    /* ============================================================== 03 */
    {
      id: "leaders",
      label: "Worldwide Leaders",
      title: "Worldwide *Leaders*",
      lead:
        "Deloitte have consistently led the category for worldwide leader in custom software development.",

      sections: [
        {
          type: "supportbar",
          title:
            "Recognised as worldwide leaders for custom software development in 2023, 2024 and 2025",
          body: "The 2026 Gartner Magic Quadrant is yet to be released.",
        },

        /* Two short cards side by side; the long recognition narrative runs
           full width underneath rather than stretching two near-empty cards
           beside it. */
        {
          type: "cards",
          cols: 2,
          items: [
            {
              num: null,
              title: "Industry Recognition",
              body:
                "Our efforts are consistently recognised by leading industry analysts including Gartner, IDC and Forrester, validating our strategic vision and execution excellence. These independent third-party validations provide customers with assurance of quality, innovation and proven delivery capability.",
            },
            {
              num: null,
              title: "2025 Worldwide Leader",
              body: [
                "\u201CDeloitte\u2019s current and future focus is to help clients transform their businesses by providing comprehensive CSD services, leveraging deep industry expertise to deliver tailored solutions and measurable business outcomes.\u201D",
                "\u2014 Gartner, 2025 Magic CSD Quadrant extract",
              ],
            },
          ],
        },

        {
          section: "Worldwide Leaders three years straight",
          type: "text",
          body: [
            "We have been recognised as the Leader in the Gartner\u00AE Magic Quadrant\u2122 for Custom Software Development Services, Worldwide since 2023. This prestigious designation places Deloitte as the top partner globally. Leader status signifies that Deloitte demonstrates strategic excellence \u2014 a clear, forward-thinking vision aligned with emerging technologies and market trends \u2014 and operational excellence, consistently delivering high-quality solutions on time and within budget with measurable business impact and exceptional customer satisfaction.",
            "For the Cognitive AI implementation, this recognition provides assurance that you are partnering with a vendor that combines proven delivery excellence, strategic innovation leadership and unwavering commitment to long-term customer success. Deloitte\u2019s Leader status reflects our global scale, enterprise-grade capabilities, expertise in complex system integration, security and compliance excellence, and commitment to managed services and continuous improvement \u2014 all critical factors for the Cognitive AI programme.",
          ],
        },

        {
          /* Export the quadrant graphic from the source deck at full
             resolution into assets/img/, then set `src` below. A crop of a
             screenshot will not hold up on a projector. */
          type: "image",
          src: "" /* "assets/img/gartner-mq-2025.png" */,
          placeholder: "Magic Quadrant chart \u2014 add the file and set `src` above",
          caption:
            "Gartner\u00AE Magic Quadrant\u2122 for Custom Software Development Services, Worldwide. As of September 2025. \u00A9 Gartner, Inc.",
        },
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
