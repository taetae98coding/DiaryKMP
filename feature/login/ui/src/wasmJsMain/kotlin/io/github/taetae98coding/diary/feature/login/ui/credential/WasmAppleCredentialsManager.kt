@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import kotlinx.browser.window
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import org.w3c.dom.MessageEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager {
    val config = koinInject<AppleCredentialsConfig>()

    return remember(config) {
        WasmAppleCredentialsManager(
            requestFactory = AppleWebSignInRequestFactory(config = config),
            callbackOrigin = URL(config.callbackUrl).origin,
        )
    }
}

private class WasmAppleCredentialsManager(
    private val requestFactory: AppleWebSignInRequestFactory,
    private val callbackOrigin: String,
) : AppleCredentialsManager {
    override suspend fun signIn(): AppleCredential =
        suspendCancellableCoroutine { continuation ->
            val request = requestFactory.create(returnUri = window.location.origin)
            val popup = window.open(url = request.authorizationUrl, target = POPUP_TARGET, features = POPUP_FEATURES)

            if (popup == null) {
                continuation.resumeWithException(AppleCredentialsException(message = "popup blocked"))
            } else {
                AppleSignInPopupSession(popup = popup, request = request, callbackOrigin = callbackOrigin, continuation = continuation)
                    .start()
            }
        }

    private companion object {
        private const val POPUP_TARGET = "diary-apple-sign-in"
        private const val POPUP_FEATURES = "popup=yes,width=600,height=700"
    }
}

private class AppleSignInPopupSession(
    private val popup: Window,
    private val request: AppleWebSignInRequest,
    private val callbackOrigin: String,
    private val continuation: CancellableContinuation<AppleCredential>,
) {
    private val messageListener: (Event) -> Unit = { event -> onMessage(event as MessageEvent) }
    private var closedWatcherId = 0

    fun start() {
        window.addEventListener(type = MESSAGE_EVENT, callback = messageListener)
        // 다른 출처의 팝업은 close 이벤트를 주지 않으므로 닫힘을 주기적으로 확인한다.
        closedWatcherId =
            window.setInterval(
                handler = {
                    onClosedCheck()
                    null
                },
                timeout = POPUP_CLOSED_POLL_MILLIS,
            )
        continuation.invokeOnCancellation { finish(closePopup = true) }
    }

    // 서버 콜백 페이지가 보낸 결과만 받는다. 다른 출처의 메시지는 다른 창이 보낸 것이다.
    private fun onMessage(event: MessageEvent) {
        if (event.origin != callbackOrigin || !continuation.isActive) return

        val message = event.data?.unsafeCast<AppleSignInMessage>()
        val response =
            AppleWebSignInResponse(
                idToken = message?.idToken,
                state = message?.state,
                error = message?.error,
            )

        finish(closePopup = true)
        runCatching { request.toCredential(response = response) }
            .onSuccess(continuation::resume)
            .onFailure(continuation::resumeWithException)
    }

    private fun onClosedCheck() {
        if (!popup.closed || !continuation.isActive) return

        finish(closePopup = false)
        continuation.resumeWithException(AppleCredentialsUserCancelException(message = "popup closed"))
    }

    private fun finish(closePopup: Boolean) {
        window.removeEventListener(type = MESSAGE_EVENT, callback = messageListener)
        window.clearInterval(closedWatcherId)
        if (closePopup && !popup.closed) {
            popup.close()
        }
    }

    private companion object {
        private const val MESSAGE_EVENT = "message"
        private const val POPUP_CLOSED_POLL_MILLIS = 500
    }
}
