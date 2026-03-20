package dev.achmad.domain.folder_pair

import java.time.Instant

data class FolderPair(
    val id: Long = 0L,
    val name: String,
    val localPath: String,
    val remotePath: String,
    val storageAccountId: Long,
    val strategy: SyncStrategy,
    val isEnabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val lastSyncAt: Instant? = null,
)

enum class SyncStrategy {
    /** Bidirectional sync with conflict resolution. */
    TWO_WAY,

    /** Copy local files to remote. Remote-only files are left untouched. */
    UPLOAD_ONLY,

    /** Copy local files to remote, then delete the local originals after a successful upload. */
    UPLOAD_THEN_DELETE,

    /** Make the remote an exact mirror of local: upload new/changed local files and delete remote-only files. */
    UPLOAD_MIRROR,

    /** Copy remote files to local. Local-only files are left untouched. */
    DOWNLOAD_ONLY,

    /** Copy remote files to local, then delete the remote originals after a successful download. */
    DOWNLOAD_THEN_DELETE,

    /** Make local an exact mirror of remote: download new/changed remote files and delete local-only files. */
    DOWNLOAD_MIRROR,
}
