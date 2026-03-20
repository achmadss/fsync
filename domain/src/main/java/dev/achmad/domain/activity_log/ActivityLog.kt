package dev.achmad.domain.activity_log

import java.time.Instant

data class ActivityLog(
    val id: Long = 0L,
    val folderId: Long,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val status: ActivityStatus,
    val filesUploaded: Int = 0,
    val filesDownloaded: Int = 0,
    val filesDeleted: Int = 0,
    val bytesTransferred: Long = 0L,
    val errorMessage: String? = null,
)

enum class ActivityStatus {
    /** A sync job is currently in progress. */
    RUNNING,

    /** The sync completed successfully. */
    SUCCESS,

    /** The sync terminated due to an error. See [ActivityLog.errorMessage]. */
    FAILURE,

    /** The sync was cancelled before completion. */
    CANCELLED,
}
