# Spens

Spens (Afrikaans for pantry/larder) is an Android app for tracking what's in your kitchen and
figuring out what you can actually cook with it. Add ingredients as you buy them, and the app
matches your stock against a small cookbook of 20 South African dishes, only suggesting a recipe
once every ingredient it needs is on the shelf in enough quantity. It also reminds you a few
days before something goes off, so less of it ends up in the bin.

## Features

- **My Spens**: add, edit and delete pantry items (name, quantity, unit, optional expiry date).
  Items expiring soon or already past their date are colour-coded. Delete with the bin button
  (asks first) or by swiping a card away (with Undo).
- **What Can I Cook**: the recipes you can make right now, and nothing else. Below them, a
  separate **Almost there** section lists recipes exactly one ingredient short and says what's
  missing, e.g. "Missing 1 can baked beans" or "Need 200 g more flour".
- **Recipe detail**: the full ingredient list and method for any recipe.
- **Settings**: turn on going-off alerts, a daily notification about ingredients expiring
  within 3 days.

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
belongs to one recipe). The 20 recipes are seeded on first launch.

## How recipe matching works

`RecipeMatcher` applies a strict rule: a recipe is suggested only if **every** ingredient is in
the pantry in at least the required quantity. Four out of five is not a match. To cope with how
people actually type things:

- **Names** are compared case-insensitively and singularised: "Tomatoes" matches "tomato",
  "Apples" matches "apple", "Onions" matches "onion".
- **Units** are converted within the same kind of measure: mass (mg, g, kg, oz, lb) and volume
  (ml, l, tsp, tbsp, cup). So 1 kg of flour covers a recipe needing 500 g, and 750 ml of oil
  covers 2 tbsp. Mass and volume are never mixed, since that would mean guessing the density.
- Several entries for the same ingredient are added together (500 g + 1 kg = 1.5 kg).

## Architecture

Screens follow MVVM: each `Fragment`/`Activity` owns a `ViewModel`, which goes through a
`Repository` to reach the `Dao`. All writes run on a background executor so nothing blocks the
main thread. `RecipeMatcher` has no Android dependencies, which is what makes it possible to
unit test without an emulator.

## Running it

1. Install Android Studio with an SDK covering API 24 (minSdk) through 34 (targetSdk and
   compileSdk), and JDK 17 (the project uses Gradle 8.5).
2. Clone the repository and open the folder in Android Studio. Let Gradle sync.
3. Pick an emulator or a device running Android 7.0 (API 24) or later, and run the `app`
   configuration.

## Tests

- Unit tests (no device needed): `./gradlew testDebugUnitTest`. These cover the matching rules,
  unit conversion, the "almost there" list, quantity formatting and expiry date parsing.
- Database tests (needs a running emulator or device): `./gradlew connectedDebugAndroidTest`.
  These run pantry create/read/update/delete against a real Room database and check the seeded
  recipes. Note that this uninstalls the app afterwards, which clears its data.

## Notes

Built as a university assignment for Mobile App Development 700.
