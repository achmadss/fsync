package dev.achmad.domain.di

import dev.achmad.domain.activity_log.usecase.GetFolderActivityLogUseCase
import dev.achmad.domain.folder_pair.usecase.DeleteFolderPairUseCase
import dev.achmad.domain.folder_pair.usecase.GetFolderPairsUseCase
import dev.achmad.domain.folder_pair.usecase.SetFolderPairEnabledUseCase
import dev.achmad.domain.folder_pair.usecase.SyncFolderUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SyncFolderUseCase(get()) }
    factory { GetFolderPairsUseCase(get()) }
    factory { SetFolderPairEnabledUseCase(get()) }
    factory { DeleteFolderPairUseCase(get()) }
    factory { GetFolderActivityLogUseCase(get()) }
}
