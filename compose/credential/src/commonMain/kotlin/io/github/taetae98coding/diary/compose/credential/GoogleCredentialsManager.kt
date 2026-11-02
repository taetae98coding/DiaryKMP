package io.github.taetae98coding.diary.compose.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential

@Stable
public interface GoogleCredentialsManager {
    public val isSignInEndDetectable: Boolean

    public suspend fun signIn(): GoogleCredential
}

@Composable
public expect fun rememberGoogleCredentialsManager(): GoogleCredentialsManager
