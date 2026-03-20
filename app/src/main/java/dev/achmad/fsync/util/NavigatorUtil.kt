package dev.achmad.fsync.util

import android.os.Bundle
import cafe.adriel.voyager.navigator.Navigator
import dev.achmad.fsync.ui.components.ResultScreen
import java.io.Serializable

fun Navigator.popWithResult(bundle: Bundle) {
    val prev = if (items.size < 2) null else items[items.size - 2] as? ResultScreen
    prev?.arguments = bundle
    pop()
}

fun Navigator.popWithResult(vararg pairs: Pair<String, Any?>) {
    val bundle = Bundle()
    for ((key, value) in pairs) {
        when (value) {
            null -> bundle.putString(key, null)
            is Boolean -> bundle.putBoolean(key, value)
            is Byte -> bundle.putByte(key, value)
            is Char -> bundle.putChar(key, value)
            is Double -> bundle.putDouble(key, value)
            is Float -> bundle.putFloat(key, value)
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Short -> bundle.putShort(key, value)
            is String -> bundle.putString(key, value)
            is Bundle -> bundle.putBundle(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            else -> throw IllegalArgumentException("Unsupported type for key: $key")
        }
    }
    popWithResult(bundle)
}