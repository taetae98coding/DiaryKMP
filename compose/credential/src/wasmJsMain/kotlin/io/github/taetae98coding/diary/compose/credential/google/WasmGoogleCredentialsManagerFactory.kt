package io.github.taetae98coding.diary.compose.credential.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class WasmGoogleCredentialsManagerFactory(
    private val clientId: String,
) : GoogleCredentialsManagerFactory {
    @Composable
    override fun rememberGoogleCredentialsManager(): GoogleCredentialsManager =
        remember(clientId) {
            WasmGoogleCredentialsManager(clientId = clientId)
        }
}

private class WasmGoogleCredentialsManager(
    private val clientId: String,
) : GoogleCredentialsManager {
    override suspend fun signIn(): GoogleCredential =
        suspendCancellableCoroutine { continuation ->
            val client =
                GoogleAuthorizationCodeClient(
                    clientId = clientId,
                    scope = GOOGLE_AUTHORIZATION_SCOPE,
                    callback = { response ->
                        runCatching {
                            GoogleCredential.AuthorizationCode(
                                code = googleAuthorizationCodeOrThrow(code = response.code.orEmpty(), error = response.error.orEmpty()),
                                clientId = clientId,
                                redirectUri = POST_MESSAGE_REDIRECT_URI,
                            )
                        }.onSuccess(continuation::resume)
                            .onFailure(continuation::resumeWithException)
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
