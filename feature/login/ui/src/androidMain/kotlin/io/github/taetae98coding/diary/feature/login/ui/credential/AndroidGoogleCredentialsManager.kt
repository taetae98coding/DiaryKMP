package io.github.taetae98coding.diary.feature.login.ui.credential

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid

@Composable
internal actual fun rememberGoogleCredentialsManager(): GoogleCredentialsManager {
    val context = LocalContext.current
    val serverClientId = koinInject<String>(qualifier = named<GoogleCredentialsServerClientId>())

    return remember(context, serverClientId) {
        AndroidGoogleCredentialsManager(
            context = context,
            serverClientId = serverClientId,
        )
    }
}

private class AndroidGoogleCredentialsManager(
    private val context: Context,
    private val serverClientId: String,
) : GoogleCredentialsManager {
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(): GoogleCredential =
        try {
            val nonce = Uuid.random().toString()
            val option =
                GetSignInWithGoogleOption
                    .Builder(serverClientId)
                    .setNonce(CredentialsNonce.hash(nonce))
                    .build()
            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(option)
                    .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential as CustomCredential
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)

            GoogleCredential.IdToken(
                idToken = googleCredential.idToken,
                nonce = nonce,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: GetCredentialCancellationException) {
            throw GoogleCredentialsUserCancelException(cause = exception)
        } catch (throwable: Throwable) {
            throw GoogleCredentialsException(cause = throwable)
        }
}
