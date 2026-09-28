# Dijong

*Cook what you have. Waste nothing.*

Dijong is an Android app for tracking what's in your kitchen and figuring out what you can
actually cook with it. Add ingredients as you buy them, and the app matches your stock against a
cookbook of 20 South African dishes, only suggesting a recipe once every ingredient it needs is
on the shelf, in date, in enough quantity. It also flags food that's about to go off, so less of
it ends up in the bin.

## Features

- **Dijong** (the main page): add, edit and delete pantry items (name, quantity, unit, category,
  optional expiry date). Each card shows its category, and anything expiring soon or already
  expired gets a "Tomorrow" / "In 2 days" / "Expired" badge and moves to the top. Delete from the
  edit screen or by swiping a card away; both offer Undo.
- **What Can I Cook**: the recipes you can make right now, and nothing else. Below them, a
  separate **Almost there** panel lists recipes exactly one ingredient short and says why:
  "Missing: baked beans", "Need 200 g more butternut" or "Expired: milk".
- **Recipe**: every ingredient ticked if your pantry has enough, or marked "Missing", "Not
  enough" or "Expired", and the method as numbered steps.
- **Settings**: expiring-soon alerts (a daily notification about food expiring within 3 days),
  show or hide the Almost There list, metric or imperial units (one or the other: the unit menu
  and recipe amounts follow the choice), and a button to restore the built-in recipes.

## Database: SQLite, through Room

The app stores everything in a local SQLite database on the device, accessed through Google's
Room library. I chose on-device SQLite over Firebase or PostgreSQL because:

- A pantry is personal and only needs to live on one phone, so a cloud backend would add
  accounts, network errors and hosting for no real benefit.
- It works fully offline, which matters in a kitchen or on a patchy connection.
- It's the approach covered in the module, and needs no server that the marker has to run.

Room rather than a hand-written `SQLiteOpenHelper` because it checks SQL at compile time (a
typo in a column name fails the build instead of crashing on a device), generates the DAO
boilerplate, and returns `LiveData`, so screens refresh themselves whenever the data changes.

The schema has three tables: `pantry_items`, `recipes`, and `recipe_ingredients` (each row
belongs to one recipe). The database is at version 2: version 2 added a `category` column to
`pantry_items`, and `MIGRATION_1_2` adds it without losing existing items (checked by
`MigrationTest`). Everything is saved on the device, so pantry items are still there after the
app is closed or the phone restarts.

## The built-in recipes

`SouthAfricanRecipeSeeder` holds 20 South African recipes (the brief asks for 15 to 20), each
with a name, its ingredients and a numbered method, sized for about four people. The data
follows a few rules so that strict matching works on it:

- Dishes and ingredients keep their South African names (pap, chakalaka, boerewors, biltong,
  rooibos, potjiekos), with English everywhere else ("Pap and Chakalaka").
- Every ingredient uses the unit it's bought or measured in here: a 10 g sachet of yeast, a
  400 g roll of pastry, a can of baked beans, spoons of curry powder.
- The same ingredient has the same name in every recipe, so one pantry item serves them all.
- Tap water is never an ingredient; the method says how much to boil.
- Every ingredient a step uses is in that recipe's list.

The recipes are checked **every time the app starts**, not only on first launch. If they're
missing or incomplete (say the app was closed halfway through loading them), or if an update
ships improved recipes (a higher `SEED_VERSION`), they're reloaded in one transaction. Pantry
items are never touched. `RecipeSeederTest` checks the data rules above and this start-up
repair.

## How recipe matching works

`RecipeMatcher` applies a strict rule: a recipe is suggested only if **every** ingredient is in
the pantry in at least the required quantity. Four out of five is not a match. Strict also
means:

- **Only usable food counts.** Anything past its expiry date is left out, so expired milk can't
  make milk tart look ready. The recipe screen marks it "Expired" rather than "Missing".
- **An ingredient listed twice must be covered twice.** Repeated lines are added together
  before checking (1 onion + 1 onion needs 2 onions).
- **Tap water is never required**, and a recipe that needs nothing from the pantry is never
  suggested.

To cope with how people actually type and shop, it's forgiving about how things are written,
but never about whether they're there:

- **Names** ignore case, plurals, punctuation and extra spaces: "Tomatoes", "tomato" and
  "green-peppers" all match their recipe ingredient. A short list of true equivalents is
  recognised, such as "mielie meal" for maize meal, "cake flour" for flour and "mince" for beef
  mince. There's no loose word matching: beef stock never counts as beef.
- **Units** are converted within the same kind of measure: mass (mg, g, kg, oz, lb) and volume
  (ml, L, fl oz, tsp, tbsp, cup). So 1 kg of flour covers a recipe needing 500 g, and 750 ml of
  oil covers 2 tbsp.
- **Spoons and grams** are converted for ingredients that recipes measure in spoons but shops
  sell by weight (curry powder, cinnamon, ginger, sugar, flour, yeast, baking powder,
  bicarbonate of soda, butter, honey, apricot jam, chutney), using a typical density for each:
  a 100 g packet of curry powder covers "1 tbsp". For everything else, mass and volume are
  never mixed, since that would mean guessing the density.
- Several pantry entries for the same ingredient are added together (500 g + 1 kg = 1.5 kg).

## Architecture

Screens follow MVVM: each `Fragment`/`Activity` owns a `ViewModel`, which goes through a
`Repository` to reach the `Dao`. All writes run on a background executor so nothing blocks the
main thread. `RecipeMatcher` and the other helpers in `util` have no Android dependencies, which
is what makes it possible to unit test them without an emulator.

## Running it

1. Install Android Studio with an SDK covering API 24 (minSdk) through 34 (targetSdk and
   compileSdk), and JDK 17 (the project uses Gradle 8.5).
2. Clone the repository and open the folder in Android Studio. Let Gradle sync.
3. Pick an emulator or a device running Android 7.0 (API 24) or later, and run the `app`
   configuration.

## Tests

- Unit tests (no device needed): `./gradlew testDebugUnitTest`. These cover the matching rules
  (expired stock, repeated ingredients, alternative names, water), unit conversion, the Almost
  There list, expiry badges and sorting, date parsing, quantity formatting and splitting recipe
  methods into steps.
- Database tests (needs a running emulator or device): `./gradlew connectedDebugAndroidTest`.
  These run pantry create/read/update/delete against a real Room database, check the built-in
  recipes and their start-up repair, and upgrade a version 1 database to version 2. Note that
  this uninstalls the app afterwards, which clears its data.

## Screenshots

Screenshots of every screen and core function are in [`docs/screenshots`](docs/screenshots), with a caption for each in its [index](docs/screenshots/README.md).

## Notes

Built as a university assignment for Mobile App Development 700.
