package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.Uuid

@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        IosAppleCredentialsManager(viewController = viewController)
    }
}

private class IosAppleCredentialsManager(
    private val viewController: UIViewController,
) : AppleCredentialsManager {
    override val isSignInEndDetectable: Boolean = true

    /**
     * [ASAuthorizationController]가 delegate를 weak 참조로 들고 있어, 응답을 받을 때까지
     * delegate를 여기에서 강하게 참조한다. delegate는 controller를 강하게 참조한다.
     */
    private var pendingDelegate: AppleIdAuthorizationDelegate? = null

    override suspend fun signIn(): AppleCredential =
        suspendCancellableCoroutine { continuation ->
            val nonce = Uuid.random().toString()
            val request =
                ASAuthorizationAppleIDProvider().createRequest().apply {
                    setRequestedScopes(listOf(ASAuthorizationScopeEmail))
                    setNonce(CredentialsNonce.hash(nonce))
                }
            val delegate =
                AppleIdAuthorizationDelegate(
                    viewController = viewController,
                    onCredential = { idToken ->
                        pendingDelegate = null
                        continuation.resume(AppleCredential(idToken = idToken, nonce = nonce))
                    },
                    onFailure = { throwable ->
                        pendingDelegate = null
                        continuation.resumeWithException(throwable)
                    },
                )
            val controller = ASAuthorizationController(authorizationRequests = listOf(request))

            controller.delegate = delegate
            controller.presentationContextProvider = delegate
            delegate.controller = controller
            pendingDelegate = delegate

            continuation.invokeOnCancellation {
                pendingDelegate?.controller?.cancel()
                pendingDelegate = null
            }

            controller.performRequests()
        }
}
