package com.mianu.paymentapp.data

import retrofit2.HttpException
import java.io.IOException

/**
 * Result wrapper for network calls.
 *
 * Screens previously swallowed exceptions in a bare `catch { }`, which meant a misconfigured host
 * and a rejected card looked identical to the user. [Failure] keeps the distinction so the UI can
 * say something useful.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

sealed interface ApiError {
    val message: String

    /** No route to the server — usually the backend isn't running, or BASE_URL points elsewhere. */
    data class Network(override val message: String = "Can't reach the server. Check your connection.") : ApiError

    /** Server answered with a non-2xx status. */
    data class Http(val code: Int, override val message: String) : ApiError

    data class Unknown(override val message: String) : ApiError
}

/** True when the failure is worth retrying as-is. */
val ApiError.isRetryable: Boolean
    get() = this is ApiError.Network || (this is ApiError.Http && code >= 500)

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onFailure(action: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(error)
    return this
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

/**
 * Runs [block], mapping thrown exceptions onto [ApiError].
 *
 * HTTP failures are translated to language an operator running a meal line can act on, rather than
 * a status code. The raw detail is kept for unmapped codes so nothing is lost.
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: IOException) {
    ApiResult.Failure(ApiError.Network())
} catch (e: HttpException) {
    val detail = runCatching {
        val raw = e.response()?.errorBody()?.string()
        if (!raw.isNullOrBlank()) {
            org.json.JSONObject(raw).optString("detail")
        } else null
    }.getOrNull()?.takeIf { it.isNotBlank() }

    val friendly = detail ?: when (e.code()) {
        400 -> "That request was rejected. Check the values and try again."
        401 -> "Invalid username or password."
        403 -> "You don't have permission to do that."
        404 -> "Not found — the card or record isn't registered."
        409 -> "That conflicts with an existing record."
        422 -> "Some details are missing or invalid."
        in 500..599 -> "The server had a problem. Try again in a moment."
        else -> e.message().ifBlank { "Request failed (${e.code()})." }
    }
    ApiResult.Failure(ApiError.Http(e.code(), friendly))
} catch (e: Exception) {
    ApiResult.Failure(ApiError.Unknown(e.message ?: "Something went wrong."))
}
