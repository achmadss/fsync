package dev.achmad.fsync.base

import android.app.Application
import android.util.Log
import dev.achmad.core.di.coreModule
import dev.achmad.core.di.util.injectLazy
import dev.achmad.data.di.dataModule
import dev.achmad.domain.activity_log.ActivityLogRepository
import dev.achmad.domain.activity_log.ActivityStatus
import dev.achmad.domain.di.domainModule
import dev.achmad.fsync.util.isRunning
import dev.achmad.fsync.util.workManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import java.time.Instant

class MainApplication: Application() {

    private val activityLogRepository: ActivityLogRepository by injectLazy()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            logger(
                object: Logger() {
                    override fun display(level: Level, msg: MESSAGE) {
                        when (level) {
                            Level.DEBUG -> Log.d(null, msg)
                            Level.INFO -> Log.i(null, msg)
                            Level.WARNING -> Log.w(null, msg)
                            Level.ERROR -> Log.e(null, msg)
                            Level.NONE -> Log.v(null, msg)
                        }
                    }
                }
            )
            androidContext(this@MainApplication)
            modules(
                listOf(
                    coreModule,
                    dataModule,
                    domainModule,
                )
            )
        }
        CoroutineScope(Dispatchers.IO).launch { onStartup() }
    }

    private suspend fun onStartup() {
        activityLogRepository.getOrphanedRunning().forEach { log ->
            if (!workManager.isRunning("sync_${log.folderId}")) {
                activityLogRepository.complete(
                    log.copy(
                        finishedAt = Instant.now(),
                        status = ActivityStatus.CANCELLED,
                        errorMessage = "App restarted while sync was running",
                    )
                )
            }
        }
    }
}