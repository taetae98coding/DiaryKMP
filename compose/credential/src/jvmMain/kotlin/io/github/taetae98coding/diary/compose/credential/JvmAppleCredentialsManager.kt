package io.github.taetae98coding.diary.compose.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    // 시스템 브라우저 창이 닫혀도 앱에는 아무 신호가 오지 않는다.
    override val isSignInEndDetectable: Boolean = false

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
