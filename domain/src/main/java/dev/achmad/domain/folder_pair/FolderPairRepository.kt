package dev.achmad.domain.folder_pair

import kotlinx.coroutines.flow.Flow

interface FolderPairRepository {

    fun subscribeAll(): Flow<List<FolderPair>>

    fun subscribeEnabled(): Flow<List<FolderPair>>

    fun subscribeByStorageAccount(storageAccountId: Long): Flow<List<FolderPair>>

    suspend fun getById(id: Long): FolderPair?

    suspend fun getEnabled(): List<FolderPair>

    suspend fun save(folderPair: FolderPair): Result<FolderPair>

    suspend fun setEnabled(id: Long, enabled: Boolean): Result<Unit>

    suspend fun updateLastSyncAt(id: Long): Result<Unit>

    suspend fun delete(id: Long): Result<Unit>

    suspend fun deleteByStorageAccount(storageAccountId: Long): Result<Unit>
}
