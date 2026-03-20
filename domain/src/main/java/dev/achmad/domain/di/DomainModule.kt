package dev.achmad.domain.di

import dev.achmad.domain.folder_pair.SyncFolderUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SyncFolderUseCase(get()) }
}