package dev.achmad.data.remote

import dev.achmad.data.remote.smb.SmbStorageClient
import dev.achmad.data.remote.webdav.WebDavStorageClient
import dev.achmad.domain.storage_account.StorageAccount

class StorageClientFactory {
    fun create(account: StorageAccount): RemoteStorageClient = when (account) {
        is StorageAccount.Smb -> SmbStorageClient(account)
        is StorageAccount.WebDav -> WebDavStorageClient(account)
    }
}
