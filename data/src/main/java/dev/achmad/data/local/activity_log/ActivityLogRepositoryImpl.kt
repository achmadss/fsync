package dev.achmad.data.local.activity_log

import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.concurrent.TimeUnit

class ActivityLogRepositoryImpl(
    private val dao: ActivityLogDao,
) : ActivityLogRepository {

    override fun subscribeRecent(limit: Int): Flow<List<ActivityLog>> =
        dao.subscribeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun subscribeByFolder(folderId: Long): Flow<List<ActivityLog>> =
        dao.subscribeByFolder(folderId).map { list -> list.map { it.toDomain() } }

    override fun subscribeRunning(folderId: Long): Flow<ActivityLog?> =
        dao.subscribeRunning(folderId).map { it?.toDomain() }

    override suspend fun getById(id: Long): ActivityLog? =
        dao.getById(id)?.toDomain()

    override suspend fun getOrphanedRunning(): List<ActivityLog> =
        dao.getOrphanedRunning().map { it.toDomain() }

    override suspend fun create(log: ActivityLog): Result<ActivityLog> = runCatching {
        val entity = log.toEntity()
        val newId = dao.insert(entity)
        log.copy(id = newId)
    }

    override suspend fun complete(log: ActivityLog): Result<Unit> = runCatching {
        dao.update(log.toEntity())
    }

    override suspend fun pruneOlderThan(days: Int): Result<Int> = runCatching {
        val cutoff = Instant.now().minusMillis(TimeUnit.DAYS.toMillis(days.toLong()))
        dao.deleteOlderThan(cutoff.toEpochMilli())
    }

    override suspend fun deleteByFolder(folderId: Long): Result<Unit> = runCatching {
        dao.deleteByFolder(folderId)
    }
}
