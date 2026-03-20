package dev.achmad.core.di.util

import android.content.Context
import org.koin.android.error.MissingAndroidContextException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatformTools

inline fun <reified T : Any> injectLazy(): Lazy<T> {
    return lazy { KoinPlatformTools.defaultContext().get().get(T::class) }
}

inline fun <reified T : Any> injectLazy(key: String): Lazy<T> {
    return lazy { KoinPlatformTools.defaultContext().get().get(T::class, named(key)) }
}

inline fun <reified T : Any> inject(): T {
    return KoinPlatformTools.defaultContext().get().get(T::class)
}

inline fun <reified T : Any> inject(key: String): T {
    return KoinPlatformTools.defaultContext().get().get(T::class, named(key))
}

fun injectApplicationContext(): Context {
    val context: Context = try {
        inject()
    } catch (e: Exception) {
        e.printStackTrace()
        throw MissingAndroidContextException("Can't resolve Context instance.")
    }
    return context.applicationContext
}