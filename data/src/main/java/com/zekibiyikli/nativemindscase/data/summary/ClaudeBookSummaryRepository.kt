package com.zekibiyikli.nativemindscase.data.summary

import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.analytics.SummarySource
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.crashlytics.CrashReporter
import com.zekibiyikli.nativemindscase.core.di.IoDispatcher
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.core.result.asOutcome
import com.zekibiyikli.nativemindscase.data.BuildConfig
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.local.BookSummaryDao
import com.zekibiyikli.nativemindscase.data.local.BookSummaryEntity
import com.zekibiyikli.nativemindscase.data.remote.anthropic.AnthropicApi
import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.MessageParamDto
import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.MessageRequestDto
import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.OutputConfigDto
import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.OutputFormatDto
import com.zekibiyikli.nativemindscase.data.remote.mapErrors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modelin donmesini istedigimiz yapi.
 *
 * [known] bilerek var: modelden bilmedigi bir kitap icin konu uydurmak yerine
 * bilmedigini soylemesini istiyoruz. false geldiginde ozet gosterilmiyor,
 * Google Books aciklamasina dusuluyor.
 */
@Serializable
private data class SummaryPayload(
    @SerialName("known") val known: Boolean,
    @SerialName("summary") val summary: String = ""
)

@Singleton
class ClaudeBookSummaryRepository @Inject constructor(
    private val api: AnthropicApi,
    private val summaryDao: BookSummaryDao,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookSummaryRepository {

    override val isEnabled: Boolean = BuildConfig.ANTHROPIC_API_KEY.isNotBlank()

    override fun observeSummary(item: ContentItem): Flow<Outcome<String?>> = flow {
        if (!isEnabled) {
            logShown(SummarySource.DISABLED)
            emit(null)
            return@flow
        }

        val cached = summaryDao.find(item.id)
        if (cached != null) {
            // Onbellekten gelenler ayri raporlaniyor: kac istegin gercekten
            // modele gittigini, yani onbellegin ise yarayip yaramadigini gosterir.
            logShown(if (cached.summary != null) SummarySource.CACHE else SummarySource.NONE)
            emit(cached.summary)
            return@flow
        }

        val generated = requestSummary(item)
        logShown(if (generated != null) SummarySource.GENERATED else SummarySource.NONE)
        // Bilinmeyen kitaplar da yaziliyor: aksi halde her acilista tekrar sorulurdu.
        summaryDao.upsert(
            BookSummaryEntity(
                id = item.id,
                summary = generated,
                generatedAt = System.currentTimeMillis()
            )
        )
        emit(generated)
    }.flowOn(ioDispatcher).mapErrors().asOutcome()

    private suspend fun requestSummary(item: ContentItem): String? {
        val response = api.createMessage(
            MessageRequestDto(
                model = AppConfig.Anthropic.MODEL,
                maxTokens = AppConfig.Anthropic.MAX_TOKENS,
                system = SYSTEM_PROMPT,
                messages = listOf(
                    MessageParamDto(role = "user", content = userPrompt(item))
                ),
                outputConfig = OutputConfigDto(
                    effort = AppConfig.Anthropic.EFFORT,
                    format = OutputFormatDto(
                        type = OutputFormatDto.TYPE_JSON_SCHEMA,
                        schema = SUMMARY_SCHEMA
                    )
                )
            )
        )

        // Guvenlik siniflandiricisi istegi reddedebiliyor; bu durumda content
        // bos ya da yarim gelir, okumadan once kontrol ediliyor.
        if (response.stopReason == STOP_REASON_REFUSAL) {
            CrashReporter.log("Claude ozet istegi reddedildi: ${item.id}")
            return null
        }

        // Dusunme varsayilan olarak acik; metin blogu ilk blok olmayabilir.
        val text = response.content
            .firstOrNull { it.type == CONTENT_TYPE_TEXT }
            ?.text
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val payload = runCatching { json.decodeFromString<SummaryPayload>(text) }
            .getOrElse {
                CrashReporter.recordException(it, mapOf("volumeId" to item.id))
                return null
            }

        return payload.summary.trim().takeIf { payload.known && it.isNotBlank() }
    }

    private fun logShown(source: String) {
        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.SUMMARY_SHOWN,
            params = mapOf(AnalyticsParam.SOURCE to source)
        )
    }

    private fun userPrompt(item: ContentItem): String = buildString {
        append("Book title: ").append(item.title)
        if (item.author.isNotBlank()) {
            append("\nAuthor: ").append(item.author)
        }
        item.publishedDate?.let { append("\nPublished: ").append(it) }
        item.categories.firstOrNull()?.let { append("\nCategory: ").append(it) }
    }

    private companion object {
        const val STOP_REASON_REFUSAL = "refusal"
        const val CONTENT_TYPE_TEXT = "text"

        /**
         * Uydurmayi engelleyen kisim "known" alani: modele bilmedigi kitapta
         * tahmin yurutmemesini soyluyoruz.
         */
        val SYSTEM_PROMPT = """
            You write short English summaries of books for a reading app.

            You will be given a book's title and, when available, its author,
            publication year and category. Identify that specific book and
            summarize what it is about in about ${AppConfig.Anthropic.SUMMARY_WORD_TARGET} words.

            Rules:
            - Write in English, in plain prose. No headings, lists or markdown.
            - Describe the book's subject, themes and why a reader might pick it
              up. Do not spoil the ending.
            - If you are not confident you know this specific book, set "known"
              to false and leave "summary" empty. Never invent a plot, characters
              or claims about a book you do not recognise. A missing summary is
              far better than a fabricated one.
            - Titles can be ambiguous; use the author and year to disambiguate.
              If they do not identify a single book you know, set "known" to false.
        """.trimIndent()

        val SUMMARY_SCHEMA: JsonObject = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("known") {
                    put("type", "boolean")
                    put("description", "True only if you recognise this specific book.")
                }
                putJsonObject("summary") {
                    put("type", "string")
                    put("description", "The English summary, or an empty string when known is false.")
                }
            }
            put(
                "required",
                buildJsonArray {
                    add("known")
                    add("summary")
                }
            )
            put("additionalProperties", false)
        }
    }
}
