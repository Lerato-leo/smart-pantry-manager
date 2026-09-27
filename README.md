# Spens

Spens (Afrikaans for pantry/larder) is an Android app for tracking what's in your kitchen and
figuring out what you can actually cook with it. Add ingredients as you buy them, and the app
matches your stock against a small cookbook of South African dishes, only suggesting a recipe
once every ingredient it needs is on the shelf in enough quantity. It also reminds you a few
days before something goes off, so less of it ends up in the bin.

## Architecture

The data layer is built on Room rather than raw SQLite. Room catches schema mistakes at compile
time (a typo in a column name fails the build instead of crashing on a device), generates the
DAO boilerplate that's tedious and error-prone to hand-write, and plugs straight into `LiveData`
so the UI updates itself whenever the database changes instead of needing manual refresh calls.

Screens follow MVVM: each `Fragment`/`Activity` owns a `ViewModel`, which goes through a
`Repository` to reach the `Dao`. All writes run on a background executor so nothing blocks the
main thread. The pantry-to-recipe matching itself (`RecipeMatcher`) has no Android dependencies,
which is what makes it possible to unit test without an emulator.

## Running it

1. Android Studio with an SDK covering API 24 (minSdk) through 34 (targetSdk/compileSdk), and a
   JDK compatible with Gradle 8.5 (JDK 17 is what this project builds against).
2. Clone the repository and open it in Android Studio; let Gradle sync.
3. Run the `app` configuration on an emulator or device running Android 7.0 (API 24) or later.

Unit tests live under `app/src/test` and run with `./gradlew testDebugUnitTest`.

## Notes

Built as a university assignment.
