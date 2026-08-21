package com.littleblueworld.alia.wish

import com.littleblueworld.alia.app.AppConfig
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseWishApiTest {
    @Test
    fun `insert uses exact endpoint headers and minimal JSON payload`() = runTest {
        var capturedRequest: WishHttpRequest? = null
        val api = apiWithClient { request ->
            capturedRequest = request
            WishHttpResponse(statusCode = 201, body = "")
        }

        val result = api.insert(
            WishSubmission(
                requestId = REQUEST_ID,
                message = "a tiny \"wish\"\nplease",
            ),
        )

        assertEquals(WishApiResult.Inserted, result)
        assertEquals("https://alia-test.supabase.co/rest/v1/wishes", capturedRequest?.url)
        assertEquals(
            mapOf(
                "apikey" to PUBLISHABLE_KEY,
                "Content-Type" to "application/json",
                "Prefer" to "return=minimal",
            ),
            capturedRequest?.headers,
        )
        assertEquals(
            """{"request_id":"$REQUEST_ID","message":"a tiny \"wish\"\nplease"}""",
            capturedRequest?.body,
        )
        assertTrue(capturedRequest?.headers?.containsKey("Authorization") == false)
    }

    @Test
    fun `only confirmed postgres unique violation is duplicate success`() = runTest {
        val duplicate = apiWithResponse(409, """{"code":"23505","message":"duplicate"}""")
            .insert(WishSubmission(REQUEST_ID, "wish"))
        val genericConflict = apiWithResponse(409, """{"code":"other"}""")
            .insert(WishSubmission(REQUEST_ID, "wish"))

        assertEquals(WishApiResult.Duplicate, duplicate)
        assertEquals(WishApiResult.PermanentFailure, genericConflict)
    }

    @Test
    fun `network and server failures remain retryable classifications`() = runTest {
        val networkApi = apiWithClient { throw IOException("offline") }
        val serverApi = apiWithResponse(503, "unavailable")

        assertEquals(
            WishApiResult.TemporaryFailure(TemporaryFailureKind.NETWORK),
            networkApi.insert(WishSubmission(REQUEST_ID, "wish")),
        )
        assertEquals(
            WishApiResult.TemporaryFailure(TemporaryFailureKind.SERVER),
            serverApi.insert(WishSubmission(REQUEST_ID, "wish")),
        )
    }

    @Test
    fun `blank configuration and invalid payload never call transport`() = runTest {
        var calls = 0
        val api = SupabaseWishApi(
            config = AppConfig("", ""),
            httpClient = WishHttpClient {
                calls += 1
                WishHttpResponse(201, "")
            },
            ioDispatcher = Dispatchers.Unconfined,
        )

        assertEquals(
            WishApiResult.PermanentFailure,
            api.insert(WishSubmission(REQUEST_ID, "wish")),
        )
        assertEquals(0, calls)
    }

    private fun apiWithResponse(statusCode: Int, body: String) = apiWithClient {
        WishHttpResponse(statusCode, body)
    }

    private fun apiWithClient(client: WishHttpClient) = SupabaseWishApi(
        config = AppConfig("https://alia-test.supabase.co/", PUBLISHABLE_KEY),
        httpClient = client,
        ioDispatcher = Dispatchers.Unconfined,
    )

    private companion object {
        const val REQUEST_ID = "3f67e80a-3912-4f5d-a1e7-c321bfecbd17"
        const val PUBLISHABLE_KEY = "sb_publishable_test_value"
    }
}
