package io.github.taetae98coding.diary.compose.credential.apple

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.browser.auth.AuthTabIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

public class AndroidAppleCredentialsManagerFactory(
    private val config: AppleCredentialsConfig,
) : AppleCredentialsManagerFactory {
    @Composable
    override fun rememberAppleCredentialsManager(): AppleCredentialsManager {
        val context = LocalContext.current
        val resultReceiver = remember { AuthTabResultReceiver() }
        val launcher =
            rememberLauncherForActivityResult(AuthTabIntent.AuthenticateUserResultContract()) { result ->
                resultReceiver.deliver(resultCode = result.resultCode, resultUri = result.resultUri)
            }

        return remember(context, config, resultReceiver, launcher) {
            AndroidAppleCredentialsManager(
                requestFactory = AppleWebSignInRequestFactory(config = config),
                redirectScheme = context.packageName,
                resultReceiver = resultReceiver,
                launchAuthTab = { url -> AuthTabIntent.Builder().build().launch(launcher, url.toUri(), context.packageName) },
            )
        }
    }
}

internal class AndroidAppleCredentialsManager(
    private val requestFactory: AppleWebSignInRequestFactory,
    redirectScheme: String,
    private val resultReceiver: AuthTabResultReceiver,
    private val launchAuthTab: (String) -> Unit,
) : AppleCredentialsManager {
    private val returnUri = "$redirectScheme://$APPLE_SIGN_IN_REDIRECT_HOST"

    override suspend fun signIn(): AppleSignInResult {
        val request = requestFactory.create(returnUri = returnUri)
        val result = resultReceiver.begin()

        return try {
            launchAuthTab(request.authorizationUrl)
            request.toResult(response = result.await().toResponse())
        } catch (throwable: Throwable) {
            throw throwable.toAppleCredentialsFailure()
        } finally {
            resultReceiver.end(result)
        }
    }

    private fun AuthTabResult.toResponse(): AppleWebSignInResponse =
        when {
            resultCode == AuthTabIntent.RESULT_OK && resultUri != null -> AppleWebSignInResponse.fromParameters(resultUri::getQueryParameter)
            resultCode == AuthTabIntent.RESULT_CANCELED -> throw AppleCredentialsUserCancelException(message = "auth tab closed")
            else -> throw AppleCredentialsException(message = "auth tab result $resultCode")
        }

    companion object {
        const val APPLE_SIGN_IN_REDIRECT_HOST: String = "apple-sign-in"
    }
}
