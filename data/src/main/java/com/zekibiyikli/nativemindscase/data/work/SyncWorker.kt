package com.zekibiyikli.nativemindscase.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zekibiyikli.nativemindscase.core.crashlytics.CrashReporter
import com.zekibiyikli.nativemindscase.core.di.IoDispatcher
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.zekibiyikli.nativemindscase.core.config.AppConfig

/**
 * Kayitli favorilerin bilgilerini (kapak, ozet, sayfa sayisi) Google Books'tan
 * gunde bir tazeler; boylece cevrimdisi acilan favoriler bayatlamaz.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val contentRepository: ContentRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runCatching {
            contentRepository.refreshFavorites()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { throwable ->
                CrashReporter.recordException(throwable, mapOf("worker" to AppConfig.Sync.WORK_NAME))
                if (runAttemptCount < AppConfig.Sync.MAX_RETRIES) Result.retry() else Result.failure()
            }
        )
    }

    companion object {

        /** Uygulama acilisinda cagrilabilir: gunde bir, sadece ag varken. */
        fun enqueuePeriodic(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(AppConfig.Sync.INTERVAL_DAYS, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                AppConfig.Sync.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
