package io.github.taetae98coding.diary.compose.credential.apple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization

@Stable
public interface AppleCredentialsManager {
    public suspend fun signIn(): AppleSignInResult
}

public suspend fun AppleCredentialsManager.authorize(): AppleAuthorization = signIn().authorization ?: throw AppleCredentialsException(message = "no authorization code")

@Composable
public expect fun rememberAppleCredentialsManager(): AppleCredentialsManager
