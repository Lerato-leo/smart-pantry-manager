# Smart Pantry Manager

## Description
An Android app designed to help users reduce food waste by tracking pantry ingredients and suggesting recipes that can be made with the ingredients currently available. The app uses a strict-matching algorithm to suggest only recipes where every required ingredient is present in sufficient quantity.

## Database Choice
The app uses SQLite for local persistence via a hand-written `SQLiteOpenHelper`. This choice was made because:
- It is lightweight and built into the Android framework.
- It does not require additional dependencies or external services.
- It is suitable for the small to medium amount of data expected in a pantry management app.
- It provides full control over the database schema and queries.

## Setup and Run Instructions
1. **Prerequisites**:
   - Android Studio Arctic Fox or later (tested with Android Studio Flamingo)
   - Java Development Kit (JDK) 11 or later
   - Android SDK with minSdk 24 and targetSdk 34

2. **Setup**:
   - Clone the repository: `git clone https://github.com/Lerato-leo/smart-pantry-manager.git`
   - Open the project in Android Studio.
   - Wait for Gradle to sync (may take a few minutes).
   - Ensure you have an Android emulator or device connected with API level 24 or higher.

3. **Run**:
   - Select the desired run configuration (app) and click the Run button.
   - The app should launch on the emulator or device.

4. **Generating a Signed APK** (for release):
   - Follow the instructions in the Android Studio documentation for generating a signed bundle or APK.

## Notes
- This project was created as a university assignment.
- Feel free to contribute by opening issues or submitting pull requests.