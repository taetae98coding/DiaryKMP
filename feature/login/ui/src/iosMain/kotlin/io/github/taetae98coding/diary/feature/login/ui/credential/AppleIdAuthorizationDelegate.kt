@file:OptIn(BetaInteropApi::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

import kotlinx.cinterop.BetaInteropApi
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationErrorCanceled
import platform.AuthenticationServices.ASAuthorizationErrorDomain
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject

internal class AppleIdAuthorizationDelegate(
    private val viewController: UIViewController,
    private val onCredential: (String) -> Unit,
    private val onFailure: (Throwable) -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {
    var controller: ASAuthorizationController? = null

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val idToken = credential?.identityToken?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding) }

        if (idToken == null) {
            onFailure(AppleCredentialsException(message = "no identity token"))
        } else {
            onCredential(idToken.toString())
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        if (didCompleteWithError.domain == ASAuthorizationErrorDomain && didCompleteWithError.code == ASAuthorizationErrorCanceled) {
            onFailure(AppleCredentialsUserCancelException(message = didCompleteWithError.localizedDescription))
        } else {
            onFailure(AppleCredentialsException(message = didCompleteWithError.localizedDescription))
        }
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): UIWindow? = viewController.view.window
}
