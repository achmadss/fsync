package dev.achmad.data.local.storage_account

import dev.achmad.domain.storage_account.StorageAccount
import dev.achmad.domain.storage_account.StorageAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StorageAccountRepositoryImpl(
    private val dao: StorageAccountDao,
) : StorageAccountRepository {

    override fun subscribeAll(): Flow<List<StorageAccount>> =
        dao.subscribeAll().map { list -> list.map { it.toDomain() } }

    override fun subscribeById(id: Long): Flow<StorageAccount?> =
        dao.subscribeById(id).map { it?.toDomain() }

    override suspend fun getAll(): List<StorageAccount> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): StorageAccount? =
        dao.getById(id)?.toDomain()

    override suspend fun save(account: StorageAccount): Result<StorageAccount> = runCatching {
        val entity = account.toEntity()
        val newId = dao.upsert(entity)
        val savedId = if (account.id == 0L) newId else account.id
        when (account) {
            is StorageAccount.Smb    -> account.copy(id = savedId)
            is StorageAccount.WebDav -> account.copy(id = savedId)
        }
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        dao.deleteById(id)
    }

    override suspend fun hasAssociatedFolderPairs(id: Long): Boolean {
        // Delegated to FolderPairRepository via use case; data layer can also query directly if needed
        return false
    }
}
