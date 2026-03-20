package dev.achmad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.achmad.data.local.activity_log.ActivityLogDao
import dev.achmad.data.local.activity_log.ActivityLogEntity
import dev.achmad.data.local.folder_pair.FolderPairDao
import dev.achmad.data.local.folder_pair.FolderPairEntity
import dev.achmad.data.local.storage_account.StorageAccountDao
import dev.achmad.data.local.storage_account.StorageAccountEntity

@Database(
    entities = [
        StorageAccountEntity::class,
        FolderPairEntity::class,
        ActivityLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storageAccountDao(): StorageAccountDao
    abstract fun folderPairDao(): FolderPairDao
    abstract fun activityLogDao(): ActivityLogDao
}
