package dev.achmad.domain.storage_account

import kotlinx.coroutines.flow.Flow

interface StorageAccountRepository {

    fun subscribeAll(): Flow<List<StorageAccount>>

    fun subscribeById(id: Long): Flow<StorageAccount?>

    suspend fun getAll(): List<StorageAccount>

    suspend fun getById(id: Long): StorageAccount?

    suspend fun save(account: StorageAccount): Result<StorageAccount>

    suspend fun delete(id: Long): Result<Unit>

    suspend fun hasAssociatedFolderPairs(id: Long): Boolean
}
