@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIViewController
import swiftPMImport.DiaryKmp.feature.login.feature.login.ui.GIDSignIn
import swiftPMImport.DiaryKmp.feature.login.feature.login.ui.kGIDSignInErrorCodeCanceled
import swiftPMImport.DiaryKmp.feature.login.feature.login.ui.kGIDSignInErrorDomain
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.Uuid

@Composable
internal actual fun rememberGoogleCredentialsManager(): GoogleCredentialsManager {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        IosGoogleCredentialsManager(viewController = viewController)
    }
}

private class IosGoogleCredentialsManager(
    private val viewController: UIViewController,
) : GoogleCredentialsManager {
    override val isSignInEndDetectable: Boolean = true

    override suspend fun signIn(): GoogleCredential =
        suspendCancellableCoroutine { continuation ->
            val nonce = Uuid.random().toString()

            GIDSignIn.sharedInstance.signInWithPresentingViewController(
                presentingViewController = viewController,
                hint = null,
                additionalScopes = null,
                nonce = CredentialsNonce.hash(nonce),
                claims = null,
            ) { result, error ->
                val idToken = result?.user?.idToken?.tokenString

                when {
                    idToken != null -> {
                        continuation.resume(
                            GoogleCredential.IdToken(
                                idToken = idToken,
                                nonce = nonce,
                            ),
                        )
                    }

                    error != null && error.domain == kGIDSignInErrorDomain && error.code == kGIDSignInErrorCodeCanceled -> {
                        continuation.resumeWithException(GoogleCredentialsUserCancelException())
                    }

                    else -> {
                        continuation.resumeWithException(GoogleCredentialsException(message = error?.localizedDescription))
                    }
                }
            }
        }
}
