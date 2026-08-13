package com.zekibiyikli.nativemindscase.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryOnServerErrorInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    /** Testte gercek bekleme olmasin diye sleep no-op. */
    private fun client(maxAttempts: Int = 3) = OkHttpClient.Builder()
        .addInterceptor(RetryOnServerErrorInterceptor(maxAttempts = maxAttempts, sleep = {}))
        .build()

    private fun call(client: OkHttpClient) =
        client.newCall(Request.Builder().url(server.url("/volumes")).build()).execute()

    @Test
    fun `503 sonrasi 200 gelirse basarili yanit doner`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        call(client()).use { response ->
            assertEquals(200, response.code)
        }
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `surekli 503'te deneme sayisi asilmaz`() {
        repeat(5) { server.enqueue(MockResponse().setResponseCode(503)) }

        call(client(maxAttempts = 3)).use { response ->
            assertEquals(503, response.code)
        }
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `4xx tekrar denenmez`() {
        // Gecersiz anahtar veya kota asimi tekrar denemekle duzelmez.
        server.enqueue(MockResponse().setResponseCode(400))

        call(client()).use { response ->
            assertEquals(400, response.code)
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `basarili istek tek seferde biter`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        call(client()).use { response ->
            assertEquals(200, response.code)
        }
        assertEquals(1, server.requestCount)
    }
}
