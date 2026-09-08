/* ---------------------------------------------------------------------------
   Tab 4 — Demo.

   This page is your running order for the live walk-through. Keep it on the
   second screen if you have one, or read it once before you switch to the app.

   To drop in screenshots instead of running live, put the image files in
   assets/ and reference them like this:

       ![Today dashboard](assets/today.png)
   --------------------------------------------------------------------------- */
page({
  slug: 'demo',

  eyebrow: 'Demo',
  title: 'Let us show you',
  lead: 'Ten minutes in the working application. Everything below is live — nothing here is a mock-up.',

  body: `
::: note.accent Before switching
The app is running at **http://localhost:8081**. Signed in as the demo account
with a month of seeded history behind it.
:::

## Running order

::: steps
### Today — the first screen
Start where the user starts. Calories against target, macros with what remains,
weight and trend, recovery and strain, and the calculated insight strip.

Point out that none of these numbers came from a model.

### Log a meal by typing
Quick-add from the bottom right. Name, meal, time, macros. Show the totals
update immediately.

### Log a meal from a saved food
Same entry point, two taps, quantity scales every macro. This is the path a
returning user actually uses.

### Describe a meal in a sentence
Type a real sentence. Show the review screen: detected items, confidence, the
assumptions the model had to make. Edit one number in front of the room, remove
one item, then save.

This is the moment to say it out loud — nothing is logged until this screen is
confirmed.

### Photograph a meal
Same flow, same review screen. If the room's lighting is poor, use the prepared
photo rather than fighting the camera.

### Training
Add a session with an exercise and a couple of sets. Show that it lands on
Today.

### WHOOP
Settings → WHOOP. Show the connection state, the last sync time, and a manual
sync. Then back to Today to show recovery and strain in place.

### Progress
Daily, weekly, monthly. Switch one chart to its table view — this is the
accessibility commitment, not a fallback.
:::

## If something goes wrong

::: cards
### The AI features are unavailable
Say so and move on. Manual and saved-food logging are unaffected, and that is
the point of the design: the optional parts announce themselves rather than
breaking the app.

### WHOOP will not sync
Show the stored history instead. The dashboard is fully functional with no
device connected — a no-op adapter is active when nothing is attached.

### The laptop dies
This deck prints to PDF from any browser, and the screenshots below cover the
same ground.
:::

## Screenshots

Replace this section with images once you have them — put the files in
**assets/** and reference them with the image syntax shown at the top of
content/demo.js.
`
});
