package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO Android Apple 로그인 지원. 웹 OAuth 흐름으로 authorization code를 받아 서버에서 교환해야 한다.
@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager = remember { UnsupportedAppleCredentialsManager() }
