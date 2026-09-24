package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager {
    val config = koinInject<AppleCredentialsConfig>()
    val coroutineDispatcher = koinInject<CoroutineDispatcher>(qualifier = named<CredentialsDispatcher>())

    return remember(config, coroutineDispatcher) {
        JvmAppleCredentialsManager(
            requestFactory = AppleWebSignInRequestFactory(config = config),
            coroutineDispatcher = coroutineDispatcher,
        )
    }
}

private class JvmAppleCredentialsManager(
    private val requestFactory: AppleWebSignInRequestFactory,
    private val coroutineDispatcher: CoroutineDispatcher,
) : AppleCredentialsManager {
    override suspend fun signIn(): AppleCredential =
        try {
            runInterruptible(coroutineDispatcher) {
                AppleSignInLoopbackReceiver().use { receiver ->
                    val request = requestFactory.create(returnUri = receiver.returnUri)

                    AuthorizationCodeInstalledApp.browse(request.authorizationUrl)
                    request.toCredential(response = receiver.waitForResponse())
                }
            }
        } catch (throwable: Throwable) {
            throw throwable.toAppleCredentialsFailure()
        }
}
