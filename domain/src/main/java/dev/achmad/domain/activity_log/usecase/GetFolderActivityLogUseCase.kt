package dev.achmad.domain.activity_log.usecase

import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFolderActivityLogUseCase(
    private val repository: ActivityLogRepository
) {
    operator fun invoke(folderId: Long): Flow<ActivityLog?> =
        repository.subscribeByFolder(folderId).map { it.firstOrNull() }
}
