# વைகறைப் பாணி

An Android-first alarm and time companion. All music search uses the public Apple iTunes Search API. No Spotify, Joytify, or unofficial music API is used.

## Music search
- Searches the India and US iTunes song catalogues concurrently after a 500 ms typing pause.
- Keeps only tracks with a playable 30-second preview, de-duplicates by track ID, and lists India results first.
- Preview clips are the alarm audio source. The app downloads and stores selected previews and artwork locally for offline use.
- iTunes Search API is keyless. Availability and catalogue contents depend on Apple's regional search results.

## Android build
GitHub Actions builds the Capacitor Android app and uploads a debug APK artifact on every push to main.

## Alarm runtime
Exact alarms use Android AlarmManager. Native alarm records are restored after reboot, app update, time changes and timezone changes. A foreground media-playback service loops the locally cached preview and falls back to the bundled default alarm tone if the selected audio is unavailable. The ringing activity provides Snooze and Dismiss controls on the lock screen.

Android permissions include notifications, exact alarms, full-screen alarm notifications, and battery optimization exemption. Android/OEM settings can still restrict background behaviour, so physical-device testing is required.

## Local storage and privacy
Music previews and artwork are cached in the app's private files directory. Alarm metadata is stored locally. No music account login is required.

## Signing
The CI artifact is a debug APK for installation and testing. A production release APK requires a private signing keystore and protected CI secrets. Never commit signing keys or passwords.
