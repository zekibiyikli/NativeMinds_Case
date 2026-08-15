import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Release imzalama bilgileri keystore/keystore.properties'ten okunur; ne dosya ne de
 * .jks repoya girer. Keystore yoksa signingConfig hic olusturulmaz ve release imzasiz
 * uretilir — repoyu klonlayan biri debug tarafinda takilmasin diye build patlamiyor.
 * providers.fileContents kullaniliyor ki configuration cache dogru invalide olsun.
 */
val keystoreProperties: String = providers
    .fileContents(rootProject.layout.projectDirectory.file("keystore/keystore.properties"))
    .asText.orNull.orEmpty()

fun keystoreProperty(key: String): String = keystoreProperties
    .lineSequence()
    .map(String::trim)
    .firstOrNull { it.startsWith("$key=") }
    ?.substringAfter('=')
    ?.trim()
    .orEmpty()

val releaseStoreFile = keystoreProperty("storeFile")
    .takeIf { it.isNotBlank() }
    ?.let(rootProject::file)
    ?.takeIf { it.exists() }

android {
    namespace = "com.zekibiyikli.nativemindscase"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zekibiyikli.nativemindscase"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = keystoreProperty("storePassword")
                keyAlias = keystoreProperty("keyAlias")
                keyPassword = keystoreProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Debug derlemelerinde mapping upload'a gerek yok, build süresini uzatıyor.
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            // Keystore tanımlıysa imzalanır, değilse imzasız çıkar (kurulamaz ama derlenir).
            signingConfig = signingConfigs.findByName("release")

            // R8 kapalı: açmak Retrofit/kotlinx.serialization/Room tarafında keep kuralı
            // gerektirebiliyor ve gerçek cihazda baştan sona test edilmeden açılmamalı.
            // Debug'a göre asıl hız farkı zaten debuggable=false olmasından geliyor.
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Coroutines / Flow
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation (route'lar @Serializable oldugu icin json runtime'i gerekiyor)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    // NativeMindsApp'in Configuration.Provider'i icin HiltWorkerFactory.
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)

    // Paging (anasayfa feed sayfalamasi)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Lottie (splash animasyonu; anim_book.lottie dotLottie formatinda)
    implementation(libs.lottie.compose)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Premium sayfasindaki header GIF'i icin; Coil GIF'i cekirdekte cozmuyor.
    implementation(libs.coil.gif)

    // WorkManager: Application'daki Configuration.Provider ve sync planlamasi icin.
    implementation(libs.androidx.work.runtime.ktx)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.session)

    // Play Billing
    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}