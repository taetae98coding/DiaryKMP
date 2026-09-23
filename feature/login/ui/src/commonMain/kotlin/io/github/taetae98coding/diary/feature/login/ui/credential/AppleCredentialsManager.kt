package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential

@Stable
internal interface AppleCredentialsManager {
    suspend fun signIn(): AppleCredential
}

@Composable
internal expect fun rememberAppleCredentialsManager(): AppleCredentialsManager

internal class UnsupportedAppleCredentialsManager : AppleCredentialsManager {
    override suspend fun signIn(): AppleCredential = throw AppleCredentialsException(message = "Apple sign-in is not supported on this platform")
}
