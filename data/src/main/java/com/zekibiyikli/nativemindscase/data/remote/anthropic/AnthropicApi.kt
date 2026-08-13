package com.zekibiyikli.nativemindscase.data.remote.anthropic

import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.MessageRequestDto
import com.zekibiyikli.nativemindscase.data.remote.anthropic.dto.MessageResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Claude Messages API.
 *
 * Kimlik ve surum basliklari [AnthropicAuthInterceptor] tarafindan ekleniyor.
 */
interface AnthropicApi {

    @POST("v1/messages")
    suspend fun createMessage(@Body request: MessageRequestDto): MessageResponseDto
}
