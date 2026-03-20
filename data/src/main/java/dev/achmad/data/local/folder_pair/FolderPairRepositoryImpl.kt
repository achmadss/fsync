package dev.achmad.data.local.folder_pair

import dev.achmad.domain.folder_pair.FolderPair
import dev.achmad.domain.folder_pair.FolderPairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class FolderPairRepositoryImpl(
    private val dao: FolderPairDao,
) : FolderPairRepository {

    override fun subscribeAll(): Flow<List<FolderPair>> =
        dao.subscribeAll().map { list -> list.map { it.toDomain() } }

    override fun subscribeEnabled(): Flow<List<FolderPair>> =
        dao.subscribeEnabled().map { list -> list.map { it.toDomain() } }

    override fun subscribeByStorageAccount(storageAccountId: Long): Flow<List<FolderPair>> =
        dao.subscribeByStorageAccount(storageAccountId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): FolderPair? =
        dao.getById(id)?.toDomain()

    override suspend fun getEnabled(): List<FolderPair> =
        dao.getEnabled().map { it.toDomain() }

    override suspend fun save(folderPair: FolderPair): Result<FolderPair> = runCatching {
        val entity = folderPair.toEntity()
        val newId = dao.upsert(entity)
        val savedId = if (folderPair.id == 0L) newId else folderPair.id
        folderPair.copy(id = savedId)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean): Result<Unit> = runCatching {
        dao.setEnabled(id, enabled)
    }

    override suspend fun updateLastSyncAt(id: Long): Result<Unit> = runCatching {
        dao.updateLastSyncAt(id, Instant.now().toEpochMilli())
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        dao.deleteById(id)
    }

    override suspend fun deleteByStorageAccount(storageAccountId: Long): Result<Unit> = runCatching {
        dao.deleteByStorageAccount(storageAccountId)
    }
}
