package dev.achmad.data.remote

interface RemoteStorageClient {
    suspend fun listFiles(remotePath: String): List<RemoteFile>
    suspend fun uploadFile(localPath: String, remotePath: String): Result<Unit>
    suspend fun downloadFile(remotePath: String, localPath: String): Result<Unit>
    suspend fun deleteFile(remotePath: String): Result<Unit>
    suspend fun createDirectory(remotePath: String): Result<Unit>
    suspend fun exists(remotePath: String): Boolean
}
