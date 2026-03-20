package dev.achmad.data.remote

import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityLogRepository
import dev.achmad.domain.activity_log.ActivityStatus
import dev.achmad.domain.folder_pair.FolderPairRepository
import dev.achmad.domain.folder_pair.SyncService
import dev.achmad.domain.folder_pair.SyncStrategy
import dev.achmad.domain.storage_account.StorageAccountRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

class SyncEngine(
    private val clientFactory: StorageClientFactory,
    private val storageAccountRepository: StorageAccountRepository,
    private val folderPairRepository: FolderPairRepository,
    private val activityLogRepository: ActivityLogRepository,
) : SyncService {

    override suspend fun sync(folderId: Long): Result<ActivityLog> {
        val pair = folderPairRepository.getById(folderId)
            ?: return Result.failure(IllegalArgumentException("FolderPair $folderId not found"))
        val account = storageAccountRepository.getById(pair.storageAccountId)
            ?: return Result.failure(IllegalArgumentException("StorageAccount ${pair.storageAccountId} not found"))

        val client = clientFactory.create(account)

        val initialLog = ActivityLog(
            folderId = folderId,
            startedAt = Instant.now(),
            status = ActivityStatus.RUNNING,
        )
        val log = activityLogRepository.create(initialLog).getOrElse {
            return Result.failure(it)
        }

        return try {
            val localFiles = scanLocalFiles(pair.localPath)
            val remoteFiles = client.listFiles(pair.remotePath)

            val counters = SyncCounters()

            when (pair.strategy) {
                SyncStrategy.UPLOAD_ONLY -> uploadOnly(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.UPLOAD_THEN_DELETE -> uploadThenDelete(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.UPLOAD_MIRROR -> uploadMirror(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.DOWNLOAD_ONLY -> downloadOnly(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.DOWNLOAD_THEN_DELETE -> downloadThenDelete(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.DOWNLOAD_MIRROR -> downloadMirror(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
                SyncStrategy.TWO_WAY -> twoWay(
                    client = client,
                    localRoot = pair.localPath,
                    remoteRoot = pair.remotePath,
                    local = localFiles,
                    remote = remoteFiles,
                    counters = counters
                )
            }

            val completed = log.copy(
                finishedAt = Instant.now(),
                status = ActivityStatus.SUCCESS,
                filesUploaded = counters.uploaded,
                filesDownloaded = counters.downloaded,
                filesDeleted = counters.deleted,
                bytesTransferred = counters.bytesTransferred,
            )
            activityLogRepository.complete(completed)
            folderPairRepository.updateLastSyncAt(folderId)
            Result.success(completed)
        } catch (e: CancellationException) {
            val failed = log.copy(
                finishedAt = Instant.now(),
                status = ActivityStatus.CANCELLED,
                errorMessage = e.message,
            )
            activityLogRepository.complete(failed)
            Result.failure(e)
        } catch (e: Exception) {
            val failed = log.copy(
                finishedAt = Instant.now(),
                status = ActivityStatus.FAILURE,
                errorMessage = e.message,
            )
            activityLogRepository.complete(failed)
            Result.failure(e)
        }
    }

    // ── Strategy implementations ──────────────────────────────────────────────

    private suspend fun uploadOnly(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        val remoteByPath = remote.associateBy { it.path.removePrefix("$remoteRoot/") }
        for (file in local.filter { !it.isDirectory }) {
            val remoteEntry = remoteByPath[file.relativePath]
            if (remoteEntry == null || file.lastModified.isAfter(remoteEntry.lastModified) || file.size != remoteEntry.size) {
                client.uploadFile("$localRoot/${file.relativePath}", "$remoteRoot/${file.relativePath}").getOrThrow()
                counters.uploaded++
                counters.bytesTransferred += file.size
            }
        }
    }

    private suspend fun uploadThenDelete(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        uploadOnly(client, localRoot, remoteRoot, local, remote, counters)
        for (file in local.filter { !it.isDirectory }) {
            File("$localRoot/${file.relativePath}").delete()
        }
    }

    private suspend fun uploadMirror(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        uploadOnly(client, localRoot, remoteRoot, local, remote, counters)
        val localPaths = local.map { it.relativePath }.toSet()
        for (file in remote.filter { !it.isDirectory }) {
            val rel = file.path.removePrefix("$remoteRoot/")
            if (rel !in localPaths) {
                client.deleteFile(file.path).getOrThrow()
                counters.deleted++
            }
        }
    }

    private suspend fun downloadOnly(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        val localByPath = local.associateBy { it.relativePath }
        for (file in remote.filter { !it.isDirectory }) {
            val rel = file.path.removePrefix("$remoteRoot/")
            val localEntry = localByPath[rel]
            if (localEntry == null || file.lastModified.isAfter(localEntry.lastModified) || file.size != localEntry.size) {
                client.downloadFile(file.path, "$localRoot/$rel").getOrThrow()
                counters.downloaded++
                counters.bytesTransferred += file.size
            }
        }
    }

    private suspend fun downloadThenDelete(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        downloadOnly(client, localRoot, remoteRoot, local, remote, counters)
        for (file in remote.filter { !it.isDirectory }) {
            client.deleteFile(file.path).getOrThrow()
            counters.deleted++
        }
    }

    private suspend fun downloadMirror(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        downloadOnly(client, localRoot, remoteRoot, local, remote, counters)
        val remotePaths = remote.map { it.path.removePrefix("$remoteRoot/") }.toSet()
        for (file in local.filter { !it.isDirectory }) {
            if (file.relativePath !in remotePaths) {
                File("$localRoot/${file.relativePath}").delete()
                counters.deleted++
            }
        }
    }

    private suspend fun twoWay(
        client: RemoteStorageClient,
        localRoot: String,
        remoteRoot: String,
        local: List<LocalFile>,
        remote: List<RemoteFile>,
        counters: SyncCounters,
    ) {
        val localByPath = local.filter { !it.isDirectory }.associateBy { it.relativePath }
        val remoteByPath = remote.filter { !it.isDirectory }.associateBy { it.path.removePrefix("$remoteRoot/") }
        val allPaths = localByPath.keys + remoteByPath.keys

        for (rel in allPaths) {
            val l = localByPath[rel]
            val r = remoteByPath[rel]
            when {
                l != null && r == null -> {
                    // local-only → upload
                    client.uploadFile("$localRoot/$rel", "$remoteRoot/$rel").getOrThrow()
                    counters.uploaded++
                    counters.bytesTransferred += l.size
                }
                l == null && r != null -> {
                    // remote-only → download
                    client.downloadFile(r.path, "$localRoot/$rel").getOrThrow()
                    counters.downloaded++
                    counters.bytesTransferred += r.size
                }
                l != null && r != null -> {
                    // both exist → newer wins; if sizes differ and remote is not newer, re-upload
                    val localIsNewer = l.lastModified.isAfter(r.lastModified)
                    val remoteIsNewer = r.lastModified.isAfter(l.lastModified)
                    val sizesDiffer = l.size != r.size
                    when {
                        localIsNewer || (sizesDiffer && !remoteIsNewer) -> {
                            client.uploadFile("$localRoot/$rel", "$remoteRoot/$rel").getOrThrow()
                            counters.uploaded++
                            counters.bytesTransferred += l.size
                        }
                        remoteIsNewer -> {
                            client.downloadFile(r.path, "$localRoot/$rel").getOrThrow()
                            counters.downloaded++
                            counters.bytesTransferred += r.size
                        }
                        // timestamps equal AND sizes equal → no-op
                    }
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun scanLocalFiles(rootPath: String): List<LocalFile> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists()) return@withContext emptyList()
        root.walkTopDown().drop(1).map { file ->
            LocalFile(
                relativePath = file.relativeTo(root).path,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = Instant.ofEpochMilli(file.lastModified()),
                isDirectory = file.isDirectory,
            )
        }.toList()
    }

    private data class LocalFile(
        val relativePath: String,
        val size: Long,
        val lastModified: Instant,
        val isDirectory: Boolean,
    )

    private class SyncCounters {
        var uploaded: Int = 0
        var downloaded: Int = 0
        var deleted: Int = 0
        var bytesTransferred: Long = 0L
    }
}
