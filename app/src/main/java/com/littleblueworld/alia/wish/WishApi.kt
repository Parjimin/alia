package com.littleblueworld.alia.wish

data class WishSubmission(
    val requestId: String,
    val message: String,
)

fun interface WishApi {
    suspend fun insert(submission: WishSubmission): WishApiResult
}

sealed interface WishApiResult {
    data object Inserted : WishApiResult
    data object Duplicate : WishApiResult
    data class TemporaryFailure(val kind: TemporaryFailureKind) : WishApiResult
    data object PermanentFailure : WishApiResult
}

enum class TemporaryFailureKind {
    NETWORK,
    SERVER,
}
