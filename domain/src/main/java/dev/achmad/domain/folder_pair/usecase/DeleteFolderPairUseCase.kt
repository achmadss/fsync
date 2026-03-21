package dev.achmad.domain.folder_pair.usecase

import dev.achmad.domain.folder_pair.FolderPairRepository

class DeleteFolderPairUseCase(
    private val repository: FolderPairRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
