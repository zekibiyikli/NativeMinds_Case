package com.zekibiyikli.nativemindscase.data.remote

import com.zekibiyikli.nativemindscase.core.result.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import retrofit2.HttpException
import java.io.IOException

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_TOO_MANY_REQUESTS = 429

/** Ag katmani istisnalarini sunum katmaninin anladigi turlere cevirir. */
fun Throwable.toAppException(): AppException = when {
    this is AppException -> this
    this is HttpException && code() == HTTP_TOO_MANY_REQUESTS -> AppException.RateLimited(this)
    this is HttpException && code() == HTTP_BAD_REQUEST -> AppException.InvalidApiKey(this)
    this is HttpException -> AppException.Server(code = code(), cause = this)
    this is IOException -> AppException.NoConnection(this)
    else -> AppException.Unknown(this)
}

/** Akistaki hatalari [AppException]'a cevirir; akisi oldurmez, yeniden firlatir. */
fun <T> Flow<T>.mapErrors(): Flow<T> = catch { throw it.toAppException() }
