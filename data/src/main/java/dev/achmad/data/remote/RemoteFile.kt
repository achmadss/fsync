package dev.achmad.data.remote

import java.time.Instant

data class RemoteFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Instant,
    val isDirectory: Boolean,
)
