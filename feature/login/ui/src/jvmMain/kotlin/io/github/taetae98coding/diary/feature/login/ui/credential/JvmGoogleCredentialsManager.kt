package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
internal actual fun rememberGoogleCredentialsManager(): GoogleCredentialsManager {
    val clientId = koinInject<String>(qualifier = named<GoogleCredentialsClientId>())
    val coroutineDispatcher = koinInject<CoroutineDispatcher>(qualifier = named<CredentialsDispatcher>())

    return remember(clientId, coroutineDispatcher) {
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
    // 시스템 브라우저 창이 닫혀도 앱에는 아무 신호가 오지 않는다.
    override val isSignInEndDetectable: Boolean = false

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
