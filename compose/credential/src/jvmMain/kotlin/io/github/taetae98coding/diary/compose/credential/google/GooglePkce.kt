package io.github.taetae98coding.diary.compose.credential.google

internal data class GooglePkce(
    val codeVerifier: String,
    val codeChallenge: String,
)
