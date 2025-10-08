# XUIManager Android

XUIManager is a Jetpack Compose Android application for managing Xtream UI
(XUI) reseller and administrator panels from a phone or tablet. After
authenticating with your panel credentials the app surfaces the data and actions
resellers rely on every day: customer subscriptions, reseller hierarchies,
currently connected viewers, and live channel watcher counts. Renewals, ad-hoc
API calls, and inline filtering tools make it easy to keep your panel organised
without reaching for a desktop browser.

## Feature highlights

- **Secure login** – authenticate against your XUI instance, optionally enabling
  HTTP logging to debug connectivity problems during development.
- **Dashboard overview** – view resellers, subscriptions, online users, and
  channel watcher statistics in dedicated cards with quick search and detail
  dialogs.
- **Powerful filtering** – limit subscriptions by status and channel watchers by
  channel identifier without re-entering credentials.
- **Renewals in seconds** – renew subscriptions for any number of months directly
  from the dashboard.
- **Custom API actions** – craft bespoke reseller API requests with multiple
  parameters, send them on demand, and reuse the responses immediately.
- **Clipboard friendly details** – inspect every field returned by the API and
  copy the full payload to the clipboard for further analysis.
- **Session persistence** – optionally remember the current token on-device so
  the dashboard restores instantly the next time you launch the app.
- **One tap refresh and logout** – refresh all dashboard data or clear the
  session token from the top app bar.

## Project structure

The Android application lives entirely inside the [`android/`](android/)
folder and is organised into a lightweight MVVM stack:

- `data/` – Retrofit service definitions, repository logic, DataStore-backed
  session persistence, and serialisable response models.
- `ui/` – Jetpack Compose screens, widgets, and the `XuiViewModel` that powers
  them.
- `MainActivity` – entry point that wires the Compose UI to the view model.

Networking uses Retrofit with Kotlin serialization for resilient parsing while
OkHttp handles connectivity and optional logging.

## Getting started

1. Open the `android/` directory in Android Studio (Giraffe or newer).
2. Allow the IDE to download the Android Gradle Plugin 8.1, Kotlin 1.9, and
   Compose dependencies.
3. Create a run configuration for the `app` module and deploy it to an emulator
   or device running Android 7.0 (API 24) or later.

Alternatively, build from the command line using the Gradle wrapper:

```bash
cd android
./gradlew assembleDebug
```

## Usage tips

- Enter the base URL to your reseller portal exactly as you would in a browser.
  The app automatically normalises missing schemes and trailing slashes.
- Enable request logging from the login screen if you need to inspect the raw
  HTTP traffic in Logcat.
- Toggle **Remember session** to store the authenticated token securely using
  DataStore so you can jump straight back to the dashboard after closing the
  app.
- Tap any list item to open a detail dialog. Use the **Copy data** button to
  place the formatted payload on the clipboard.
- The custom action card accepts multiple key/value pairs. Use it to call
  lesser-used reseller API actions without leaving the app.
- Pull-to-refresh style updates are handled by the refresh icon in the top app
  bar.

> **Note:** Available actions and field names depend on your specific XUI
> deployment. Consult your panel's documentation for the full reseller API
> reference.

## Contributing

Issues and pull requests are welcome! Please describe the XUI endpoint you are
integrating with so we can reproduce any problems quickly.
