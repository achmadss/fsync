package dev.achmad.fsync.util.folder_pair

import androidx.annotation.StringRes
import dev.achmad.domain.folder_pair.SyncStrategy
import dev.achmad.fsync.R

val SyncStrategy.displayNameRes: Int
    @StringRes get() = when (this) {
        SyncStrategy.TWO_WAY -> R.string.sync_strategy_two_way
        SyncStrategy.UPLOAD_ONLY -> R.string.sync_strategy_upload_only
        SyncStrategy.UPLOAD_THEN_DELETE -> R.string.sync_strategy_upload_then_delete
        SyncStrategy.UPLOAD_MIRROR -> R.string.sync_strategy_upload_mirror
        SyncStrategy.DOWNLOAD_ONLY -> R.string.sync_strategy_download_only
        SyncStrategy.DOWNLOAD_THEN_DELETE -> R.string.sync_strategy_download_then_delete
        SyncStrategy.DOWNLOAD_MIRROR -> R.string.sync_strategy_download_mirror
    }
