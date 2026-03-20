package dev.achmad.data.local.activity_log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_logs ORDER BY startedAt DESC LIMIT :limit")
    fun subscribeRecent(limit: Int): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE folderId = :folderId ORDER BY startedAt DESC")
    fun subscribeByFolder(folderId: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE folderId = :folderId AND status = 'RUNNING' LIMIT 1")
    fun subscribeRunning(folderId: Long): Flow<ActivityLogEntity?>

    @Query("SELECT * FROM activity_logs WHERE id = :id")
    suspend fun getById(id: Long): ActivityLogEntity?

    @Query("SELECT * FROM activity_logs WHERE status = 'RUNNING'")
    suspend fun getOrphanedRunning(): List<ActivityLogEntity>

    @Insert
    suspend fun insert(entity: ActivityLogEntity): Long

    @Update
    suspend fun update(entity: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE startedAt < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long): Int

    @Query("DELETE FROM activity_logs WHERE folderId = :folderId")
    suspend fun deleteByFolder(folderId: Long)
}
