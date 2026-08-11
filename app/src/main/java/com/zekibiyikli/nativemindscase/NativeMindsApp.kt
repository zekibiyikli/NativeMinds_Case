package com.zekibiyikli.nativemindscase

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.zekibiyikli.nativemindscase.crashlytics.CrashReporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Firebase, firebase-common'daki ContentProvider sayesinde otomatik initialize olur;
 * burada Hilt, WorkManager ve Coil'in uygulama seviyesi kurulumu yapiliyor.
 */
@HiltAndroidApp
class NativeMindsApp : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    /** @HiltWorker ile isaretli worker'lara bagimlilik enjekte eder. */
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CrashReporter.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
    }

    /** Coil'in uygulama genelinde kullandigi ImageLoader. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
}
