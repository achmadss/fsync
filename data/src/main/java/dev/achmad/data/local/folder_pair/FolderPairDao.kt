package dev.achmad.data.local.folder_pair

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderPairDao {

    @Query("SELECT * FROM folder_pairs ORDER BY name ASC")
    fun subscribeAll(): Flow<List<FolderPairEntity>>

    @Query("SELECT * FROM folder_pairs WHERE isEnabled = 1 ORDER BY name ASC")
    fun subscribeEnabled(): Flow<List<FolderPairEntity>>

    @Query("SELECT * FROM folder_pairs WHERE storageAccountId = :storageAccountId ORDER BY name ASC")
    fun subscribeByStorageAccount(storageAccountId: Long): Flow<List<FolderPairEntity>>

    @Query("SELECT * FROM folder_pairs WHERE id = :id")
    suspend fun getById(id: Long): FolderPairEntity?

    @Query("SELECT * FROM folder_pairs WHERE isEnabled = 1")
    suspend fun getEnabled(): List<FolderPairEntity>

    @Upsert
    suspend fun upsert(entity: FolderPairEntity): Long

    @Query("UPDATE folder_pairs SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE folder_pairs SET lastSyncAt = :epochMillis WHERE id = :id")
    suspend fun updateLastSyncAt(id: Long, epochMillis: Long)

    @Query("DELETE FROM folder_pairs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM folder_pairs WHERE storageAccountId = :storageAccountId")
    suspend fun deleteByStorageAccount(storageAccountId: Long)
}
