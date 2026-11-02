@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.credential

internal external interface AppleSignInMessage : JsAny {
    val idToken: String?
    val code: String?
    val state: String?
    val error: String?
}
