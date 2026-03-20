package dev.achmad.fsync.ui.screens.home.storage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.fsync.R

object StorageTab: Tab {
    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.storage),
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.Storage
                        else -> Icons.Outlined.Storage
                    }
                )
            )
        }

    @Composable
    override fun Content() {

    }
}