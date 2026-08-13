package com.zekibiyikli.nativemindscase.data.remote.anthropic.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MessageRequestDto(
    @SerialName("model") val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    @SerialName("system") val system: String,
    @SerialName("messages") val messages: List<MessageParamDto>,
    @SerialName("output_config") val outputConfig: OutputConfigDto? = null
)

@Serializable
data class MessageParamDto(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class OutputConfigDto(
    /** low | medium | high | xhigh | max */
    @SerialName("effort") val effort: String? = null,
    @SerialName("format") val format: OutputFormatDto? = null
)

/**
 * [type] bilerek varsayilan deger almiyor: kotlinx varsayilan ayarda
 * (encodeDefaults = false) default'a esit alanlari JSON'a yazmaz, o durumda
 * "type" hic gitmez ve istek gecersiz olur.
 */
@Serializable
data class OutputFormatDto(
    @SerialName("type") val type: String,
    @SerialName("schema") val schema: JsonObject
) {
    companion object {
        const val TYPE_JSON_SCHEMA = "json_schema"
    }
}

@Serializable
data class MessageResponseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("model") val model: String? = null,
    /**
     * end_turn | max_tokens | refusal | ...
     * "refusal" geldiginde content bos ya da yarim olabilir; icerigi
     * okumadan once bu alan kontrol edilmeli.
     */
    @SerialName("stop_reason") val stopReason: String? = null,
    @SerialName("content") val content: List<ContentBlockDto> = emptyList()
)

/**
 * Yanit birden fazla blok icerebilir. Bu modelde dusunme varsayilan olarak
 * acik oldugu icin metinden once "thinking" bloklari gelebiliyor — bu yuzden
 * content[0] okunmaz, type'a gore filtrelenir.
 */
@Serializable
data class ContentBlockDto(
    @SerialName("type") val type: String,
    @SerialName("text") val text: String? = null
)
