# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug              # Build debug APK
./gradlew build                      # Build all modules
```

## Architecture

This is a **Clean Architecture** multi-module Android project with 4 modules:

- **`:app`** — Presentation layer: Jetpack Compose UI, Voyager screens, single `MainActivity`
- **`:core`** — Shared infrastructure: OkHttp networking, `PreferenceStore`, Koin DI setup
- **`:domain`** — Business logic layer: use cases and repository interfaces (currently a scaffold)
- **`:data`** — Data layer: repository implementations, API clients (currently a scaffold)

Module dependency direction: `app` → `core`, `domain`, `data`; `data` → `core`, `domain`

### Key Patterns

**Navigation:** Voyager (`cafe.adriel.voyager`) — screen-based, no Fragments. Screens implement `cafe.adriel.voyager.core.screen.Screen`. State lives in `ScreenModel` (Voyager's ViewModel equivalent).

**DI:** Koin — initialized in `MainApplication`, with one Koin module per layer (`CoreModule`, `DomainModule`, `DataModule`). Add new bindings in the respective `di/` directory.

**Network:** `NetworkHelper` in `:core` configures the OkHttp client. Use `OkHttpExtensions.kt` suspend helpers (`await()`, `awaitSuccess()`, `parseAs<T>()`) for async calls. JSON is handled via Kotlinx Serialization.

**Preferences:** Type-safe access through `PreferenceStore` → `Preference<T>`. Preferences expose `Flow<T>` / `StateFlow<T>` for reactive observation. See `AndroidPreferenceStore` for the SharedPreferences implementation.

**UI strings/images:** Use `UiText` (sealed class) and `UiImage` to decouple resources from Composables.

### Stack

- Kotlin 2.3.20, AGP 9.0.1, Compose BOM 2026.03.00, Material3
- Voyager 1.1.0-beta03 (navigation + ScreenModel)
- Koin BOM 4.2.0
- OkHttp 5.3.2 + Kotlinx Serialization 1.10.0
- Min SDK 26, Compile/Target SDK 36, Java 11
- All dependency versions managed in `gradle/libs.versions.toml`