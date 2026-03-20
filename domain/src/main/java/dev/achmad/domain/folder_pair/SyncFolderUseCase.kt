package dev.achmad.domain.folder_pair

class SyncFolderUseCase(private val syncService: SyncService) {
    suspend operator fun invoke(folderId: Long) = syncService.sync(folderId)
}
