# வைகறைப் பாணி

A React + TypeScript + Capacitor Android app foundation for a calm alarm, world clock, stopwatch and timer experience.

## Current implementation status

- Responsive dark/light UI, alarms CRUD stored locally, world clock display, stopwatch and in-app countdown timer are included.
- This is an initial foundation, **not a production-ready alarm APK**. Android exact alarm scheduling, boot recovery, foreground/lock-screen ringing, native audio playback, persistent background timers, and Spotify Android playback have not yet been implemented or verified.
- Spotify Client ID and redirect URI are configuration values only. Spotify's written alarm-use approval has been reported by the project owner but has not been independently verified here. Keep the approval record and comply with its exact scope and Spotify's developer terms.
- PixelNex is configured as the intended Supabase project reference, but no database schema or Edge Function changes have been made. Its existing app functions must be preserved.

## Setup

1. Install Node.js 20+ and Android Studio with Android SDK.
2. Copy `.env.example` to `.env` and fill in the Supabase publishable key from the Supabase dashboard. Never put a Supabase secret/service-role key in the app.
3. Run `npm install`, `npm run dev` for web preview.
4. Run `npm run build`, `npx cap add android`, then `npx cap sync android` and open with `npx cap open android`.

## Spotify

Register `vaigaraipaani://spotify-callback` as the exact Redirect URI in Spotify Developer Dashboard. Use Authorization Code with PKCE; do not create or embed a client secret in the Android app. Spotify audio/alarm functionality must remain disabled until the written approval's scope is confirmed and the native integration is implemented and tested.

## Supabase

Project ref: `uwvizniuhecczokwwgur`. No schema migration is included. Review the existing PixelNex tables/functions and configure RLS before adding any app-specific data.

## Android release

A signed release APK requires a project-owned signing keystore and passwords configured as GitHub Actions secrets. Do not commit keystore files or passwords. A successful web build does not prove native alarm reliability; test exact alarms, reboot, Doze/battery restrictions, lock screen, audio focus and notification permission on physical Android devices.
