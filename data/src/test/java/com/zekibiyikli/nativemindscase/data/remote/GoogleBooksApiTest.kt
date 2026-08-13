package com.zekibiyikli.nativemindscase.data.remote

import com.zekibiyikli.nativemindscase.core.result.AppException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Retrofit + kotlinx-serialization + mapper zincirini gercek bir Google Books
 * yaniti uzerinde dogrular. Canli API'nin kotasindan bagimsizdir.
 */
class GoogleBooksApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: GoogleBooksApi

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor("TEST_KEY")).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GoogleBooksApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `arama yaniti parse edilip ContentItem'a cevrilir`() = runTest {
        server.enqueue(MockResponse().setBody(VOLUMES_JSON).setResponseCode(200))

        val items = api.searchVolumes(query = "subject:fiction").items
            .orEmpty()
            .map { it.toContentItem() }

        assertEquals(2, items.size)

        val first = items.first()
        assertEquals("zyTCAlFPjgYC", first.id)
        assertEquals("Dune: Çöl Gezegeni", first.title)
        assertEquals("Frank Herbert", first.author)
        assertEquals(604, first.pageCount)
        // http -> https ve HTML temizligi mapper'da yapiliyor.
        assertEquals("https://books.google.com/cover1.jpg", first.coverUrl)
        assertTrue(first.description.startsWith("Arrakis"))
        assertTrue("<i>" !in first.description)

        // volumeInfo alanlari eksik olan kayit da cokmemeli.
        assertEquals("", items[1].author)
        assertNull(items[1].coverUrl)
    }

    @Test
    fun `istek dogru path ve parametrelerle gider`() = runTest {
        server.enqueue(MockResponse().setBody(VOLUMES_JSON).setResponseCode(200))

        api.searchVolumes(query = "dune subject:fiction", maxResults = 20, orderBy = "relevance")

        val request = server.takeRequest()
        val url = request.requestUrl!!

        assertEquals("/volumes", url.encodedPath)
        assertEquals("dune subject:fiction", url.queryParameter("q"))
        assertEquals("20", url.queryParameter("maxResults"))
        assertEquals("relevance", url.queryParameter("orderBy"))
        // Anahtar interceptor tarafindan ekleniyor.
        assertEquals("TEST_KEY", url.queryParameter("key"))
    }

    @Test
    fun `anahtar bossa key parametresi hic gonderilmez`() = runTest {
        val keylessApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor("")).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GoogleBooksApi::class.java)

        server.enqueue(MockResponse().setBody(VOLUMES_JSON).setResponseCode(200))
        keylessApi.searchVolumes(query = "test")

        assertNull(server.takeRequest().requestUrl!!.queryParameter("key"))
    }

    @Test
    fun `sonuc yoksa items alani olmadan da parse edilir`() = runTest {
        server.enqueue(
            MockResponse().setBody("""{"kind":"books#volumes","totalItems":0}""").setResponseCode(200)
        )

        assertNull(api.searchVolumes(query = "asdkjhasd").items)
    }

    @Test
    fun `429 RateLimited'a cevrilir`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))

        val error = runCatching { api.searchVolumes(query = "test") }
            .exceptionOrNull()
            ?.toAppException()

        assertTrue("Beklenen RateLimited, gelen: $error", error is AppException.RateLimited)
    }

    @Test
    fun `400 InvalidApiKey'e cevrilir`() = runTest {
        // Google Books gecersiz anahtarda 400 INVALID_ARGUMENT donuyor.
        server.enqueue(MockResponse().setResponseCode(400))

        val error = runCatching { api.searchVolumes(query = "test") }
            .exceptionOrNull()
            ?.toAppException()

        assertTrue("Beklenen InvalidApiKey, gelen: $error", error is AppException.InvalidApiKey)
    }

    @Test
    fun `500 Server hatasina cevrilir`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val error = runCatching { api.searchVolumes(query = "test") }
            .exceptionOrNull()
            ?.toAppException()

        assertEquals(500, (error as? AppException.Server)?.code)
    }

    private companion object {
        /** Google Books'un gercek yanit yapisi (alanlar kisaltildi). */
        val VOLUMES_JSON = """
        {
          "kind": "books#volumes",
          "totalItems": 2,
          "items": [
            {
              "kind": "books#volume",
              "id": "zyTCAlFPjgYC",
              "volumeInfo": {
                "title": "Dune",
                "subtitle": "Çöl Gezegeni",
                "authors": ["Frank Herbert"],
                "publishedDate": "1965",
                "description": "<p>Arrakis çölünde geçen bir <i>bilim kurgu</i> klasigi.</p>",
                "pageCount": 604,
                "categories": ["Fiction"],
                "imageLinks": {
                  "smallThumbnail": "http://books.google.com/small1.jpg",
                  "thumbnail": "http://books.google.com/cover1.jpg"
                },
                "language": "tr"
              },
              "accessInfo": { "viewability": "PARTIAL" }
            },
            {
              "kind": "books#volume",
              "id": "eksikVolume",
              "volumeInfo": { "title": "Yazarsiz Kitap" }
            }
          ]
        }
        """.trimIndent()
    }
}
