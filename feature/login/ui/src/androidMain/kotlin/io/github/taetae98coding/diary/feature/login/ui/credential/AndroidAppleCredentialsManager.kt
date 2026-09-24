package io.github.taetae98coding.diary.feature.login.ui.credential

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.koinInject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val config = koinInject<AppleCredentialsConfig>()

    return remember(context, lifecycle, config) {
        AndroidAppleCredentialsManager(
            lifecycle = lifecycle,
            requestFactory = AppleWebSignInRequestFactory(config = config),
            returnUri = "${context.packageName}://$APPLE_SIGN_IN_REDIRECT_HOST",
            launchBrowser = { url -> CustomTabsIntent.Builder().build().launchUrl(context, url.toUri()) },
        )
    }
}

internal const val APPLE_SIGN_IN_REDIRECT_HOST: String = "apple-sign-in"

internal class AndroidAppleCredentialsManager(
    private val lifecycle: Lifecycle,
    private val requestFactory: AppleWebSignInRequestFactory,
    private val returnUri: String,
    private val launchBrowser: (String) -> Unit,
    private val redirectRelay: AppleSignInRedirectRelay = AppleSignInRedirectRelay,
) : AppleCredentialsManager {
    override suspend fun signIn(): AppleCredential {
        val request = requestFactory.create(returnUri = returnUri)
        val redirect = redirectRelay.begin()

        return try {
            launchBrowser(request.authorizationUrl)
            request.toCredential(response = awaitRedirect(redirect).toResponse())
        } catch (throwable: Throwable) {
            throw throwable.toAppleCredentialsFailure()
        } finally {
            redirectRelay.end(redirect)
        }
    }

    // 브라우저 탭을 닫기만 하면 앱 링크가 오지 않으므로, 앱이 다시 앞으로 돌아오는 것도 함께 기다려 미도착을 감지한다.
    private suspend fun awaitRedirect(redirect: CompletableDeferred<Uri>): Uri =
        coroutineScope {
            val returned = async { lifecycle.awaitLeaveAndReturn() }

            try {
                select {
                    redirect.onAwait { uri -> uri }
                    returned.onAwait {
                        withTimeoutOrNull(REDIRECT_GRACE_AFTER_RETURN) { redirect.await() }
                            ?: throw AppleCredentialsUserCancelException(message = "returned without response")
                    }
                }
            } finally {
                returned.cancel()
            }
        }

    private suspend fun Lifecycle.awaitLeaveAndReturn() {
        currentStateFlow.first { state -> !state.isAtLeast(Lifecycle.State.RESUMED) }
        currentStateFlow.first { state -> state.isAtLeast(Lifecycle.State.RESUMED) }
    }

    private fun Uri.toResponse(): AppleWebSignInResponse =
        AppleWebSignInResponse(
            idToken = getQueryParameter(AppleWebSignInResponse.ID_TOKEN_PARAMETER),
            state = getQueryParameter(AppleWebSignInResponse.STATE_PARAMETER),
            error = getQueryParameter(AppleWebSignInResponse.ERROR_PARAMETER),
        )

    companion object {
        // 앱 링크 Activity가 결과를 넘긴 직후 앱이 앞으로 돌아오므로, 그 순서가 뒤바뀌어도 결과를 놓치지 않을 만큼만 기다린다.
        val REDIRECT_GRACE_AFTER_RETURN: Duration = 1.seconds
    }
}
