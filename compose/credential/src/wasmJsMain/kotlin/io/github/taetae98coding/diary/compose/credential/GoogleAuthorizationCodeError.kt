@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.credential

internal external interface GoogleAuthorizationCodeError : JsAny {
    val type: String?
}
