package dev.achmad.data.remote.smb

import dev.achmad.data.remote.RemoteFile
import dev.achmad.data.remote.RemoteStorageClient
import dev.achmad.domain.storage_account.StorageAccount
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.Properties

class SmbStorageClient(private val account: StorageAccount.Smb) : RemoteStorageClient {

    private val cifsContext: CIFSContext by lazy {
        val auth = NtlmPasswordAuthenticator(
            account.domain.ifEmpty { null },
            account.username,
            account.password,
        )
        BaseContext(PropertyConfiguration(Properties())).withCredentials(auth)
    }

    private fun smbUrl(path: String): String {
        val normalizedPath = path.trimStart('/')
        return "smb://${account.host}/${account.shareName}/$normalizedPath"
    }

    override suspend fun listFiles(remotePath: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        SmbFile(smbUrl(remotePath), cifsContext).use { dir ->
            dir.listFiles()?.map { file ->
                RemoteFile(
                    name = file.name.trimEnd('/'),
                    path = "$remotePath/${file.name.trimEnd('/')}",
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = Instant.ofEpochMilli(file.lastModified()),
                    isDirectory = file.isDirectory,
                )
            } ?: emptyList()
        }
    }

    override suspend fun uploadFile(localPath: String, remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tmpPath = "$remotePath.tmp"
        runCatching {
            SmbFile(smbUrl(tmpPath), cifsContext).use { tmp ->
                tmp.openOutputStream().use { out ->
                    File(localPath).inputStream().use { it.copyTo(out) }
                    Unit
                }
            }
            SmbFile(smbUrl(tmpPath), cifsContext).renameTo(SmbFile(smbUrl(remotePath), cifsContext))
        }.onFailure {
            runCatching { SmbFile(smbUrl(tmpPath), cifsContext).use { it.delete() } }
        }
    }

    override suspend fun downloadFile(remotePath: String, localPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tmp = File("$localPath.tmp")
        runCatching {
            tmp.parentFile?.mkdirs()
            SmbFile(smbUrl(remotePath), cifsContext).use { remote ->
                remote.openInputStream().use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                    Unit
                }
            }
            check(tmp.renameTo(File(localPath))) { "Failed to rename temp file to $localPath" }
        }.onFailure { tmp.delete() }
    }

    override suspend fun deleteFile(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            SmbFile(smbUrl(remotePath), cifsContext).use { it.delete() }
        }
    }

    override suspend fun createDirectory(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            SmbFile(smbUrl("$remotePath/"), cifsContext).use { it.mkdir() }
        }
    }

    override suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            SmbFile(smbUrl(remotePath), cifsContext).use { it.exists() }
        }.getOrDefault(false)
    }
}
