package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal actual fun rememberGoogleCredentialsManager(): GoogleCredentialsManager {
    val clientId = koinInject<String>(qualifier = named<GoogleCredentialsClientId>())
    val coroutineDispatcher = koinInject<CoroutineDispatcher>(qualifier = named<CredentialsDispatcher>())

    return remember(clientId, coroutineDispatcher) {
        JvmGoogleCredentialsManager(
            clientId = clientId,
            coroutineDispatcher = coroutineDispatcher,
        )
    }
}

private class JvmGoogleCredentialsManager(
    private val clientId: String,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val authorizationCodeAttemptFactory: GoogleAuthorizationCodeAttemptFactory =
        GoogleAuthorizationCodeAttemptFactory(clientId),
) : GoogleCredentialsManager {
    override suspend fun signIn(): GoogleCredential {
        val receiver =
            LocalServerReceiver
                .Builder()
                .build()

        return try {
            runInterruptible(coroutineDispatcher) {
                val redirectUri = receiver.redirectUri
                val attempt = authorizationCodeAttemptFactory.create(redirectUri = redirectUri)

                AuthorizationCodeInstalledApp.browse(attempt.authorizationUrl)
                attempt.credential(code = receiver.waitForCode())
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            throw throwable.toGoogleCredentialsException()
        } finally {
            receiver.stop()
        }
    }

    private fun Throwable.toGoogleCredentialsException(): GoogleCredentialsException =
        if (message?.contains(ACCESS_DENIED_ERROR) == true) {
            GoogleCredentialsUserCancelException(cause = this)
        } else {
            GoogleCredentialsException(cause = this)
        }

    companion object {
        private const val ACCESS_DENIED_ERROR = "access_denied"
    }
}
