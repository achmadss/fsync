package dev.achmad.domain.folder_pair.usecase

import dev.achmad.domain.folder_pair.FolderPairRepository

class SetFolderPairEnabledUseCase(
    private val repository: FolderPairRepository
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) = repository.setEnabled(id, enabled)
}
