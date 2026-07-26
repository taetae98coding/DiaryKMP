package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential

@Stable
internal interface AppleCredentialsManager {
    suspend fun signIn(): AppleCredential
}

@Composable
internal expect fun rememberAppleCredentialsManager(): AppleCredentialsManager

/**
 * Apple 로그인을 아직 제공하지 않는 플랫폼용 구현이다.
 * 버튼은 모든 플랫폼에서 같은 자리에 표시하고, 선택하면 인증 결과를 받지 못한 실패로 안내한다.
 */
internal class UnsupportedAppleCredentialsManager : AppleCredentialsManager {
    override suspend fun signIn(): AppleCredential = throw AppleCredentialsException(message = "Apple sign-in is not supported on this platform")
}
