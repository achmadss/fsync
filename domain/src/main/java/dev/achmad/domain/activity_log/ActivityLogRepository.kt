package dev.achmad.domain.activity_log

import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {

    fun subscribeRecent(limit: Int = 50): Flow<List<ActivityLog>>

    fun subscribeByFolder(folderId: Long): Flow<List<ActivityLog>>

    fun subscribeRunning(folderId: Long): Flow<ActivityLog?>

    suspend fun getById(id: Long): ActivityLog?

    /** Returns all entries still in [ActivityStatus.RUNNING] state. Used on startup to detect orphaned logs from a previous crash. */
    suspend fun getOrphanedRunning(): List<ActivityLog>

    /** Creates a new log entry at the start of a sync job. */
    suspend fun create(log: ActivityLog): Result<ActivityLog>

    /** Updates an existing log entry on sync completion, failure, or cancellation. */
    suspend fun complete(log: ActivityLog): Result<Unit>

    /** Deletes logs older than [days] days. Returns the number of rows deleted. */
    suspend fun pruneOlderThan(days: Int): Result<Int>

    suspend fun deleteByFolder(folderId: Long): Result<Unit>
}
