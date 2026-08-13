package com.zekibiyikli.nativemindscase

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsUserProperty
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.crashlytics.CrashReporter
import com.zekibiyikli.nativemindscase.data.premium.PremiumRepository
import com.zekibiyikli.nativemindscase.data.work.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    /** Periyodik senkronizasyonu planlamak icin. */
    @Inject lateinit var workManager: WorkManager

    @Inject lateinit var premiumRepository: PremiumRepository

    /** Uygulama omru boyunca yasayan is; Application ile birlikte sonlaniyor. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        CrashReporter.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
        // KEEP politikasi sayesinde her acilista yeniden planlanmaz.
        SyncWorker.enqueuePeriodic(workManager)

        // Event parametresi degil kullanici ozelligi: boylece GA4'teki her
        // rapor premium/ucretsiz diye ayrilabiliyor, event'lere dokunmadan.
        appScope.launch {
            premiumRepository.isPremium.collect { isPremium ->
                AnalyticsHelper.setUserProperty(
                    name = AnalyticsUserProperty.IS_PREMIUM,
                    value = isPremium.toString()
                )
            }
        }
    }

    /** Coil'in uygulama genelinde kullandigi ImageLoader. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
                // ImageDecoder tabanli cozucu daha verimli ama API 28 gerektiriyor;
                // minSdk 24 oldugu icin eski cihazlarda Movie tabanli olana dusuluyor.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = AppConfig.Storage.IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(AppConfig.Storage.IMAGE_DISK_CACHE_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()
}
