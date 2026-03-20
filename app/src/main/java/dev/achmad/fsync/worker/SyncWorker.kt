package dev.achmad.fsync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.core.di.util.injectApplicationContext
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.folder_pair.SyncFolderUseCase
import dev.achmad.fsync.util.workManager

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val syncFolderUseCase: SyncFolderUseCase by injectLazy()

    override suspend fun doWork(): Result {
        val folderId = inputData.getLong(KEY_FOLDER_ID, -1L)
        return syncFolderUseCase(folderId).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() },
        )
    }

    companion object {
        const val KEY_FOLDER_ID = "folder_id"

        fun dispatch(folderId: Long) {
            val name = "sync_$folderId"
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(workDataOf(KEY_FOLDER_ID to folderId))
                .addTag(name)
                .build()
            injectApplicationContext()
                .workManager
                .enqueueUniqueWork(name, ExistingWorkPolicy.KEEP, request)
        }
    }
}
