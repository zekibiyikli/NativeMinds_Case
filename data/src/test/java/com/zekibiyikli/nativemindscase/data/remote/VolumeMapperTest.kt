package com.zekibiyikli.nativemindscase.data.remote

import com.zekibiyikli.nativemindscase.data.remote.dto.ImageLinksDto
import com.zekibiyikli.nativemindscase.data.remote.dto.VolumeDto
import com.zekibiyikli.nativemindscase.data.remote.dto.VolumeInfoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeMapperTest {

    @Test
    fun `kapak linki https'e cevrilir`() {
        // Google http donuyor; cleartext engelli oldugu icin gorsel yuklenmezdi.
        val dto = volume(
            imageLinks = ImageLinksDto(thumbnail = "http://books.google.com/books?id=1&img=1")
        )

        assertEquals("https://books.google.com/books?id=1&img=1", dto.toContentItem().coverUrl)
    }

    @Test
    fun `thumbnail yoksa smallThumbnail kullanilir`() {
        val dto = volume(
            imageLinks = ImageLinksDto(smallThumbnail = "https://example.com/small.jpg")
        )

        assertEquals("https://example.com/small.jpg", dto.toContentItem().coverUrl)
    }

    @Test
    fun `gorsel hic yoksa null doner`() {
        assertNull(volume(imageLinks = null).toContentItem().coverUrl)
    }

    @Test
    fun `aciklamadaki html temizlenir`() {
        val dto = volume(
            description = "<p>Bir <b>kitap</b> &quot;ozeti&quot; &amp; devami</p>"
        )

        assertEquals("Bir kitap \"ozeti\" & devami", dto.toContentItem().description)
    }

    @Test
    fun `alt baslik varsa basliga eklenir`() {
        val dto = volume(title = "Dune", subtitle = "Cöl Gezegeni")

        assertEquals("Dune: Cöl Gezegeni", dto.toContentItem().title)
    }

    @Test
    fun `bos volumeInfo cokme yerine bos alanlar uretir`() {
        val item = VolumeDto(id = "abc", volumeInfo = null).toContentItem()

        assertEquals("abc", item.id)
        assertTrue(item.title.isEmpty())
        assertTrue(item.author.isEmpty())
        assertEquals(0, item.pageCount)
    }

    @Test
    fun `birden fazla yazar virgulle birlestirilir`() {
        val dto = volume(authors = listOf("Ali Veli", "Ayse Fatma"))

        assertEquals("Ali Veli, Ayse Fatma", dto.toContentItem().author)
    }

    private fun volume(
        title: String? = "Baslik",
        subtitle: String? = null,
        authors: List<String>? = null,
        description: String? = null,
        imageLinks: ImageLinksDto? = null
    ) = VolumeDto(
        id = "abc",
        volumeInfo = VolumeInfoDto(
            title = title,
            subtitle = subtitle,
            authors = authors,
            description = description,
            imageLinks = imageLinks
        )
    )
}
