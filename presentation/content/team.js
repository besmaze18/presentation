/* =====================================================================
   TEAM  —  delivery leadership.

   Each person is one entry in the `team` block below:

     { name: "Full Name", role: "Program title — what they own",
       photo: "assets/img/name.jpg" }

   `photo` is optional. Leave it as "" and the card shows the person's
   initial in a circle instead, so the page never looks broken while you
   are still collecting headshots. Put the image files in assets/img/.
   ===================================================================== */
window.CONTENT = {
  title: "Delivery *leadership*",
  subtitle:
    "The following Partner group will be the key Partner group focused on various aspects of the program.",

  sections: [
    {
      type: "team",
      items: [
        {
          photo: "",
          name: "Peter Stojkovski",
          role:
            "Engagement Partner — oversees the overall success of the engagement and collaborates with internal and external stakeholders to ensure the program delivers its intended outcomes.",
        },
        {
          photo: "" /* assets/img/aditi-nitin.jpg */,
          name: "Aditi Nitin",
          role: "[Program title and role — see note below]",
        },
        {
          photo: "" /* assets/img/emanuel-durou.jpg */,
          name: "Emanuel Durou",
          role: "[Program title and role — see note below]",
        },
        {
          photo: "" /* assets/img/mohamad-madhoun.jpg */,
          name: "Mohamad Madhoun",
          role: "[Program title and role — see note below]",
        },
      ],
    },

    {
      section: "Still to add",
      type: "text",
      body: [
        "Only the first row of the Partner table was legible in the material supplied — Peter Stojkovski, Engagement Partner. The three names above come from the photo strip on the same slide, so their program titles and role descriptions are placeholders.",
        "Send the rest of the table and this page is complete. Headshots go in assets/img/ and are wired up with the `photo` field in content/team.js; until then each card shows the person's initial.",
      ],
    },
  ],

  footer: "Confidential — prepared for {{CLIENT}}.",
};
