# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug              # Build debug APK
./gradlew build                      # Build all modules
./gradlew lint                       # Run lint checks
./gradlew lintFix                    # Run lint and auto-apply safe fixes
./gradlew testDebugUnitTest          # Run unit tests (debug variant)
./gradlew connectedDebugAndroidTest  # Run instrumentation tests on device
```

## Architecture

Clean Architecture multi-module Android project:

- **`:app`** — Presentation layer: Jetpack Compose UI, Voyager screens, single `MainActivity`
- **`:core`** — Shared infrastructure: OkHttp networking, `PreferenceStore`, Koin DI helpers
- **`:domain`** — Business logic: use cases, repository interfaces, domain models
- **`:data`** — Data layer: Room database, SMB/WebDAV remote clients, repository implementations

Module dependency direction: `app` → `core`, `domain`, `data`; `data` → `core`, `domain`

### Stack

- Kotlin 2.3.20, AGP 9.0.1, Compose BOM 2026.03.00, Material3
- Voyager 1.1.0-beta03 (navigation + ScreenModel)
- Koin BOM 4.2.0
- OkHttp 5.3.2 + Kotlinx Serialization 1.10.0
- Room 2.8.4 (local persistence)
- WorkManager 2.11.1 (background sync)
- jcifs-ng 2.1.10 (SMB), sardine-android 0.9 (WebDAV)
- Min SDK 26, Compile/Target SDK 36, Java 11
- All versions in `gradle/libs.versions.toml`; JitPack added to repositories for sardine-android

### Key Patterns

**Navigation:** Voyager — screen-based, no Fragments. Screens are `object` singletons implementing `Screen`. State lives in `ScreenModel`. Home uses `TabNavigator` with four tab singletons (`FoldersTab`, `ActivityTab`, `StorageTab`, `MoreTab`); each tab is an `object : Tab` with a `@Composable` `options` getter that switches icon painter based on `LocalTabNavigator.current` selection state.

**DI:** Koin — initialized in `MainApplication`, one module per layer (`coreModule`, `domainModule`, `dataModule`). Use `injectLazy<T>()` from `KoinUtil.kt` for lazy property injection outside of Composables (e.g. in Workers). `injectApplicationContext()` resolves `Context` without a direct reference.

**Network:** `NetworkHelper` in `:core` configures OkHttp. Chucker HTTP inspector is attached in debug builds. Use `OkHttpExtensions.kt` suspend helpers (`await()`, `awaitSuccess()`, `parseAs<T>()`) for async calls. JSON via Kotlinx Serialization.

**Preferences:** `PreferenceStore` → `Preference<T>` with `Flow<T>` / `StateFlow<T>`. In Composables, call `preference.collectAsState()` from `PreferenceUtil.kt`. The preference UI system uses a sealed `Preference` hierarchy rendered by `PreferenceScreen`; supports switch, list, multi-select, edit-text, permission, alert dialog, and custom widget types.

**UI strings/images:** `UiText` (sealed class with `Resource` / `Raw` variants) and `UiImage` decouple Android resources from Composables.

### Domain Layer

Three aggregate roots with repository interfaces in `:domain`:

- `StorageAccount` — sealed class (`Smb`, `WebDav`) with connection credentials
- `FolderPair` — maps a local directory to a remote path, holds `SyncStrategy` and enabled flag
- `ActivityLog` — records one sync run: status (`RUNNING`, `SUCCESS`, `FAILURE`, `CANCELLED`), file counters, bytes transferred, timestamps

`SyncFolderUseCase` is a thin invoke-operator wrapper around `SyncService.sync(folderId): Result<ActivityLog>`.

### Data Layer

**Room database** (`AppDatabase`, version 1): three entities mapping 1-to-1 to domain models. Each entity has `toDomain()` and a companion `toEntity()` for conversion. `StorageAccountEntity` uses a `type` string discriminator to store both SMB and WebDAV accounts in one table.

**Remote storage:** `RemoteStorageClient` interface provides `listFiles`, `uploadFile`, `downloadFile`, `deleteFile`, `createDirectory`, `exists`. `StorageClientFactory.create(account)` returns `SmbStorageClient` or `WebDavStorageClient` based on account type. Both clients write to a `.tmp` file then rename atomically; all I/O runs on `Dispatchers.IO`.

**SyncEngine** implements `SyncService` and contains the full sync logic:
1. Load `FolderPair` and `StorageAccount`, create remote client
2. Insert an `ActivityLog` with `RUNNING` status
3. Scan local files; list remote files
4. Apply one of 7 strategies (UPLOAD_ONLY, UPLOAD_THEN_DELETE, UPLOAD_MIRROR, DOWNLOAD_ONLY, DOWNLOAD_THEN_DELETE, DOWNLOAD_MIRROR, TWO_WAY)
5. Update log with counters; mark SUCCESS, FAILURE, or CANCELLED on exception

TWO_WAY conflict resolution: newest timestamp wins; equal timestamps with size difference → no-op.

### Background Sync

`SyncWorker` is a `CoroutineWorker` that calls `SyncFolderUseCase(folderId)`. Schedule a sync with `SyncWorker.dispatch(folderId)`, which enqueues a unique one-time work request tagged `"sync_$folderId"` using `ExistingWorkPolicy.KEEP` (prevents duplicate syncs for the same folder).

On app startup `MainApplication.onStartup()` scans for `ActivityLog` records left in `RUNNING` state and marks any whose WorkManager job is no longer active as `CANCELLED`. Check `workManager.isRunning(tag)` (from `WorkManagerUtil.kt`) before dispatching to avoid duplicate work.
