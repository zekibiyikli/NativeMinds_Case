package com.zekibiyikli.nativemindscase.data.content

import com.zekibiyikli.nativemindscase.data.content.model.Subject

/**
 * Google Books'ta "tum kategorileri listele" endpoint'i yok, bu yuzden
 * kategori listesi burada sabit tutuluyor.
 *
 * [Subject.id] dogrudan "subject:" filtresine giden terim; Google Books'un
 * kendi kategori adlariyla birebir ayni olmali, yoksa sonuc donmez.
 * [Subject.name] ekranda gosterilen etiket — su an terimle ayni, gerekirse
 * Turkcelestirmek icin sadece bu alani degistirmek yeterli.
 */
internal object Subjects {

    val all: List<Subject> = listOf(
        "Fiction",
        "Fantasy",
        "Science Fiction",
        "Mystery",
        "Thriller",
        "Romance",
        "Horror",
        "Historical Fiction",
        "Adventure",
        "Crime",
        "Drama",
        "Poetry",
        "Young Adult Fiction",
        "Children's Fiction",
        "Biography & Autobiography",
        "History",
        "Science",
        "Philosophy",
        "Psychology",
        "Self-Help",
        "Business & Economics",
        "Politics",
        "Religion",
        "True Crime",
        "Health & Fitness",
        "Travel",
        "Art",
        "Cooking",
        "Education",
        "Technology",
        "Nature",
        "Comics & Graphic Novels",
        "Humor",
        "Music",
        "Sports & Recreation",
        "Language Arts",
        "Reference"
    ).map { term -> Subject(id = term, name = term) }

    /** Feed'in acilista gosterecegi kategori. */
    val default: Subject get() = all.first()

    /**
     * En cok tercih edilen kategoriler. Arama ekranindaki one cikan serit
     * bunlardan birini gosteriyor.
     */
    val popular: List<Subject> = listOf(
        "Fiction",
        "Fantasy",
        "Science Fiction",
        "Mystery",
        "Romance",
        "History",
        "Biography & Autobiography",
        "Self-Help"
    ).mapNotNull { term -> all.firstOrNull { it.id == term } }
}
