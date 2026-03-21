package dev.achmad.domain.folder_pair.usecase

import dev.achmad.domain.folder_pair.FolderPair
import dev.achmad.domain.folder_pair.FolderPairRepository
import kotlinx.coroutines.flow.Flow

class GetFolderPairsUseCase(
    private val repository: FolderPairRepository
) {
    operator fun invoke(): Flow<List<FolderPair>> = repository.subscribeAll()
}
