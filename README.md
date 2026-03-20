# FSync

An Android app for syncing folders between your device and remote storage over SMB (Windows shares) or WebDAV.

## Features

- **SMB and WebDAV** remote storage support
- **7 sync strategies**: upload-only, download-only, mirror, two-way, and delete-after-transfer variants
- **Background sync** via WorkManager with per-folder unique job scheduling
- **Activity log** tracking files transferred, bytes moved, and sync status per run

## Build

```bash
./gradlew assembleDebug
```

Requires Android Studio with SDK 36 and JDK 11+. Min SDK 26.
