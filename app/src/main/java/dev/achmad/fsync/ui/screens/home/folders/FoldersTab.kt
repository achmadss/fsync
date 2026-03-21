package dev.achmad.fsync.ui.screens.home.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.domain.activity_log.ActivityLog
import dev.achmad.domain.activity_log.ActivityStatus
import dev.achmad.domain.folder_pair.FolderPair
import dev.achmad.domain.folder_pair.SyncStrategy
import dev.achmad.fsync.R
import dev.achmad.fsync.ui.components.AppBar
import dev.achmad.fsync.ui.components.AppBarActions
import dev.achmad.fsync.ui.components.EmptyScreen
import dev.achmad.fsync.ui.components.EmptyScreenButtonConfig
import dev.achmad.fsync.ui.components.SearchToolbar
import dev.achmad.fsync.ui.components.UnpaddedSwitch
import dev.achmad.fsync.ui.theme.AppTheme
import dev.achmad.fsync.util.activity_log.badgeLabelRes
import dev.achmad.fsync.util.activity_log.progressLabelRes
import dev.achmad.fsync.util.activity_log.toProgressState
import dev.achmad.fsync.util.folder_pair.displayNameRes
import dev.achmad.fsync.util.toHumanReadableSize
import dev.achmad.fsync.util.toRelativeTime
import java.time.Instant

object FoldersTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.folders),
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.Folder
                        else -> Icons.Outlined.Folder
                    }
                )
            )
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        @Suppress("UNUSED_VARIABLE")
        val navigator = LocalNavigator.current
        val screenModel = rememberScreenModel { FoldersTabScreenModel() }
        val state by screenModel.state.collectAsState()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        FoldersTabContent(
            state = state,
            onClickAddFolderPair = {},
            onSearch = { screenModel.setSearchQuery(it) },
            onClickFolderPair = {},
            onClickFolderPairMore = { screenModel.selectFolderPair(it) },
            onClickFilter = {},
        )

        val selectedFolderPair = state.selectedFolderPair
        if (selectedFolderPair != null) {
            ModalBottomSheet(
                onDismissRequest = { screenModel.dismissBottomSheet() },
                sheetState = sheetState,
            ) {
                FolderPairDetailBottomSheetContent(
                    folderPair = selectedFolderPair,
                    latestLog = state.selectedFolderLatestLog,
                    onToggleEnabled = { enabled ->
                        screenModel.setFolderPairEnabled(selectedFolderPair.id, enabled)
                    },
                    onClickSyncNow = {
                        screenModel.syncFolderPair(selectedFolderPair.id)
                    },
                    onClickViewActivityLog = {},
                    onClickEdit = {},
                    onClickDelete = {
                        screenModel.deleteFolderPair(selectedFolderPair.id)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldersTabContent(
    state: FoldersTabUiState = FoldersTabUiState(),
    onClickAddFolderPair: () -> Unit = {},
    onSearch: (String?) -> Unit = {},
    onClickFolderPair: (FolderPair) -> Unit = {},
    onClickFolderPairMore: (FolderPair) -> Unit = {},
    onClickFilter: () -> Unit = {},
) {
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(shadowElevation = 4.dp) {
                SearchToolbar(
                    searchQuery = searchQuery,
                    onSearch = onSearch,
                    onChangeSearchQuery = {
                        searchQuery = it
                        onSearch(it)
                    },
                    titleContent = {
                        Text(stringResource(R.string.folders))
                    },
                    actions = {
                        AppBarActions(
                            actions = listOf(
                                AppBar.Action(
                                    title = stringResource(R.string.filter),
                                    icon = Icons.Default.FilterList,
                                    onClick = onClickFilter,
                                ),
                            )
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.add_folder_pair)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onClickAddFolderPair,
                expanded = true,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
        ) {
            val isEmpty = state.folderPairs.isEmpty() && !state.isLoading
            if (isEmpty) {
                EmptyScreen(
                    modifier = Modifier.align(Alignment.Center),
                    title = stringResource(R.string.folder_pairs_empty_title),
                    description = stringResource(R.string.folder_pairs_empty_description),
                    secondaryButton = EmptyScreenButtonConfig(
                        text = stringResource(R.string.add_folder_pair),
                        onClick = onClickAddFolderPair,
                        icon = Icons.Default.Add,
                    ),
                    icon = {
                        Icon(
                            modifier = Modifier.size(48.dp),
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                        )
                    }
                )
                return@Box
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp
                )
            ) {
                items(state.filteredFolderPairs) { folderPair ->
                    FolderPairItem(
                        folderPair = folderPair,
                        latestLog = null,
                        onClickMore = { onClickFolderPairMore(folderPair) },
                        onClick = { onClickFolderPair(folderPair) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderPairItem(
    folderPair: FolderPair,
    latestLog: ActivityLog?,
    onClickMore: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = folderPair.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onClickMore,
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PathRow(icon = Icons.Default.Cloud, path = folderPair.remotePath)
            Spacer(modifier = Modifier.height(12.dp))
            PathRow(icon = Icons.Default.PhoneAndroid, path = folderPair.localPath)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(folderPair.strategy.displayNameRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        val (progressValue, progressColor, isIndeterminate) = latestLog.toProgressState()
        if (isIndeterminate) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
                strokeCap = StrokeCap.Square,
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progressValue },
                color = progressColor ?: MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
                strokeCap = StrokeCap.Square,
                drawStopIndicator = {},
            )
        }
        Column(
            modifier = Modifier.padding(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val context = LocalContext.current
                val lastSyncValue = folderPair.lastSyncAt?.toRelativeTime(context)
                    ?: stringResource(R.string.never)
                Text(
                    text = stringResource(R.string.last_sync, lastSyncValue),
                    style = MaterialTheme.typography.labelSmall,
                )
                FolderPairBadge(activityLog = latestLog)
            }
        }
    }
}

@Composable
private fun PathRow(icon: ImageVector, path: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = path,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FolderPairBadge(activityLog: ActivityLog?) {
    val color = when (activityLog?.status) {
        null -> Color(0xFF9E9E9E)
        ActivityStatus.RUNNING -> MaterialTheme.colorScheme.primary
        ActivityStatus.SUCCESS -> Color(0xFF4CAF50)
        ActivityStatus.FAILURE -> MaterialTheme.colorScheme.error
        ActivityStatus.CANCELLED -> Color(0xFFFF9800)
    }
    Badge(
        containerColor = color.copy(alpha = 0.15f),
        contentColor = color,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(activityLog?.status.badgeLabelRes),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderPairDetailBottomSheetContent(
    folderPair: FolderPair,
    latestLog: ActivityLog?,
    onToggleEnabled: (Boolean) -> Unit = {},
    onClickSyncNow: () -> Unit = {},
    onClickViewActivityLog: () -> Unit = {},
    onClickEdit: () -> Unit = {},
    onClickDelete: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        // Header: name + badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = folderPair.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            FolderPairBadge(activityLog = latestLog)
        }

        // Path info rows
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PathRow(icon = Icons.Default.Cloud, path = folderPair.remotePath)
            PathRow(icon = Icons.Default.PhoneAndroid, path = folderPair.localPath)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(folderPair.strategy.displayNameRes),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Stat boxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val totalSize = latestLog?.bytesTransferred?.toHumanReadableSize() ?: "0 B"
            val synced = latestLog?.let {
                it.filesUploaded + it.filesDownloaded + it.filesDeleted
            } ?: 0

            StatBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.stat_total),
                value = totalSize,
                color = MaterialTheme.colorScheme.primary,
            )
            StatBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.stat_synced),
                value = synced.toString(),
                color = Color(0xFF4CAF50),
            )
            StatBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.stat_remaining),
                value = "0",
                color = Color(0xFF9E9E9E),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            val (progressValue, progressColor, isIndeterminate) = latestLog.toProgressState()
            val progressLabel = stringResource(latestLog?.status.progressLabelRes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.sync_progress),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!isIndeterminate) {
                    Text(
                        text = "${(progressValue * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (isIndeterminate) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progressValue },
                    color = progressColor ?: MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                    drawStopIndicator = {},
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val finishedAt = latestLog?.finishedAt
                if (finishedAt != null) {
                    Text(
                        text = stringResource(R.string.last_synced, finishedAt.toRelativeTime(LocalContext.current)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor ?: MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // Enable toggle row
        BottomSheetActionRow(
            icon = Icons.Default.PowerSettingsNew,
            label = stringResource(R.string.enable),
            trailingContent = {
                UnpaddedSwitch(
                    checked = folderPair.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    scale = 0.8f,
                )
            }
        )
        HorizontalDivider()

        // Action rows
        BottomSheetActionRow(
            icon = Icons.Default.Sync,
            label = stringResource(R.string.sync_now),
            onClick = onClickSyncNow,
        )
        HorizontalDivider()
        BottomSheetActionRow(
            icon = Icons.Default.History,
            label = stringResource(R.string.view_activity_log),
            onClick = onClickViewActivityLog,
        )
        HorizontalDivider()
        BottomSheetActionRow(
            icon = Icons.Default.Edit,
            label = stringResource(R.string.edit),
            onClick = onClickEdit,
        )
        HorizontalDivider()
        BottomSheetActionRow(
            icon = Icons.Default.Delete,
            label = stringResource(R.string.delete_pair),
            tint = MaterialTheme.colorScheme.error,
            onClick = onClickDelete,
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun BottomSheetActionRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = null,
                tint = tint,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
        )
        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FolderPairFilterBottomSheetContent() {
}

// --- Previews ---

@Composable
@Preview
private fun FoldersTabContentPreview() {
    AppTheme {
        val folderPair = FolderPair(
            name = "NAS Backup",
            localPath = "/storage/emulated/0/Music",
            remotePath = "/NAS/Music/Youtube/Favorites",
            storageAccountId = 0L,
            strategy = SyncStrategy.DOWNLOAD_THEN_DELETE,
            isEnabled = true,
            lastSyncAt = Instant.now().minusSeconds(300),
        )
        FoldersTabContent(
            state = FoldersTabUiState(
                folderPairs = listOf(folderPair),
                isLoading = false,
            )
        )
    }
}

@Composable
@Preview
private fun FoldersTabContentPreviewDark() {
    AppTheme(darkTheme = true) {
        FoldersTabContent(
            state = FoldersTabUiState(isLoading = false),
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun FolderPairBottomSheetContentPreview() {
    AppTheme {
        FolderPairDetailBottomSheetContent(
            folderPair = FolderPair(
                name = "NAS Backup",
                localPath = "/storage/emulated/0/Music",
                remotePath = "/NAS/Music/Youtube/Favorites/Playlist",
                storageAccountId = 0L,
                strategy = SyncStrategy.DOWNLOAD_THEN_DELETE,
                isEnabled = true,
                lastSyncAt = Instant.now(),
            ),
            latestLog = ActivityLog(
                folderId = 0L,
                startedAt = Instant.now().minusSeconds(4),
                finishedAt = Instant.now(),
                status = ActivityStatus.SUCCESS,
                filesUploaded = 120,
                filesDownloaded = 0,
                filesDeleted = 0,
                bytesTransferred = 2_254_857_830L,
            ),
        )
    }
}
