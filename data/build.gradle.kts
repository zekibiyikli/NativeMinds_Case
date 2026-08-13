plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Google Books anahtari local.properties'ten okunur ve repoya girmez.
 * Anahtarsiz istekler de calisir (dusuk kota), bu yuzden bos olmasi hata degil.
 * providers.fileContents kullaniliyor ki configuration cache dogru invalide olsun.
 */
val localProperties: String = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText.orNull.orEmpty()

fun localProperty(key: String): String = localProperties
    .lineSequence()
    .map(String::trim)
    .firstOrNull { it.startsWith("$key=") }
    ?.substringAfter('=')
    ?.trim()
    .orEmpty()

val googleBooksApiKey: String = localProperty("GOOGLE_BOOKS_API_KEY")
val anthropicApiKey: String = localProperty("ANTHROPIC_API_KEY")

android {
    namespace = "com.zekibiyikli.nativemindscase.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"$googleBooksApiKey\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"$anthropicApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    // Room migration testleri icin sema dosyalari repoya yazilir.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    // Dispatcher qualifier'lari ve Outcome data'nin imzalarinda -> api.
    api(project(":core"))

    // Entity/DAO tipleri disari aciliyor.
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    // PagingData repository imzalarinda disari aciliyor -> api.
    api(libs.androidx.paging.common)

    // Retrofit + kotlinx-serialization; tipler repository'nin arkasinda kaliyor.
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging.interceptor)

    // SyncWorker.enqueuePeriodic imzasi WorkManager tipini disari aciyor -> api.
    api(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofit.converter.kotlinx.serialization)
}
