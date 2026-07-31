# PetAdopt - Agent Guide

## Stack
- **Kotlin 2.1.20**, Android Gradle Plugin from `gradle/libs.versions.toml`, compileSdk 34, minSdk 24
- **Jetpack Compose** with Material 3, **Navigation Compose**, **Coil** for images
- **Supabase** via `supabase-kt`: Auth, PostgREST, Storage
- **Ktor** with OkHttp for Supabase/GigaChat HTTP clients
- **AWS SDK for Kotlin** plus custom S3 signing/storage helpers
- **GigaChat** integration for questionnaire risk assessment
- **Hilt 2.51.1** with kapt: `@HiltViewModel`, `@Inject`, `@AndroidEntryPoint`
- Separate **React/Vite/TypeScript** web admin panel under `web-panel/`

## Build commands
```bash
./gradlew assembleDebug             # full Android build
./gradlew :app:assembleDebug        # Android app module
./gradlew testDebugUnitTest         # Android unit tests

cd web-panel
npm run build                       # web panel build
npm run dev                         # web panel dev server
```
All Android dependency versions are centralized in `gradle/libs.versions.toml`; use `alias(libs.*)` in Gradle build files.

## Architecture
- Android app is a single Gradle module: `:app`
- Entry point: `PetAdoptApp` + `MainActivity` -> `navigation/NavGraph.kt`
- Main layers: `data.model.*` -> `data.repository.*` -> `domain.usecase.*` -> `viewmodel.*` -> `ui.screens.*`
- DI is in `di/RepositoryModule.kt`; `di/AppModule.kt` is currently a placeholder
- Supabase client is centralized in `util/SupabaseConfig.kt`
- S3 config/signing helpers live in `util/S3Config.kt` and `util/S3SigV4Signer.kt`
- Web panel uses Supabase JS from `web-panel/src/lib/supabase.ts`
- SQL schema/RLS/migration helpers live in root `*.sql` files and `web-panel/*.sql`

## Main Navigation Routes
- User flow: `auth`, `loading`, `onboarding`, `questionnaire`, `swipe`, `details/{petId}`, `application/{petId}/{petName}`, `matches`, `account`, `edit_profile`, `applications`
- Shelter/admin flow: `shelter`, `admin/addPet`, `admin/editPet/{petId}`, `admin/applications/{petId}/{petName}`, `admin/application/detail/{applicationId}`
- Chat flow: `application_chat/{applicationId}`, `chat/{applicationId}`

## Key Patterns
- **Repositories**: expose suspend functions and hide Supabase/S3/GigaChat implementation details behind repository interfaces where interfaces already exist.
- **ViewModels**: use `@HiltViewModel`, inject use cases/repositories via `@Inject constructor`, launch work in `viewModelScope`, expose UI state with `StateFlow`.
- **Use cases**: keep app actions under `domain/usecase/`; prefer existing use-case wrappers when wiring ViewModels.
- **Screens**: Compose screens receive `hiltViewModel()` as a default parameter and navigation callbacks from `NavGraph`.
- **UI wrappers**: use existing `Screen { }`, `PrimaryButton`, `SwipeCard`, `PetCard`, and risk/chat components instead of introducing parallel styles.
- **Navigation**: pass stable IDs through routes and load data from repositories/ViewModels. Do not pass long free-text fields directly in the path.

## UI Patterns
- `Screen(content: ColumnScope.() -> Unit)` provides the common full-size background column with padding.
- `PrimaryButton` is the themed full-width primary action.
- `SwipeCard` wraps drag gestures and triggers left/right swipes at the configured threshold.
- `PetCard` renders pet imagery, gradient overlay, actions, and tags.
- Keep Russian user-facing labels consistent with existing screens.

## Configuration And Secrets
- `.env` is loaded by `app/build.gradle.kts` into `BuildConfig` for S3, GigaChat, and Supabase fields.
- Do not add real secrets to source files.
- Prefer `BuildConfig` config values over hardcoded URLs/keys.
- Treat `app/google-services.json`, `.env`, Supabase anon keys, S3 keys, and GigaChat credentials as sensitive.

## Known Quirks / Risks
- The project previously used Firebase-oriented docs, but the current code is Supabase-based.
- `SupabaseConfig.kt` reads Supabase URL/key from `BuildConfig`; keep real values in `.env`, not source files.
- `AndroidManifest.xml` currently allows cleartext traffic; avoid expanding HTTP usage.
- `GigaChatRepository.kt` uses the default OkHttp TLS checks. If custom certificates are needed, add a scoped network security config instead of disabling verification.
- Some files contain mojibake/broken Russian text from encoding issues.
- Startup navigation is centralized through `loading` and `NavViewModel`; `NavViewModel` still uses retry delays while waiting for session/profile data.
- Application detail navigation passes only `applicationId`; keep this pattern for new detail routes.
- `proguard-rules.pro` is empty while release minification is enabled.
- Tests are mostly boilerplate.

## Tests
- Existing tests: `app/src/test/.../ExampleUnitTest.kt` and `app/src/androidTest/.../ExampleInstrumentedTest.kt`
- Add JVM unit tests under `app/src/test/`
- Add instrumented Android tests under `app/src/androidTest/`
- For risky repository/use-case changes, add focused tests around mapping, validation, and error handling where practical.

## Working Tree Safety
- The repo may contain user changes. Check `git status --short` before edits and do not revert unrelated files.
- Keep changes scoped to the requested area.
