# preAlarm

Android app (Kotlin + Jetpack Compose + Material 3) to leave home **on time and stress-free**.

## The idea

You have to leave home at, say, 14:00, but first you have a list of things to do, in order, and you know roughly how long each one takes. preAlarm turns that list into a guided plan:

- It calculates **when you need to start** and schedules an exact alarm for your departure time.
- When the session starts, it shows a **countdown for the current task** and plays a **soft sound alert** (a notification-style alert, not a dismissable alarm) when each period ends.
- You tap **"Done!"** when you finish each task, so the app can tell you whether you are **on time, ahead, or behind schedule**.
- The final alarm reminds you it is time to leave.

## Screens

- **Home**: your saved routines, a big "New routine" button, a Help button and a Settings button.
- **Editor**: routine name, departure time, ordered tasks with name + approximate minutes. You can edit or delete each task after adding it, and a **live preview** shows what time you would have to start with the current plan. From here you can **Cancel**, **Save**, or **Start without saving** (temporary routine).
- **Session**: current task with a large countdown, progress, "Done!" button, the full task timeline, and a status chip (on time / ahead / behind).
- **Settings**: language, theme (system / light / dark), dynamic colors (Material You), alert sound with preview, alert channel (alarm or media volume) and vibration.

Temporary routines run exactly like saved ones but are not persisted; just close or stop the session and nothing is stored.

## Design

- Material 3 (Material You): dynamic color on Android 12+, custom light/dark palettes, rounded shapes.
- A cute, low-contrast vector background (moon, clouds, sparkles) tinted with your theme's primary color so it adapts to Material You without being distracting.
- Playful title: bundled **Baloo 2** font (SIL Open Font License) with a logo badge.
- Localized into **English (default), Spanish, Catalan, German, French, Italian, Japanese and Chinese**. The app follows the device language by default and can be overridden per-app from Settings (AndroidX per-app locales).

## Technical details

- Single-module Kotlin app, Jetpack Compose, Material 3, no dependency injection or navigation libraries — as lightweight as possible (~1.4 MB release APK with R8).
- Persistence with Jetpack **DataStore**: routines stored as JSON (kotlinx.serialization), settings as typed preferences.
- Scheduling with `AlarmManager` exact alarms (`setExactAndAllowWhileIdle`), one per task end plus the departure alarm; the receiver plays the alert through a `Ringtone` on the configured channel (alarm or media) and vibrates, with no need to dismiss anything.
- Minimum SDK 26, target/compile SDK 35, Java 17, AGP 8.7.3, Kotlin 2.0.21.

## Building

```bash
./gradlew assembleDebug      # debug APK
./gradlew testDebugUnitTest  # unit tests
./gradlew assembleRelease    # release APK (see signing below)
```

You need JDK 17 and an Android SDK (API 35). `local.properties` with `sdk.dir` is picked up automatically.

## Releasing and signing

Release builds are signed automatically if a `keystore.properties` file exists at the repo root (it is gitignored — never commit it):

```properties
storeFile=/absolute/path/to/prealarm.keystore
storePassword=your-store-password
keyAlias=prealarm
keyPassword=your-key-password
```

Create a keystore once with `keytool`:

```bash
keytool -genkeypair -v -keystore prealarm.keystore -alias prealarm \
  -keyalg RSA -keysize 2048 -validity 10000
```

Then `./gradlew assembleRelease` produces a signed APK at `app/build/outputs/apk/release/`.

Notes:

- The APK checked in under `dist/` is signed with a **test key** for convenience only — fine for sideloading and testing, not for distribution or Google Play.
- **Back up your keystore and passwords.** Losing them means you can never update an already-published app.
- For Google Play, prefer an Android App Bundle (`./gradlew bundleRelease`) with **Play App Signing**.

To publish a release on GitHub: *Releases → Draft a new release*, create a tag (e.g. `v1.2.1`), add release notes and attach the signed APK.

## Project structure

```
app/src/main/java/.../
  data/       Models, RoutineRepository (DataStore + JSON), SettingsRepository
  schedule/   ScheduleCalculator: builds the task plan from departure time + tasks
  alarm/      AlarmScheduler and AlarmReceiver (exact alarms, sound + vibration)
  ui/         AppViewModel, screens (Home, Editor, Session, Settings), theme, formatting
```
