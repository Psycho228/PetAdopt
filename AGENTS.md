# PetAdopt - Agent Guide

## Product

The user-facing brand is **Hvostiki** (Russian: `Хвостики`). The repository
contains an Android adoption app plus a React web panel for shelters and
administrators. The Android app supports guests, adopters, shelters, breeders,
and administrators. Keep Russian UI copy UTF-8 and consistent with the brand.

## Stack

- Kotlin 2.1.20, AGP 8.5.2, JDK 17, compile/target SDK 34, min SDK 24
- Jetpack Compose, Material 3, Navigation Compose, Coil, Coroutines/StateFlow
- Hilt 2.51.1 with kapt
- Supabase Auth, PostgREST, Storage, and Supabase JS in the web panel
- Ktor/OkHttp, Kotlin serialization, AWS SDK plus custom SigV4 S3 helpers
- GigaChat for questionnaire risk assessment
- React 18, TypeScript, Vite 5, Tailwind CSS, Lucide, Recharts under `web-panel/`

Versions belong in `gradle/libs.versions.toml`; use `alias(libs.*)` in Gradle.

## Commands

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedAndroidTest

cd web-panel
npm.cmd run build
npm.cmd run dev
```

On this Windows setup, prefer `npm.cmd` because PowerShell may block `npm.ps1`.
The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Vite output is
`web-panel/dist/`.

## Architecture

- Android is one module, `:app`; entry is `PetAdoptApp` / `MainActivity`.
- Navigation is centralized in `navigation/NavGraph.kt` and startup routing in
  `viewmodel/NavViewModel.kt`.
- Main flow: `data.model` -> `data.repository` -> `domain.usecase` ->
  `viewmodel` -> `ui.screens`.
- Hilt bindings live in `di/RepositoryModule.kt`.
- Supabase is centralized in `util/SupabaseConfig.kt`.
- S3 upload/signing is in `data/repository/S3StorageRepository.kt`,
  `util/S3Config.kt`, and `util/S3SigV4Signer.kt`.
- The active `StorageRepository` binding is `S3StorageRepository`.
  `SupabaseStorageRepository` remains an alternate implementation.
- SQL schema, triggers, and RLS helpers are root `*.sql` files. Breeder market
  contracts are in `add_breeder_marketplace.sql`.

## Roles And Routing

- Guests start at `swipe` and may browse pet details and `marketplace`.
- Sending an adoption application requires auth. New accounts continue through
  `questionnaire_after_registration` before returning to the application flow.
- Shelter/admin accounts start at `shelter`.
- Breeders start at `breeder_cabinet` and use `breeder_listing/{listingId}`.
- A pending or rejected breeder may still have `users.role = user`.
  `NavViewModel` intentionally treats an existing own `breeder_profiles` row as
  a breeder account before verification.
- Main marketplace routes are `marketplace`, `marketplace/{listingId}`, and
  `breeder_cabinet`.
- Application detail and chat routes pass stable IDs only. The adoption form
  still has the legacy `application/{petId}/{petName}` route; do not add more
  free text to it, and prefer loading names from a repository when revisiting
  that flow. Never put messages or contact-time text into route paths.

## Breeder Marketplace

- `breeder_profiles.verification_status`: `pending`, `verified`, `rejected`.
- `sale_listings.status`: `draft`, `pending`, `available`, `reserved`, `sold`,
  `rejected`, `archived`.
- The client creates a profile as `pending`; it must not directly assign the
  protected `breeder` role. Database triggers assign the role after approval.
- Admin moderation is implemented in `web-panel/src/pages/BreederModerationPage.tsx`
  at `/breeders` and must remain restricted to role `admin`.
- Listing main photo is `photo_url`; the remaining gallery is
  `additional_photos`. The Android breeder form allows at most six photos.
- Listing details use `HorizontalPager`; keep pager, counter, and thumbnails in
  sync when changing gallery behavior.

## Image Pipeline

All shelter and breeder uploads go through `StorageRepository` use cases.
`util/ImageUploadProcessor.kt` reads EXIF orientation, applies rotation/mirroring,
limits the longest side to 1024 px, and emits JPEG before upload. Do not bypass
this processor with direct `BitmapFactory` compression: that recreates sideways
phone photos. Existing incorrectly uploaded files cannot be repaired reliably
without selecting the source image again.

## UI Patterns

- Reuse `PrimaryButton`, `PetCard`, `SwipeCard`, `Screen`, existing theme colors,
  and established section components.
- Use icons for familiar actions, with content descriptions and stable button
  dimensions. Use Material/Lucide icons instead of custom SVGs.
- Keep compact controls readable on narrow phones; avoid fixed-width text chips
  and nested cards.
- `PetCard` and detail galleries use `ContentScale.Crop`. Fix source orientation
  in the upload pipeline, not with per-screen rotation guesses.
- Compose screens take navigation callbacks and default Hilt ViewModels.

## Configuration And Deployment

- Root `.env` is loaded by `app/build.gradle.kts` into `BuildConfig` for
  Supabase, S3, and GigaChat. Despite the stale comment in `.env.example`, the
  Gradle build reads `.env`, not `.env.local`.
- Web config is `web-panel/.env` with `VITE_*` variables. These values are
  embedded at build time. Rebuild and redeploy after rotating the Supabase anon
  key.
- Never commit real `.env`, Supabase keys, S3 credentials, GigaChat credentials,
  signing keys, or `local.properties`.
- Current web root is `/var/www/petadopt` for `hvostiki-admpan.online`. Deploy the
  contents of `web-panel/dist/` over SFTP port 22, not FTP port 21.
- The web chat uses optimistic append plus five-second polling when Realtime is
  unavailable. Android chat also polls while the screen is visible.

## Tests And Verification

- JVM tests are under `app/src/test/`; current focused tests cover GigaChat risk
  parsing and Russian mojibake repair in addition to boilerplate examples.
- Instrumented tests are under `app/src/androidTest/`.
- Scale tests with risk. Add focused JVM tests for parsers, mappings, validation,
  and pure helpers where practical.
- After Android changes, run at least `:app:assembleDebug`. After web changes,
  run `npm.cmd run build`.

## Known Risks

- Some source text and legacy database values still contain mojibake. Prefer
  fixing the source/ingestion path; use `util/RussianText.kt` only for legacy
  data that cannot be migrated immediately.
- `NavViewModel` still retries while Supabase restores session/profile state.
- Supabase Realtime on the self-hosted server may be unhealthy; polling fallback
  is intentional until the proxy/service is fixed.
- Android currently permits cleartext traffic. Do not add new HTTP endpoints;
  migrate existing URLs to HTTPS before disabling it.
- Client-side S3 and GigaChat credentials remain a production security risk.
  Prefer a server-side proxy for future integration work.
- Release minification is enabled while `proguard-rules.pro` is effectively
  empty. Verify release builds before distribution.
- Automated coverage is still sparse.

## Working Tree Safety

The worktree may contain user changes. Run `git status --short` before edits,
do not revert unrelated files, and keep changes scoped to the request. Use
`apply_patch` for manual edits. Do not commit generated APKs, Vite `dist/`, IDE
state, or secrets.
