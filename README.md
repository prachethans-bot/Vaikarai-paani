# வைகறைப் பாணி (Vaigarai Paani)

An Android-first alarm and time companion with the permanent silver wordmark, native alarm scheduling, repeating schedules, snooze/dismiss, world clocks, stopwatch, countdown timer and Spotify track selection.

## Android build
GitHub Actions builds an installable debug APK on every push to main. The workflow generates the Capacitor Android host, adds the native AlarmBridge plugin, downloads Spotify's official App Remote AAR and uploads the APK artifact.

## Alarm architecture
- Exact alarm scheduling uses Android AlarmManager.setAlarmClock.
- Alarm records are kept in Android SharedPreferences and restored on reboot, app update, time change and timezone change.
- Full-screen ringing uses a native lock-screen activity with Snooze (5 minutes) and Dismiss.
- Native alarm audio requests Spotify App Remote for a selected track and uses the device's default alarm tone as a fallback.
- Countdown completion uses Android's alarm service and can fire when the WebView is closed.
- Android may require exact-alarm, notification and full-screen-intent permissions. OEM battery controls can affect delivery and should be acceptance-tested on the target device.

## Spotify requirements
Spotify App Remote uses the Spotify app for playback; Spotify must be installed and signed in. The registered Android package name and signing-certificate fingerprint must match the Spotify Developer Dashboard app configuration. No client secret is embedded in this app.

## Supabase
PixelNex has not been modified. This version stores alarms on-device so that scheduling does not depend on network availability. No database migrations or Edge Function changes are performed.

## Signing and release
The CI artifact is a debug-signed APK for direct installation and acceptance testing. A production release APK requires the project's private signing keystore and protected CI secrets. Do not commit signing keys or passwords.

## Device acceptance checklist
Test ringing while locked, after app closure, after reboot, with battery saver enabled, exact alarm access granted and denied, Spotify connected/disconnected, and Spotify not installed. Physical device testing is required to validate OEM-specific alarm behaviour.
