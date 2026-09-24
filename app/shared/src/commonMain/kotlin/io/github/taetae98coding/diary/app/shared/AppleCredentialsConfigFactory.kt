package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsConfig

internal fun appleCredentialsConfig(clientId: String): AppleCredentialsConfig =
    AppleCredentialsConfig(
        clientId = clientId,
        callbackUrl = "${BuildKonfig.SUPABASE_URL}/functions/v1/$APPLE_SIGN_IN_CALLBACK_FUNCTION",
    )

private const val APPLE_SIGN_IN_CALLBACK_FUNCTION = "v1-session-apple-callback"
