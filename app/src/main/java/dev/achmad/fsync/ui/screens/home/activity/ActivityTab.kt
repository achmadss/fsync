package dev.achmad.fsync.ui.screens.home.activity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.fsync.R

object ActivityTab: Tab {
    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.activity),
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.SyncAlt
                        else -> Icons.Outlined.SyncAlt
                    }
                )
            )
        }

    @Composable
    override fun Content() {

    }
}