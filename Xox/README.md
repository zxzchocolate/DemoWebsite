# Xox

Xox is a minimal Android 8.0+ digital-assistant prototype for compatible Android builds, including testing on Android 8.1 / MIUI.

## What it does

- Registers an Android `VoiceInteractionService` so compatible phones can offer Xox as the default digital assistant.
- Shows a simple Xox panel when the system invokes the assistant session.
- Keeps a normal launcher activity.
- Does not declare or use an Accessibility Service, intercept volume keys, request assist data, or listen to the microphone.

## Set Xox as assistant

Open **Settings → Apps → Default apps → Digital assistant app** (the wording varies by Android/MIUI version) and choose Xox if it appears.

Some MIUI versions do not expose third-party assistants in the picker or may restrict assistant gestures. Xox cannot force itself to become the default. This is only a minimal assistant shell, not a voice-recognition or AI assistant. Android may still require the PIN/pattern to access protected parts of the phone.

## Build with GitHub Actions

The project is in the `Xox/` folder of this repository. Open **Actions**, select the latest **Build Xox APK** run, and download the `Xox-debug-apk` artifact when it finishes.
