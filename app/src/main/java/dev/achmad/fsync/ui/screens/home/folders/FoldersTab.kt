package dev.achmad.fsync.ui.screens.home.folders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.fsync.R
import dev.achmad.fsync.ui.components.AppBar

object FoldersTab: Tab {
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
        val navigator = LocalNavigator.current
        FoldersTabContent(

        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldersTabContent(

) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.folders),
            )
        }
    ) { contentPadding ->

    }
}