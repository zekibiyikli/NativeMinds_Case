package com.zekibiyikli.nativemindscase.data.content

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.local.AppDatabase
import com.zekibiyikli.nativemindscase.data.local.CachedBookDao
import com.zekibiyikli.nativemindscase.data.local.CachedBookEntity
import com.zekibiyikli.nativemindscase.data.local.FeedRemoteKeyDao
import com.zekibiyikli.nativemindscase.data.local.FeedRemoteKeyEntity
import com.zekibiyikli.nativemindscase.data.local.toCachedEntity
import com.zekibiyikli.nativemindscase.data.remote.GoogleBooksApi
import com.zekibiyikli.nativemindscase.data.remote.SearchQuery
import com.zekibiyikli.nativemindscase.data.remote.toAppException
import com.zekibiyikli.nativemindscase.data.remote.toContentItem
import kotlinx.coroutines.CancellationException

/**
 * Ag ile Room arasindaki koprü.
 *
 * Liste her zaman Room'dan okunuyor; bu sinif sadece onbellegi dolduruyor.
 * Ag hatasi verdiginde Paging bunu [RemoteMediator.MediatorResult.Error]
 * olarak bildirir ama Room'daki kayitlar ekranda kalmaya devam eder —
 * cevrimdisi calismanin esasi bu.
 */
@OptIn(ExperimentalPagingApi::class)
class FeedRemoteMediator(
    private val subjectId: String,
    private val api: GoogleBooksApi,
    private val database: AppDatabase,
    private val cachedBookDao: CachedBookDao,
    private val remoteKeyDao: FeedRemoteKeyDao
) : RemoteMediator<Int, CachedBookEntity>() {

    /**
     * Onbellek tazeyse acilista ag'a gidilmiyor: uygulama aninda yerelden
     * doluyor. Suresi gecmisse normal yenileme akisi calisiyor.
     */
    override suspend fun initialize(): InitializeAction {
        val key = remoteKeyDao.find(subjectId) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = System.currentTimeMillis() - key.refreshedAt
        return if (age < AppConfig.Storage.FEED_CACHE_TIMEOUT_MS) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CachedBookEntity>
    ): MediatorResult {
        val startIndex = when (loadType) {
            LoadType.REFRESH -> 0

            // Google Books yalnizca ileri dogru sayfalaniyor.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val key = remoteKeyDao.find(subjectId)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                if (key.endReached) return MediatorResult.Success(endOfPaginationReached = true)
                key.nextStartIndex
            }
        }

        val query = SearchQuery.build(query = null, subjectId = subjectId)
        if (query.isBlank()) return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val response = api.searchVolumes(
                query = query,
                // Google Books 40'in ustunu reddediyor.
                maxResults = state.config.pageSize.coerceAtMost(AppConfig.Network.MAX_PAGE_SIZE),
                startIndex = startIndex,
                orderBy = AppConfig.Network.FEED_ORDER_BY,
                langRestrict = AppConfig.Network.LANG_RESTRICT.takeIf { it.isNotBlank() }
            )

            val received = response.items.orEmpty()
            // Basliksiz kayitlar izgarada bos kart olarak gorunuyor.
            val items = received.map { it.toContentItem() }.filter { it.title.isNotBlank() }
            val endReached = received.isEmpty() ||
                startIndex + received.size >= response.totalItems

            database.withTransaction {
                // Yenileme tam degisim: eski sayfalar silinip bastan yaziliyor.
                if (loadType == LoadType.REFRESH) cachedBookDao.deleteBySubject(subjectId)

                val nextPosition = (cachedBookDao.maxPosition(subjectId) ?: -1) + 1
                cachedBookDao.insertAll(
                    items.mapIndexed { offset, item ->
                        item.toCachedEntity(subjectId = subjectId, position = nextPosition + offset)
                    }
                )
                remoteKeyDao.upsert(
                    FeedRemoteKeyEntity(
                        subjectId = subjectId,
                        // Filtrelenmis degil ham sayi kadar ilerlemeli, yoksa
                        // API tarafinda kayitlar atlanir ya da tekrar eder.
                        nextStartIndex = startIndex + received.size,
                        endReached = endReached,
                        refreshedAt = System.currentTimeMillis()
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (cancellation: CancellationException) {
            // Iptal bir hata degil; Error'a cevrilirse Paging yeniden denemez.
            throw cancellation
        } catch (throwable: Throwable) {
            MediatorResult.Error(throwable.toAppException())
        }
    }
}
