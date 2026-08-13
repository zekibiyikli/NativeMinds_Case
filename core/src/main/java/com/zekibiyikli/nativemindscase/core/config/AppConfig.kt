package com.zekibiyikli.nativemindscase.core.config

/**
 * Uygulamanin ayarlanabilir sabitleri tek yerde.
 *
 * Buraya girenler: davranisi belirleyen, urun karari olan ve degistirilmesi
 * makul olan degerler (sureler, limitler, zaman asimlari, sayfa boyutu).
 *
 * Buraya girmeyenler:
 * - HTTP durum kodlari gibi protokol sabitleri (degistirilemez)
 * - Birim cevrimleri (or. bir gundeki milisaniye)
 * - Tekil ekran yerlesimi (padding, aralik); onlar kullanildiklari
 *   composable'da kalmali, yoksa okunurluk duser
 *
 * :core'da duruyor cunku hem :app hem :data buna erisiyor.
 */
object AppConfig {

    /** Sunum katmani sureleri ve olculeri. */
    object Ui {
        /** Splash ekraninin acilista bekleme suresi. */
        const val SPLASH_DURATION_MS = 3_000L
        const val SPLASH_ANIMATION_SIZE_DP = 340

        /** Ust bildirim banner'i. */
        const val BANNER_VISIBLE_DURATION_MS = 2_500L
        const val BANNER_SLIDE_DURATION_MS = 300

        /** Aramada her tusa basista istek atmamak icin bekleme. */
        const val SEARCH_DEBOUNCE_MS = 350L

        /**
         * StateFlow'larin abone kalmadiktan sonra hayatta kalma suresi.
         * Konfigurasyon degisiminde akisi bosuna yeniden baslatmamak icin.
         */
        const val STATE_FLOW_TIMEOUT_MS = 5_000L

        /** Arama ekranindaki one cikan kategori seridinde gosterilecek kayit sayisi. */
        const val FEATURED_ROW_ITEM_COUNT = 10

        /** Icerik izgarasi. */
        const val GRID_COLUMNS = 3
        const val COVER_ASPECT_RATIO = 2f / 3f

    }

    /** Google Books API ve HTTP istemcisi. */
    object Network {
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
        const val PAGE_SIZE = 20

        /** Google Books maxResults icin 40'in ustunu kabul etmiyor. */
        const val MAX_PAGE_SIZE = 40

        /** Feed siralamasi. Google Books "relevance" veya "newest" kabul ediyor. */
        const val FEED_ORDER_BY = "relevance"

        /**
         * Sonuclari bu dildeki baskilarla sinirlar (ISO 639-1).
         *
         * Google Books aciklamayi cevirmiyor; aciklama hangi baskiya aitse
         * o dilde geliyor. Ingilizce aciklama icin tek yol, sonuclarin
         * kendisini Ingilizce baskilarla sinirlamak.
         *
         * Bos birakilirsa dil filtresi uygulanmaz.
         */
        const val LANG_RESTRICT = "en"

        const val CONNECT_TIMEOUT_SECONDS = 15L
        const val READ_TIMEOUT_SECONDS = 20L

        /** Google Books araliklarla 503 donebiliyor; 5xx icin tekrar denenir. */
        const val RETRY_MAX_ATTEMPTS = 3
        const val RETRY_BACKOFF_MS = 400L
    }

    /**
     * Claude API — kitap adindan Ingilizce ozet uretimi.
     *
     * Anahtar bos birakilirsa hicbir istek atilmaz ve uygulama Google Books'un
     * kendi aciklamasina duser; repoyu anahtarsiz klonlayan biri de calistirabilir.
     */
    object Anthropic {
        const val BASE_URL = "https://api.anthropic.com/"

        /** Zorunlu surum basligi; model surumunden bagimsiz. */
        const val API_VERSION = "2023-06-01"

        const val MODEL = "claude-opus-5"

        /**
         * Ust sinir, hedef degil — uretilen token kadar odenir. Dusuk tutmak
         * riskli: bu modelde dusunme de ayni butceden harcaniyor ve cevap
         * yarida kesilebiliyor.
         */
        const val MAX_TOKENS = 16_000

        /** Kisa bir ozet icin dusuk efor yeterli; gecikmeyi belirgin dusuruyor. */
        const val EFFORT = "low"

        /** Model dusunup yazdigi icin Google Books'tan uzun surebiliyor. */
        const val READ_TIMEOUT_SECONDS = 60L

        const val SUMMARY_WORD_TARGET = 120
    }

    /** Ucretsiz plan siniri. */
    object Premium {
        const val FREE_DAILY_LIMIT = 3
    }

    /** Yerel depolama ve onbellek. */
    object Storage {
        const val DATABASE_NAME = "nativeminds.db"
        const val PREFERENCES_NAME = "user_preferences"

        /** Favoriler yerelden okundugu icin ag sayfa boyutundan bagimsiz. */
        const val FAVORITES_PAGE_SIZE = 20

        /**
         * Feed onbellegi bu sureden tazeyse acilista ag'a hic gidilmez,
         * ekran dogrudan Room'dan dolar. Kullanici asagi cekerek her an
         * yenileyebiliyor, bu yuzden uzun tutulabilir.
         */
        const val FEED_CACHE_TIMEOUT_MS = 60L * 60 * 1000

        const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_DISK_CACHE_BYTES = 50L * 1024 * 1024
    }

    /** Periyodik favori tazeleme isi. */
    object Sync {
        const val WORK_NAME = "sync_worker"
        const val INTERVAL_DAYS = 1L
        const val MAX_RETRIES = 3
    }
}
