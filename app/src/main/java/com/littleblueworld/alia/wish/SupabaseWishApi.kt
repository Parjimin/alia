package com.littleblueworld.alia.wish

import com.littleblueworld.alia.app.AppConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseWishApi(
    private val config: AppConfig,
    private val httpClient: WishHttpClient = UrlConnectionWishHttpClient(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WishApi {

    override suspend fun insert(submission: WishSubmission): WishApiResult = withContext(ioDispatcher) {
        val endpoint = endpointOrNull() ?: return@withContext WishApiResult.PermanentFailure
        if (submission.message.isBlank() || submission.message.length > MAX_MESSAGE_LENGTH) {
            return@withContext WishApiResult.PermanentFailure
        }

        val request = WishHttpRequest(
            url = endpoint,
            headers = mapOf(
                "apikey" to config.supabasePublishableKey,
                "Content-Type" to "application/json",
                "Prefer" to "return=minimal",
            ),
            body = """{"request_id":"${jsonEscape(submission.requestId)}","message":"${jsonEscape(submission.message)}"}""",
        )

        val response = try {
            httpClient.post(request)
        } catch (_: IOException) {
            return@withContext WishApiResult.TemporaryFailure(TemporaryFailureKind.NETWORK)
        }

        when {
            response.statusCode in 200..299 -> WishApiResult.Inserted
            response.statusCode == HTTP_CONFLICT && response.postgresCode() == UNIQUE_VIOLATION -> {
                WishApiResult.Duplicate
            }
            response.statusCode in TEMPORARY_STATUS_CODES || response.statusCode >= 500 -> {
                WishApiResult.TemporaryFailure(TemporaryFailureKind.SERVER)
            }
            else -> WishApiResult.PermanentFailure
        }
    }

    private fun endpointOrNull(): String? {
        val baseUrl = config.supabaseUrl.trim().trimEnd('/')
        val key = config.supabasePublishableKey.trim()
        if (!baseUrl.startsWith("https://") || !key.startsWith("sb_publishable_")) return null
        return "$baseUrl/rest/v1/wishes"
    }

    private fun WishHttpResponse.postgresCode(): String? = POSTGRES_CODE_PATTERN
        .find(body.take(MAX_ERROR_BODY_LENGTH))
        ?.groupValues
        ?.getOrNull(1)

    private fun jsonEscape(value: String): String = buildString(value.length + 16) {
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u")
                    append(character.code.toString(16).padStart(4, '0'))
                } else {
                    append(character)
                }
            }
        }
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 500
        const val MAX_ERROR_BODY_LENGTH = 4_096
        const val HTTP_CONFLICT = 409
        const val UNIQUE_VIOLATION = "23505"
        val TEMPORARY_STATUS_CODES = setOf(408, 425, 429)
        val POSTGRES_CODE_PATTERN = Regex("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
    }
}

data class WishHttpRequest(
    val url: String,
    val headers: Map<String, String>,
    val body: String,
)

data class WishHttpResponse(
    val statusCode: Int,
    val body: String,
)

fun interface WishHttpClient {
    @Throws(IOException::class)
    fun post(request: WishHttpRequest): WishHttpResponse
}

private class UrlConnectionWishHttpClient : WishHttpClient {
    override fun post(request: WishHttpRequest): WishHttpResponse {
        val connection = URL(request.url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.instanceFollowRedirects = false
            for ((name, value) in request.headers) {
                connection.setRequestProperty(name, value)
            }
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(request.body)
            }
            val statusCode = connection.responseCode
            val responseStream = if (statusCode >= 400) connection.errorStream else connection.inputStream
            val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                val buffer = CharArray(MAX_RESPONSE_BODY_LENGTH)
                val count = reader.read(buffer)
                if (count > 0) String(buffer, 0, count) else ""
            }.orEmpty()
            WishHttpResponse(statusCode = statusCode, body = body)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 10_000
        const val MAX_RESPONSE_BODY_LENGTH = 4_096
    }
}
