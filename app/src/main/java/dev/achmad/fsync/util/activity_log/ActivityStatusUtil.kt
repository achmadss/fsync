package dev.achmad.fsync.util.activity_log

import androidx.annotation.StringRes
import dev.achmad.domain.activity_log.ActivityStatus
import dev.achmad.fsync.R

val ActivityStatus?.badgeLabelRes: Int
    @StringRes get() = when (this) {
        null -> R.string.activity_status_never_synced
        ActivityStatus.RUNNING -> R.string.activity_status_syncing
        ActivityStatus.SUCCESS -> R.string.activity_status_synced
        ActivityStatus.FAILURE -> R.string.activity_status_failed
        ActivityStatus.CANCELLED -> R.string.activity_status_cancelled
    }

val ActivityStatus?.progressLabelRes: Int
    @StringRes get() = when (this) {
        null -> R.string.progress_status_no_sync_yet
        ActivityStatus.RUNNING -> R.string.progress_status_syncing
        ActivityStatus.SUCCESS -> R.string.progress_status_up_to_date
        ActivityStatus.FAILURE -> R.string.progress_status_failed
        ActivityStatus.CANCELLED -> R.string.progress_status_cancelled
    }
