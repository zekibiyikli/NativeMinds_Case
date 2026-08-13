package com.zekibiyikli.nativemindscase.data.content

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.di.IoDispatcher
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.core.result.asOutcome
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import com.zekibiyikli.nativemindscase.data.local.AppDatabase
import com.zekibiyikli.nativemindscase.data.local.CachedBookDao
import com.zekibiyikli.nativemindscase.data.local.FavoriteBookDao
import com.zekibiyikli.nativemindscase.data.local.FavoriteBookEntity
import com.zekibiyikli.nativemindscase.data.local.FeedRemoteKeyDao
import com.zekibiyikli.nativemindscase.data.local.toContentItem as cachedToContentItem
import com.zekibiyikli.nativemindscase.data.remote.GoogleBooksApi
import com.zekibiyikli.nativemindscase.data.remote.SearchQuery
import com.zekibiyikli.nativemindscase.data.remote.mapErrors
import com.zekibiyikli.nativemindscase.data.remote.toAppException
import com.zekibiyikli.nativemindscase.data.remote.toContentItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tek dogruluk kaynagi Room.
 *
 * Feed ve favoriler her zaman yerelden okunuyor; ag yalnizca onbellegi
 * tazeliyor. Boylece internet olmadan da son gorulen icerik listeleniyor.
 * Arama bunun disinda: sorgu sonucu onbelleklenmiyor, ag gerektiriyor.
 */
@Singleton
class GoogleBooksContentRepository @Inject constructor(
    private val api: GoogleBooksApi,
    private val database: AppDatabase,
    private val favoriteBookDao: FavoriteBookDao,
    private val cachedBookDao: CachedBookDao,
    private val feedRemoteKeyDao: FeedRemoteKeyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentRepository {

    override fun subjects(): List<Subject> = Subjects.all

    @OptIn(ExperimentalPagingApi::class)
    override fun pagedFeed(subjectId: String): Flow<PagingData<ContentItem>> = Pager(
        config = PagingConfig(
            pageSize = AppConfig.Network.PAGE_SIZE,
            // Varsayilan ilk yukleme 3x sayfa isterdi; Google Books 40 ile
            // sinirli oldugu icin ilk sayfayi da normal boyutta tutuyoruz.
            initialLoadSize = AppConfig.Network.PAGE_SIZE,
            prefetchDistance = AppConfig.Network.PAGE_SIZE / 2,
            enablePlaceholders = false
        ),
        remoteMediator = FeedRemoteMediator(
            subjectId = subjectId,
            api = api,
            database = database,
            cachedBookDao = cachedBookDao,
            remoteKeyDao = feedRemoteKeyDao
        ),
        // Liste ag'dan degil Room'dan okunuyor; mediator sadece besliyor.
        pagingSourceFactory = { cachedBookDao.pagingSource(subjectId) }
    ).flow.map { pagingData -> pagingData.map { it.cachedToContentItem() } }

    override fun search(query: String?, subjectId: String?): Flow<Outcome<List<ContentItem>>> =
        volumesFlow(query = query, subjectId = subjectId, orderBy = null)

    /**
     * Once yerel kopya gosteriliyor, sonra ag'dan tazeleniyor.
     *
     * Ag hata verdiginde yerel kopya varsa hata yutuluyor: kullanicinin
     * cevrimdisi acabildigi bir kitapta hata ekrani gostermek anlamsiz.
     * Yerel kopya yoksa hata normal sekilde yukari gidiyor.
     */
    override fun observeItem(id: String): Flow<Outcome<ContentItem>> = flow {
        val cached = cachedBookDao.findById(id)?.cachedToContentItem()
            ?: favoriteBookDao.find(id)?.toContentItem()
        if (cached != null) emit(Outcome.Success(cached))

        runCatching { api.getVolume(volumeId = id).toContentItem() }
            .onSuccess { emit(Outcome.Success(it)) }
            .onFailure { if (cached == null) emit(Outcome.Failure(it.toAppException())) }
    }.onStart { emit(Outcome.Loading) }.flowOn(ioDispatcher)

    override fun popularSubjects(): List<Subject> = Subjects.popular

    override fun pagedFavorites(): Flow<PagingData<ContentItem>> = Pager(
        config = PagingConfig(
            pageSize = AppConfig.Storage.FAVORITES_PAGE_SIZE,
            enablePlaceholders = false
        ),
        // Room PagingSource'u tablo her degistiginde gecersiz kilar,
        // dolayisiyla favori eklenince/cikinca liste kendini yeniler.
        pagingSourceFactory = favoriteBookDao::pagingSource
    ).flow.map { pagingData -> pagingData.map { it.toContentItem() } }

    override fun observeFavoriteIds(): Flow<Set<String>> = favoriteBookDao.observeIds()
        .map(List<String>::toSet)

    override suspend fun toggleFavorite(item: ContentItem) = withContext(ioDispatcher) {
        if (favoriteBookDao.exists(item.id)) {
            favoriteBookDao.deleteById(item.id)
        } else {
            favoriteBookDao.upsert(item.toEntity())
        }
    }

    override suspend fun refreshFavorites() = withContext(ioDispatcher) {
        favoriteBookDao.observeAll().first().forEach { entity ->
            // Tek kitabin hatasi tum sync'i dusurmesin.
            runCatching { api.getVolume(volumeId = entity.id).toContentItem() }
                .onSuccess { fresh ->
                    favoriteBookDao.upsert(fresh.toEntity().copy(savedAt = entity.savedAt))
                }
        }
    }

    private fun volumesFlow(
        query: String?,
        subjectId: String?,
        orderBy: String?
    ): Flow<Outcome<List<ContentItem>>> = flow {
        val q = SearchQuery.build(query = query, subjectId = subjectId)
        if (q.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val response = api.searchVolumes(
            query = q,
            orderBy = orderBy,
            // Aciklamanin dili baskinin dili; Ingilizce ozet icin sonuclari
            // Ingilizce baskilarla sinirliyoruz.
            langRestrict = AppConfig.Network.LANG_RESTRICT.takeIf { it.isNotBlank() }
        )
        // Kapaksiz/basliksiz kayitlar izgarada bos kart olarak gorunuyor, eleniyor.
        emit(
            response.items.orEmpty()
                .map { it.toContentItem() }
                .filter { it.title.isNotBlank() }
        )
    }.flowOn(ioDispatcher).mapErrors().asOutcome()
}

private fun ContentItem.toEntity() = FavoriteBookEntity(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    pageCount = pageCount,
    publishedDate = publishedDate,
    savedAt = System.currentTimeMillis()
)

private fun FavoriteBookEntity.toContentItem() = ContentItem(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    pageCount = pageCount,
    publishedDate = publishedDate,
    // Kategori favori kaydinda tutulmuyor; detayda API'den zaten geliyor.
    categories = emptyList()
)
