package com.zekibiyikli.nativemindscase.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `sadece kelime verilirse duz sorgu kurar`() {
        assertEquals("dune", SearchQuery.build(query = "dune", subjectId = null))
    }

    @Test
    fun `sadece kategori verilirse subject filtresi kurar`() {
        assertEquals("subject:fiction", SearchQuery.build(query = null, subjectId = "fiction"))
    }

    @Test
    fun `ikisi birlikte verilirse birlesik filtre kurar`() {
        // Ayirac bosluk: Retrofit literal "+" karakterini %2B'ye kodlar ve
        // Google bunu arama terimi sanip sonuclari bozar.
        assertEquals(
            "dune subject:fiction",
            SearchQuery.build(query = "dune", subjectId = "fiction")
        )
    }

    @Test
    fun `bastaki ve sondaki bosluklar temizlenir`() {
        assertEquals(
            "dune subject:fiction",
            SearchQuery.build(query = "  dune  ", subjectId = " fiction ")
        )
    }

    @Test
    fun `cok kelimeli kategori tirnaklanir`() {
        // Tirnaksiz gonderilirse Google "Fiction" kelimesini ayri bir arama
        // terimi sayar ve kategori filtresi bozulur.
        assertEquals(
            "subject:\"Science Fiction\"",
            SearchQuery.build(query = null, subjectId = "Science Fiction")
        )
        assertEquals(
            "subject:\"Biography & Autobiography\"",
            SearchQuery.build(query = null, subjectId = "Biography & Autobiography")
        )
    }

    @Test
    fun `tek kelimeli kategori tirnaklanmaz`() {
        assertEquals("subject:Fiction", SearchQuery.build(query = null, subjectId = "Fiction"))
        assertEquals("subject:Self-Help", SearchQuery.build(query = null, subjectId = "Self-Help"))
    }

    @Test
    fun `kelime ve cok kelimeli kategori birlikte`() {
        assertEquals(
            "dune subject:\"Science Fiction\"",
            SearchQuery.build(query = "dune", subjectId = "Science Fiction")
        )
    }

    @Test
    fun `ikisi de bossa bos sorgu doner`() {
        // Repository bu durumda istek atmadan bos liste yayiyor.
        assertEquals("", SearchQuery.build(query = "   ", subjectId = null))
        assertEquals("", SearchQuery.build(query = null, subjectId = ""))
    }
}
