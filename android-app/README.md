# Android App

Kotlin + Jetpack Compose client for MIANUCOM conference operations, covering both the admin
back-office and the logistics floor surfaces.

## Architecture

```
ui/theme/       Design system (see theme-details.md)
ui/components/  Reusable themed primitives
ui/screens/     Feature screens
ui/viewmodel/   Per-feature ViewModels + UiState
ui/navigation/  Role-aware nav graph
data/           Repository, session, ApiResult
network/        Retrofit service + client
```

Screens never call Retrofit directly. They observe a ViewModel, which calls `MianuRepository`,
which returns `ApiResult<T>` — so a failure is always rendered rather than swallowed.

## Design system

`ui/theme/` ports the web theme documented in `theme-details.md`:

- **Color.kt** — primitive ramps (primary/gold/neutral) plus semantic tokens for light and dark.
  Consume via `MianuTheme.colors`; `Theme.kt` mirrors them onto Material's `ColorScheme` so stock
  components inherit the brand.
- **Elevation.kt** — the card/raised/overlay shadow scale, corner radii, and `accentGlow`.
- **Glass.kt** — `glass`, `glassStrong`, `glassSheen`. Compose has no backdrop filter, so these
  approximate `backdrop-blur` with translucent gradients over the ambient mesh.
- **Motion.kt** — mount transitions (`MianuEnter`) and continuous effects (`shimmer`, `pulseSoft`,
  `floating`, `glowPulse`, `sheen`, `flashDanger`, `gradientShift`).
- **Ambient.kt** — the `.app-ambient` radial mesh. Wrap screens in `AmbientBackground`.
- **Type.kt** — the type scale. Inter/Sora are **not** bundled; both families fall back to the
  platform sans. Drop the TTFs into `res/font/` and swap `SansFamily` / `DisplayFamily` to enable
  them — every style routes through those two constants.

## Features

| Screen | Role | Backing endpoints |
| --- | --- | --- |
| Login / Register | all | `auth/login`, `auth/register` |
| Dashboard | both | `dashboard/overview`, `access/halls`, `teams`, `analytics/daily` |
| Meal scanning | organizer | `meals/swipe` |
| Hall access | organizer | `access/scan`, `access/halls` |
| Wallet | admin | `transactions`, `users/cards/{uid}` |
| Delegates | admin | `users` CRUD, `users/cards/link`, team assignment |
| Teams | both | `teams`, `teams/{id}/notifications` |
| Halls | admin | `access/halls`, `.../presence` |
| Reports | admin | `analytics/*`, `reports/*` |
| Settings | both | `settings/meal-windows` |

## NFC

`nfc/` owns the reader. It uses **reader mode** rather than foreground dispatch, so tags are handled
in-process instead of via intents, no other NDEF handler can steal a badge mid-service, and the
platform chime can be silenced.

- **NfcHub** — process singleton. `MainActivity` binds it in `onResume` and releases in `onPause`,
  because the platform only grants reader mode to the resumed activity. Exposes `availability`
  (unsupported / disabled / ready) and an `events` flow.
- **TagIo** — UID hex formatting and the NDEF write, including formatting blank tags.
- **NfcEffects** — `OnNfcScan` / `OnNfcEvent`, scoped to the nav entry's STARTED lifecycle so only
  the visible screen reacts to a tap.

Two behaviours worth knowing before changing anything here:

- **Platform sounds are off.** The chime fires on read, before the server rules, so it would signal
  "served" on a swipe about to be refused. `ScanTarget` replaces it with haptics after the verdict.
- **Same-UID taps inside 2.5s are dropped.** On a payment surface a duplicate is a double charge.
  Deliberate writes bypass this window.

Manual UID entry is retained everywhere as a fallback for damaged badges and reader-less devices.

Per screen: meal and access scanners submit on tap; the wallet fills the UID and looks up the holder
but never charges on tap; enrolment reads the UID and can optionally encode the badge.

## Known gaps

- **Badge encoding is cosmetic, not a credential.** The backend authenticates on the hardware UID
  (`Card.uid`), so the NDEF payload written during enrolment only labels a badge — it is not
  checked, not signed, and trivially cloneable. Offline or tamper-resistant verification would need
  signed payloads or a secure element, neither of which exists yet.
- **Session is in-memory.** A cold start returns to login. Persisting it needs an encrypted store,
  not plain DataStore — it holds a bearer token.
- **`ui/navigation/Destinations.kt` is dead code** superseded by `Screen.kt`, and safe to delete.
- **Backend blockers** (server-side, not client): several read endpoints are backed by an in-memory
  `STORE` while writes go to SQLite. See the repo-level notes — hall access scanning and team
  broadcasts cannot succeed for DB-created users, and dashboard/analytics/reports report zeros.

## Backend Connectivity

The application targets the live Cloudflare Workers edge backend at:
`https://mianu-backend.karimshacker1234.workers.dev/api/v1/`

Database: Cloudflare D1 distributed edge database (`mianu-org-db`).
Authentication: Full cryptographic PBKDF2 authentication with provisioned conference accounts.
Default Admin: `admin@mianu.org` / `Admin@2026`
Default Floor Organizer: `ops@mianu.org` / `Organizer@2026`
