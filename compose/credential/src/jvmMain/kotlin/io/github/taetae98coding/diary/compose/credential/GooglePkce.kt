package io.github.taetae98coding.diary.compose.credential

internal data class GooglePkce(
    val codeVerifier: String,
    val codeChallenge: String,
)
