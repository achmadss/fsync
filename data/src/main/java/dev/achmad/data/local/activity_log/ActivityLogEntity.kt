package dev.achmad.data.local.activity_log

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityStatus
import java.time.Instant

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val folderId: Long,
    val startedAt: Long,      // epoch millis
    val finishedAt: Long?,    // epoch millis, nullable
    val status: String,       // ActivityStatus.name
    val filesUploaded: Int,
    val filesDownloaded: Int,
    val filesDeleted: Int,
    val bytesTransferred: Long,
    val errorMessage: String?,
) {
    fun toDomain() = ActivityLog(
        id = id,
        folderId = folderId,
        startedAt = Instant.ofEpochMilli(startedAt),
        finishedAt = finishedAt?.let { Instant.ofEpochMilli(it) },
        status = ActivityStatus.valueOf(status),
        filesUploaded = filesUploaded,
        filesDownloaded = filesDownloaded,
        filesDeleted = filesDeleted,
        bytesTransferred = bytesTransferred,
        errorMessage = errorMessage,
    )
}

fun ActivityLog.toEntity() = ActivityLogEntity(
    id = id,
    folderId = folderId,
    startedAt = startedAt.toEpochMilli(),
    finishedAt = finishedAt?.toEpochMilli(),
    status = status.name,
    filesUploaded = filesUploaded,
    filesDownloaded = filesDownloaded,
    filesDeleted = filesDeleted,
    bytesTransferred = bytesTransferred,
    errorMessage = errorMessage,
)
