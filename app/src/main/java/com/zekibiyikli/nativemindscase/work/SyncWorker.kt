package com.zekibiyikli.nativemindscase.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zekibiyikli.nativemindscase.crashlytics.CrashReporter
import com.zekibiyikli.nativemindscase.data.local.SampleDao
import com.zekibiyikli.nativemindscase.di.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Hilt + WorkManager entegrasyonunun calisir ornegi. Su an sadece
 * veritabanina dokunup basarili donuyor; gercek senkronizasyon
 * mantigi buraya gelecek.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sampleDao: SampleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runCatching {
            sampleDao.getById(id = 0L)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { throwable ->
                CrashReporter.recordException(throwable, mapOf("worker" to WORK_NAME))
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            }
        )
    }

    companion object {
        const val WORK_NAME = "sync_worker"
        private const val MAX_RETRIES = 3

        /** Uygulama acilisinda cagrilabilir: gunde bir, sadece ag varken. */
        fun enqueuePeriodic(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
