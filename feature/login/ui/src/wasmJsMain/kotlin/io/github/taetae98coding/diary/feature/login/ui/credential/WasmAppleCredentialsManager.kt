package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO WASM Apple 로그인 지원. Sign in with Apple JS 흐름이 필요하다.
@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager = remember { UnsupportedAppleCredentialsManager() }
