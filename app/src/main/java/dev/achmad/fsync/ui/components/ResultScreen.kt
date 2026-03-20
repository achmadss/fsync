package dev.achmad.fsync.ui.components

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen

abstract class ResultScreen: Screen, Parcelable {
    var arguments: Bundle = Bundle()

    @Composable
    final override fun Content() {
        val currentArguments = remember(arguments) {
            Bundle(arguments).also {
                arguments.clear()
            }
        }
        Content(currentArguments)
    }

    @Composable
    abstract fun Content(arguments: Bundle)
}