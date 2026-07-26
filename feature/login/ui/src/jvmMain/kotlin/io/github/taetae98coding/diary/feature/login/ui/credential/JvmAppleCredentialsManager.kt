package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO JVM Apple 로그인 지원. 로컬 리디렉션 수신기와 브라우저 OAuth 흐름이 필요하다.
@Composable
internal actual fun rememberAppleCredentialsManager(): AppleCredentialsManager = remember { UnsupportedAppleCredentialsManager() }
