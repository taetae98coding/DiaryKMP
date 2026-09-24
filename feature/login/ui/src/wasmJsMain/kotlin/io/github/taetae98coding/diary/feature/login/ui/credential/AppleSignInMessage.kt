@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

internal external interface AppleSignInMessage : JsAny {
    val idToken: String?
    val state: String?
    val error: String?
}
