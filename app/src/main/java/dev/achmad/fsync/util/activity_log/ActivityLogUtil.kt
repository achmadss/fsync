package dev.achmad.fsync.util.activity_log

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityStatus
import java.time.Duration
import java.time.Instant

fun ActivityLog.durationSeconds(): Long {
    val end = finishedAt ?: Instant.now()
    return Duration.between(startedAt, end).seconds
}

data class ProgressState(
    val value: Float,
    val color: Color?,
    val isIndeterminate: Boolean,
)

@Composable
fun ActivityLog?.toProgressState(): ProgressState {
    return when (this?.status) {
        null -> ProgressState(0f, null, false)
        ActivityStatus.RUNNING -> ProgressState(0f, null, true)
        ActivityStatus.SUCCESS -> ProgressState(1f, Color(0xFF4CAF50), false)
        ActivityStatus.FAILURE -> ProgressState(1f, MaterialTheme.colorScheme.error, false)
        ActivityStatus.CANCELLED -> ProgressState(1f, Color(0xFFFF9800), false)
    }
}
