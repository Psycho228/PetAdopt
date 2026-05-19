# PetAdopt — Agent Guide

## Stack
- **Kotlin 1.9.24**, AGP 8.13.2, Gradle 8.13, compileSdk 34, minSdk 24
- **Jetpack Compose** (Material 3), **Navigation Compose**, **Coil** for images
- **Firebase Auth + Firestore** (BOM 33.1.2) with **kotlinx-coroutines-play-services**
- **Hilt 2.51.1** (kapt) — `@HiltViewModel`, `@Inject`, `@AndroidEntryPoint`

## Build commands
```bash
./gradlew assembleDebug             # full build
./gradlew :app:assembleDebug        # single module
./gradlew testDebugUnitTest         # unit tests (only boilerplate exists)
```
All versions centralized in `gradle/libs.versions.toml` — use `alias(libs.*)` in build files.

## Architecture
- **Single module** (`:app`), entry point: `MainActivity` → `NavGraph` (string routes)
- **MVVM**: `data.model.*` → `data.repository.*` → `viewmodel.*` → `ui.screens.*`
- **DI**: `di/AppModule.kt` provides `FirebaseAuth` + `FirebaseFirestore` singletons
- **Navigation routes**: `auth`, `loading`, `onboarding`, `questionnaire`, `swipe`, `details`, `application`, `matches`, `account`

## Key patterns (DO NOT deviate)
- **Repositories**: inject `FirebaseAuth`/`FirebaseFirestore` via constructor, all public methods are `suspend` using `.await()` from `kotlinx.coroutines.tasks`
- **ViewModels**: `@HiltViewModel`, inject repositories via `@Inject constructor`, use `viewModelScope.launch` + try/catch, expose `StateFlow`
- **Screens**: receive `hiltViewModel()` as default param, accept callbacks for navigation. Use `Screen { }` wrapper for full-size background column layout, `PrimaryButton` for full-width themed buttons.
- **Navigation**: add `onAccount: () -> Unit` callback + "Профиль" button on every screen except auth.

## UI patterns
- `Screen(content: ColumnScope.() -> Unit)` — full-size column with background + 16dp padding
- `PrimaryButton` — full-width, custom `Primary` color, white text
- `SwipeCard` — drag-gesture wrapper with spring animation, calls `onSwipedLeft`/`onSwipedRight` at 320px threshold
- `PetCard` — card with image gradient, like/dislike overlay, tags

## Known quirks
- `ui/state/MatchState.kt` is an externalized state holder (non-standard), not used in main flow
- Loading screen has no timeout — infinite spinner if Firebase hangs
- Pet placeholder URL: `"https://via.placeholder.com/300"` when `imageUrl` is empty
- `proguard-rules.pro` exists but is empty — R8 not configured
- `UserProfile` in `data.repository` package is legacy (questionnaire uses `QuestionnaireAnswer` in `data.model`)
- No GitHub Actions CI, no README

## Tests
- Only boilerplate `ExampleUnitTest` + `ExampleInstrumentedTest`
- Add unit tests under `src/test/`, instrumented under `src/androidTest/`
