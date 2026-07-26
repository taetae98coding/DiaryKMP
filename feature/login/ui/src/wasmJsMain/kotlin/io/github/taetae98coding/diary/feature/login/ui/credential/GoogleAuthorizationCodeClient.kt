@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

internal external interface GoogleAuthorizationCodeClient : JsAny {
    fun requestCode()
}

@Suppress("UnusedParameter")
internal fun GoogleAuthorizationCodeClient(
    clientId: String,
    scope: String,
    callback: (GoogleAuthorizationCodeResponse) -> Unit,
    errorCallback: (GoogleAuthorizationCodeError) -> Unit,
): GoogleAuthorizationCodeClient =
    js(
        """
        google.accounts.oauth2.initCodeClient({
            client_id: clientId,
            scope: scope,
            ux_mode: 'popup',
            callback: callback,
            error_callback: errorCallback
        })
        """,
    )
