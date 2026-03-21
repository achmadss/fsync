package dev.achmad.domain.folder_pair.usecase

import dev.achmad.domain.folder_pair.SyncService

class SyncFolderUseCase(
    private val syncService: SyncService
) {
    suspend operator fun invoke(folderId: Long) = syncService.sync(folderId)
}
