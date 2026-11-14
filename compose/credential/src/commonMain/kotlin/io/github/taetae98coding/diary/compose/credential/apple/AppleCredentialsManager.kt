package io.github.taetae98coding.diary.compose.credential.apple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
public interface AppleCredentialsManager {
    public suspend fun signIn(): AppleSignInResult
}

public interface AppleCredentialsManagerFactory {
    @Composable
    public fun rememberAppleCredentialsManager(): AppleCredentialsManager
}
