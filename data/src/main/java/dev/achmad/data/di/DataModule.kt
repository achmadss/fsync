package dev.achmad.data.di

import androidx.room.Room
import dev.achmad.data.local.AppDatabase
import dev.achmad.data.local.activity_log.ActivityLogRepositoryImpl
import dev.achmad.data.local.folder_pair.FolderPairRepositoryImpl
import dev.achmad.data.local.storage_account.StorageAccountRepositoryImpl
import dev.achmad.data.remote.StorageClientFactory
import dev.achmad.data.remote.SyncEngine
import dev.achmad.domain.activity_log.ActivityLogRepository
import dev.achmad.domain.folder_pair.FolderPairRepository
import dev.achmad.domain.folder_pair.SyncService
import dev.achmad.domain.storage_account.StorageAccountRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "fsync.db")
            .build()
    }
    single { get<AppDatabase>().storageAccountDao() }
    single { get<AppDatabase>().folderPairDao() }
    single { get<AppDatabase>().activityLogDao() }

    // Repositories
    single<StorageAccountRepository> { StorageAccountRepositoryImpl(get()) }
    single<FolderPairRepository> { FolderPairRepositoryImpl(get()) }
    single<ActivityLogRepository> { ActivityLogRepositoryImpl(get()) }

    // Remote
    single { StorageClientFactory() }
    single<SyncService> { SyncEngine(get(), get(), get(), get()) }
}
