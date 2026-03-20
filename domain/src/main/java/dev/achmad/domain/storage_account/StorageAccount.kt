package dev.achmad.domain.storage_account

sealed class StorageAccount {

    abstract val id: Long
    abstract val name: String
    abstract val host: String
    abstract val port: Int
    abstract val username: String
    abstract val password: String

    data class Smb(
        override val id: Long = 0L,
        override val name: String,
        override val host: String,
        override val port: Int = 445,
        override val username: String,
        override val password: String,
        val domain: String = "",
        val shareName: String,
    ) : StorageAccount()

    data class WebDav(
        override val id: Long = 0L,
        override val name: String,
        override val host: String,
        override val port: Int = 80,
        override val username: String,
        override val password: String,
        val path: String = "/",
        val useHttps: Boolean = true,
    ) : StorageAccount()
}
