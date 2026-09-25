package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
internal actual fun rememberGoogleCredentialsManager(): GoogleCredentialsManager {
    val clientId = koinInject<String>(qualifier = named<GoogleCredentialsClientId>())

    return remember(clientId) {
        WasmGoogleCredentialsManager(clientId = clientId)
    }
}

private class WasmGoogleCredentialsManager(
    private val clientId: String,
) : GoogleCredentialsManager {
    override val isSignInEndDetectable: Boolean = true

    override suspend fun signIn(): GoogleCredential =
        suspendCancellableCoroutine { continuation ->
            val client =
                GoogleAuthorizationCodeClient(
                    clientId = clientId,
                    scope = "openid profile email",
                    callback = { response ->
                        val code = response.code
                        if (code == null) {
                            continuation.resumeWithException(GoogleCredentialsException(message = response.error))
                        } else {
                            continuation.resume(
                                GoogleCredential.AuthorizationCode(
                                    code = code,
                                    clientId = clientId,
                                    redirectUri = POST_MESSAGE_REDIRECT_URI,
                                ),
                            )
                        }
                    },
                    errorCallback = { error ->
                        when (val type = error.type) {
                            "popup_closed" -> continuation.resumeWithException(GoogleCredentialsUserCancelException())
                            else -> continuation.resumeWithException(GoogleCredentialsException(message = type))
                        }
                    },
                )

            client.requestCode()
        }

    private companion object {
        private const val POST_MESSAGE_REDIRECT_URI = "postmessage"
    }
}
