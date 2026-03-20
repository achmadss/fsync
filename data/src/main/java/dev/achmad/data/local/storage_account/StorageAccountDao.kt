package dev.achmad.data.local.storage_account

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageAccountDao {

    @Query("SELECT * FROM storage_accounts ORDER BY name ASC")
    fun subscribeAll(): Flow<List<StorageAccountEntity>>

    @Query("SELECT * FROM storage_accounts WHERE id = :id")
    fun subscribeById(id: Long): Flow<StorageAccountEntity?>

    @Query("SELECT * FROM storage_accounts ORDER BY name ASC")
    suspend fun getAll(): List<StorageAccountEntity>

    @Query("SELECT * FROM storage_accounts WHERE id = :id")
    suspend fun getById(id: Long): StorageAccountEntity?

    @Upsert
    suspend fun upsert(entity: StorageAccountEntity): Long

    @Query("DELETE FROM storage_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
