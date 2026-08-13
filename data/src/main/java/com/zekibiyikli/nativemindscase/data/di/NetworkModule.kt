package com.zekibiyikli.nativemindscase.data.di

import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.BuildConfig
import com.zekibiyikli.nativemindscase.data.remote.ApiKeyInterceptor
import com.zekibiyikli.nativemindscase.data.remote.GoogleBooksApi
import com.zekibiyikli.nativemindscase.data.remote.RetryOnServerErrorInterceptor
import com.zekibiyikli.nativemindscase.data.remote.anthropic.AnthropicApi
import com.zekibiyikli.nativemindscase.data.remote.anthropic.AnthropicAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Iki farkli servis var; istemcileri karismasin diye ayriliyorlar. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleBooksClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AnthropicClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Google Books yanitlari cok genis; sadece ihtiyacimiz olan alanlari modelledik.
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /** Log'lara sizmamasi icin URL'deki key parametresi maskeleniyor. */
    private val apiKeyPattern = Regex("key=[^&\\s]+")

    private fun maskedLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            HttpLoggingInterceptor.Logger.DEFAULT.log(message.replace(apiKeyPattern, "key=***"))
        }.apply { level = HttpLoggingInterceptor.Level.BASIC }

    @Provides
    @Singleton
    @GoogleBooksClient
    fun provideGoogleBooksOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY))
        .addInterceptor(RetryOnServerErrorInterceptor())
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(maskedLoggingInterceptor())
        }
        .connectTimeout(AppConfig.Network.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AppConfig.Network.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @GoogleBooksClient
    fun provideGoogleBooksRetrofit(
        @GoogleBooksClient client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Network.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideGoogleBooksApi(@GoogleBooksClient retrofit: Retrofit): GoogleBooksApi =
        retrofit.create(GoogleBooksApi::class.java)

    /**
     * Ozet uretimi tek bir istek ama model dusunup yazdigi icin Google Books
     * cagrilarindan belirgin uzun surebiliyor; okuma zaman asimi ayri tutuldu.
     *
     * Anahtar basligi loglanmiyor: HttpLoggingInterceptor BASIC seviyesinde
     * basliklari basmiyor, sadece URL ve durum kodu yaziliyor.
     */
    @Provides
    @Singleton
    @AnthropicClient
    fun provideAnthropicOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AnthropicAuthInterceptor(apiKey = BuildConfig.ANTHROPIC_API_KEY))
        .addInterceptor(RetryOnServerErrorInterceptor())
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(maskedLoggingInterceptor())
        }
        .connectTimeout(AppConfig.Network.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AppConfig.Anthropic.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @AnthropicClient
    fun provideAnthropicRetrofit(
        @AnthropicClient client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Anthropic.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAnthropicApi(@AnthropicClient retrofit: Retrofit): AnthropicApi =
        retrofit.create(AnthropicApi::class.java)
}
