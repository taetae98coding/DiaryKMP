package io.github.taetae98coding.diary.compose.credential.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.credential.LoopbackRedirectReceiver
import io.github.taetae98coding.diary.compose.credential.openInSystemBrowser
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible

public class JvmGoogleCredentialsManagerFactory(
    private val clientId: String,
    private val coroutineDispatcher: CoroutineDispatcher,
) : GoogleCredentialsManagerFactory {
    @Composable
    override fun rememberGoogleCredentialsManager(): GoogleCredentialsManager =
        remember(clientId, coroutineDispatcher) {
            JvmGoogleCredentialsManager(
                attemptFactory = GoogleAuthorizationCodeAttemptFactory(clientId = clientId),
                coroutineDispatcher = coroutineDispatcher,
            )
        }
}

private class JvmGoogleCredentialsManager(
    private val attemptFactory: GoogleAuthorizationCodeAttemptFactory,
    private val coroutineDispatcher: CoroutineDispatcher,
) : GoogleCredentialsManager {
    override suspend fun signIn(): GoogleCredential =
        try {
            runInterruptible(coroutineDispatcher) {
                LoopbackRedirectReceiver().use { receiver ->
                    val attempt = attemptFactory.create(redirectUri = receiver.redirectUri)

                    openInSystemBrowser(attempt.authorizationUrl)
                    attempt.toCredential(parameters = receiver.waitForParameters())
                }
            }
        } catch (throwable: Throwable) {
            throw throwable.toGoogleCredentialsFailure()
        }
}
