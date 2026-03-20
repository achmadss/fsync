package dev.achmad.data.local.storage_account

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.achmad.domain.storage_account.StorageAccount

@Entity(tableName = "storage_accounts")
data class StorageAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,            // "smb" | "webdav"
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    // SMB-specific
    val smbDomain: String?,
    val smbShareName: String?,
    // WebDAV-specific
    val webdavPath: String?,
    val webdavUseHttps: Boolean?,
) {
    fun toDomain(): StorageAccount = when (type) {
        TYPE_SMB -> StorageAccount.Smb(
            id = id,
            name = name,
            host = host,
            port = port,
            username = username,
            password = password,
            domain = smbDomain ?: "",
            shareName = smbShareName ?: "",
        )
        TYPE_WEBDAV -> StorageAccount.WebDav(
            id = id,
            name = name,
            host = host,
            port = port,
            username = username,
            password = password,
            path = webdavPath ?: "/",
            useHttps = webdavUseHttps ?: true,
        )
        else -> error("Unknown storage account type: $type")
    }

    companion object {
        const val TYPE_SMB = "smb"
        const val TYPE_WEBDAV = "webdav"
    }
}

fun StorageAccount.toEntity(): StorageAccountEntity = when (this) {
    is StorageAccount.Smb -> StorageAccountEntity(
        id = id,
        type = StorageAccountEntity.TYPE_SMB,
        name = name,
        host = host,
        port = port,
        username = username,
        password = password,
        smbDomain = domain,
        smbShareName = shareName,
        webdavPath = null,
        webdavUseHttps = null,
    )
    is StorageAccount.WebDav -> StorageAccountEntity(
        id = id,
        type = StorageAccountEntity.TYPE_WEBDAV,
        name = name,
        host = host,
        port = port,
        username = username,
        password = password,
        smbDomain = null,
        smbShareName = null,
        webdavPath = path,
        webdavUseHttps = useHttps,
    )
}
