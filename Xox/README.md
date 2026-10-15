# Xox

Xox is an Android 8.0+ assistant-launcher prototype, initially targeted at Android 8.1 / MIUI on the Redmi 5. It requires no root.

## Features in this prototype

- Assistant activity registered for `android.intent.action.ASSIST` so it can be offered as an assist app on compatible Android builds.
- Lock-screen display flags (`showWhenLocked` / `turnScreenOn`) without dismissing the secure keyguard.
- Optional volume-key sequence shortcut through an explicitly enabled Accessibility service.
- GitHub Actions workflow to compile a debug APK and upload it as a workflow artifact.

## Important limitations

- Home-button assistant activation depends on the ROM exposing Xox in its Assist app picker. Some MIUI versions may only list apps with a full VoiceInteractionService, so this prototype may not appear there.
- Android accessibility key filtering is device/ROM-dependent. Volume-key sequences may not be delivered reliably, and other apps/system functions can consume keys first.
- A normal app cannot bypass a PIN/pattern or unlock protected apps. `showWhenLocked` only allows Xox's own activity to appear over the lock screen.
- This is a source prototype; the APK must be built and tested on the target phone. No APK is checked in.

## Build with GitHub Actions

The project is in the `Xox/` folder of this repository. Pushes to the default branch should trigger **Build Xox APK**. Open **Actions**, select the workflow run, and download the `Xox-debug-apk` artifact when it finishes.

## Enable the volume shortcut

1. Open Xox and tap **Configure shortcut**.
2. Choose the key sequence.
3. Tap **Open Settings**, find Xox under downloaded/installed accessibility services, and enable it.
4. Lock the phone and test the sequence. If MIUI does not pass key events to accessibility services while locked, that shortcut won't work on that firmware.

## Try Home assistant activation

Open **Settings → Apps → Default apps** (or **Assist & voice input**, depending on MIUI) and look for Assist app. If Xox appears, select it and test the Home-button assistant gesture.
