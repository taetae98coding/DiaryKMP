package io.github.taetae98coding.diary.compose.credential.apple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.credential.CredentialsDispatcher
import io.github.taetae98coding.diary.compose.credential.LoopbackRedirectReceiver
import io.github.taetae98coding.diary.compose.credential.openInSystemBrowser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
public actual fun rememberAppleCredentialsManager(): AppleCredentialsManager {
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
    override suspend fun signIn(): AppleSignInResult =
        try {
            runInterruptible(coroutineDispatcher) {
                LoopbackRedirectReceiver().use { receiver ->
                    val request = requestFactory.create(returnUri = receiver.redirectUri)

                    openInSystemBrowser(request.authorizationUrl)
                    request.toResult(response = AppleWebSignInResponse.fromParameters(receiver.waitForParameters()::get))
                }
            }
        } catch (throwable: Throwable) {
            throw throwable.toAppleCredentialsFailure()
        }
}
