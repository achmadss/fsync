package dev.achmad.data.remote.webdav

import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import dev.achmad.data.remote.RemoteFile
import dev.achmad.data.remote.RemoteStorageClient
import dev.achmad.domain.storage_account.StorageAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

class WebDavStorageClient(private val account: StorageAccount.WebDav) : RemoteStorageClient {

    private val sardine: Sardine by lazy {
        OkHttpSardine().apply { setCredentials(account.username, account.password) }
    }

    private fun url(path: String): String {
        val scheme = if (account.useHttps) "https" else "http"
        val base = account.path.trimEnd('/')
        val normalized = path.trimStart('/')
        return "$scheme://${account.host}:${account.port}$base/$normalized"
    }

    override suspend fun listFiles(remotePath: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        sardine.list(url(remotePath))
            .drop(1) // first entry is the directory itself
            .map { resource ->
                RemoteFile(
                    name = resource.name,
                    path = "$remotePath/${resource.name}",
                    size = resource.contentLength ?: 0L,
                    lastModified = resource.modified?.toInstant() ?: Instant.EPOCH,
                    isDirectory = resource.isDirectory,
                )
            }
    }

    override suspend fun uploadFile(localPath: String, remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tmpUrl = url("$remotePath.tmp")
        runCatching {
            sardine.put(tmpUrl, File(localPath), "application/octet-stream")
            sardine.move(tmpUrl, url(remotePath))
        }.onFailure {
            runCatching { sardine.delete(tmpUrl) }
        }
    }

    override suspend fun downloadFile(remotePath: String, localPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tmp = File("$localPath.tmp")
        runCatching {
            tmp.parentFile?.mkdirs()
            sardine.get(url(remotePath)).use { input ->
                tmp.outputStream().use { input.copyTo(it) }
            }
            check(tmp.renameTo(File(localPath))) { "Failed to rename temp file to $localPath" }
        }.onFailure { tmp.delete() }
    }

    override suspend fun deleteFile(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { sardine.delete(url(remotePath)) }
    }

    override suspend fun createDirectory(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { sardine.createDirectory(url(remotePath)) }
    }

    override suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { sardine.exists(url(remotePath)) }.getOrDefault(false)
    }
}
