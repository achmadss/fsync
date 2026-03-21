package dev.achmad.fsync.ui.screens.home.folders

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.usecase.GetFolderActivityLogUseCase
import dev.achmad.domain.folder_pair.FolderPair
import dev.achmad.domain.folder_pair.usecase.DeleteFolderPairUseCase
import dev.achmad.domain.folder_pair.usecase.GetFolderPairsUseCase
import dev.achmad.domain.folder_pair.usecase.SetFolderPairEnabledUseCase
import dev.achmad.fsync.worker.SyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FoldersTabUiState(
    val folderPairs: List<FolderPair> = emptyList(),
    val searchQuery: String? = null,
    val selectedFolderPair: FolderPair? = null,
    val selectedFolderLatestLog: ActivityLog? = null,
    val isLoading: Boolean = true,
) {
    val filteredFolderPairs: List<FolderPair>
        get() = if (searchQuery.isNullOrBlank()) folderPairs
                else folderPairs.filter { it.name.contains(searchQuery, ignoreCase = true) }
}

class FoldersTabScreenModel(
    private val setFolderPairEnabledUseCase: SetFolderPairEnabledUseCase = inject(),
    private val deleteFolderPairUseCase: DeleteFolderPairUseCase = inject(),
    private val getFolderActivityLogUseCase: GetFolderActivityLogUseCase = inject(),
    getFolderPairsUseCase: GetFolderPairsUseCase = inject(),
) : ScreenModel {

    private val _state = MutableStateFlow(FoldersTabUiState())
    val state: StateFlow<FoldersTabUiState> = _state.asStateFlow()

    private var logCollectionJob: Job? = null

    init {
        getFolderPairsUseCase()
            .onEach { pairs ->
                _state.update { it.copy(folderPairs = pairs, isLoading = false) }
            }
            .launchIn(screenModelScope)
    }

    fun setSearchQuery(query: String?) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun selectFolderPair(folderPair: FolderPair) {
        _state.update { it.copy(selectedFolderPair = folderPair, selectedFolderLatestLog = null) }
        logCollectionJob?.cancel()
        logCollectionJob = getFolderActivityLogUseCase(folderPair.id)
            .onEach { log ->
                _state.update { it.copy(selectedFolderLatestLog = log) }
            }
            .launchIn(screenModelScope)
    }

    fun dismissBottomSheet() {
        logCollectionJob?.cancel()
        logCollectionJob = null
        _state.update { it.copy(selectedFolderPair = null, selectedFolderLatestLog = null) }
    }

    fun setFolderPairEnabled(id: Long, enabled: Boolean) {
        screenModelScope.launch {
            setFolderPairEnabledUseCase(id, enabled)
        }
    }

    fun deleteFolderPair(id: Long) {
        screenModelScope.launch {
            deleteFolderPairUseCase(id)
            dismissBottomSheet()
        }
    }

    fun syncFolderPair(id: Long) {
        SyncWorker.dispatch(id)
    }

}