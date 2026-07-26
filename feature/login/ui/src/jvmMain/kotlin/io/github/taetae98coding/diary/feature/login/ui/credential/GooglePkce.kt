package io.github.taetae98coding.diary.feature.login.ui.credential

internal data class GooglePkce(
    val codeVerifier: String,
    val codeChallenge: String,
)
