# 🔴 Red Button

One tap to get started. **Red Button** is an Android app for starting timed intervals through a simple interface, built with Kotlin and Jetpack Compose.

Choose a duration, tap the red button, and follow the countdown. The button turns green while the interval is active and returns to red when it ends.

## Features

- **15, 30, and 60-minute** intervals, with 30 minutes selected by default.
- Countdown displayed in `MM:SS` format.
- Visual status indicator and animated button color transitions.
- Option to reset an interval before it ends.
- Spanish-language interface with an adaptive layout and vertical scrolling.
- A resizable home screen widget with a large circular button and a transparent background.
- Shared, persisted timer state across the app and all widget instances.
- Offline operation. Boot broadcasts refresh the widget after a device restart.

## How to use

1. Open the app and choose an interval duration.
2. Tap **ACTIVAR** (Activate) to start the countdown.
3. Check the remaining time while the button is green.
4. Tap **Restablecer intervalo** (Reset interval) to cancel it, or wait for it to finish.

Duration selection is disabled while an interval is active. Once it finishes or is reset, you can start another interval.

### Home screen widget

1. Install and open the app at least once.
2. Long-press an empty area of your Android home screen and select **Widgets**.
3. Find **RED BUTTON** and drag it onto the home screen.
4. Long-press the widget to move or resize it, depending on your launcher.

Tap the large red button to start the duration selected in the app (30 minutes by default). It turns green while active. Tap the active button to open the app, where you can check the remaining time or reset the interval. Every widget instance controls the same timer.

## Tech stack

| Component | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose and Material 3 |
| Screen state | Compose State and `rememberSaveable` |
| Timer | Coroutines and `SystemClock.elapsedRealtime()` |
| Build system | Gradle with Kotlin DSL and a version catalog |

## Requirements

- Android Studio compatible with the project's Gradle and Android Gradle Plugin versions.
- Android SDK with the **API 37** platform installed, as required by the current build configuration.
- A JDK compatible with the project's Gradle Wrapper and Android Gradle Plugin. You can use Android Studio's bundled JDK if compatible.
- A device or emulator running **Android 7.1 (API 25)** or later.

Dependency and plugin versions are defined in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Getting started

1. Clone the repository:

   ```powershell
   git clone https://github.com/RayTrunski/ReminderApp.git
   cd ReminderApp
   ```

2. Open the cloned folder in Android Studio.
3. Wait for Gradle sync to complete and install any SDK components requested by the IDE.
4. Select an emulator or connect a device with USB debugging enabled.
5. Launch the **app** module using the **Run** button.

> The GitHub repository is named `ReminderApp`; the Android project is named `RedButtonApp`.

### Build from PowerShell

With the JDK and Android SDK configured:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run tests

Local unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Instrumented tests, with a connected device or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Project structure

```text
app/
├── src/main/
│   ├── java/com/example/redbuttonapp/
│   │   ├── MainActivity.kt       # Screen, state, and timer logic
│   │   └── ui/theme/             # Compose theme
│   ├── res/                     # Android resources
│   └── AndroidManifest.xml
├── src/test/                    # Unit tests
└── src/androidTest/             # Instrumented tests
gradle/
├── libs.versions.toml           # Dependency catalog
└── wrapper/                     # Gradle Wrapper
```

## Current scope

The app and widget share a persisted timer that survives normal app closure. Restarting the device clears the active interval. Timer state is excluded from backups to avoid restoring an interval on another device.

The current version does not include notifications, sound, vibration, or interval history. Widget expiration uses an alarm without special exact-alarm access: Android may delay the final widget refresh during Doze or battery saving. Until refreshed, the widget may still appear green. Opening the app or interacting with the widget reads the current state immediately. Force-stopping the app prevents background updates until it is opened again.

Instrumented tests cover shared timer state, activation, reset, expiration, and invalidation after a device restart. These tests require a device or emulator and clear the saved timer state.
