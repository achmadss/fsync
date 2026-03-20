package dev.achmad.data.local.folder_pair

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.achmad.domain.folder_pair.FolderPair
import dev.achmad.domain.folder_pair.SyncStrategy
import java.time.Instant

@Entity(tableName = "folder_pairs")
data class FolderPairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val localPath: String,
    val remotePath: String,
    val storageAccountId: Long,
    val strategy: String,
    val isEnabled: Boolean,
    val createdAt: Long,      // epoch millis
    val lastSyncAt: Long?,    // epoch millis, nullable
) {
    fun toDomain() = FolderPair(
        id = id,
        name = name,
        localPath = localPath,
        remotePath = remotePath,
        storageAccountId = storageAccountId,
        strategy = SyncStrategy.valueOf(strategy),
        isEnabled = isEnabled,
        createdAt = Instant.ofEpochMilli(createdAt),
        lastSyncAt = lastSyncAt?.let { Instant.ofEpochMilli(it) },
    )
}

fun FolderPair.toEntity() = FolderPairEntity(
    id = id,
    name = name,
    localPath = localPath,
    remotePath = remotePath,
    storageAccountId = storageAccountId,
    strategy = strategy.name,
    isEnabled = isEnabled,
    createdAt = createdAt.toEpochMilli(),
    lastSyncAt = lastSyncAt?.toEpochMilli(),
)
