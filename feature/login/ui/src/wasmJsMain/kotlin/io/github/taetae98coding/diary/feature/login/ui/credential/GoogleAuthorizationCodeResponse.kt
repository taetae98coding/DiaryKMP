@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

internal external interface GoogleAuthorizationCodeResponse : JsAny {
    val code: String?
    val error: String?
}
