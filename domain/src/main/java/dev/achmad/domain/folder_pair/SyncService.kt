package dev.achmad.domain.folder_pair

import dev.achmad.domain.activity_log.ActivityLog

interface SyncService {
    suspend fun sync(folderId: Long): Result<ActivityLog>
}
