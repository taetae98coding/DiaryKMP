package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential

@Stable
internal interface GoogleCredentialsManager {
    val isSignInEndDetectable: Boolean

    suspend fun signIn(): GoogleCredential
}

@Composable
internal expect fun rememberGoogleCredentialsManager(): GoogleCredentialsManager
