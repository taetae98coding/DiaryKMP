package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

@Stable
internal class LoginPlatformSignInState {
    var isInProgress: Boolean by mutableStateOf(false)
        private set

    private var signInJob: Job? = null

    /**
     * 진행 표시 중이면 요청을 받지 않고 null을 반환한다.
     * 받은 요청은 진행 중이던 흐름을 취소한 뒤 [block]을 실행한다.
     */
    suspend fun <T : Any> signIn(
        isEndDetectable: Boolean,
        block: suspend () -> T,
    ): T? {
        if (isInProgress) return null

        val job = currentCoroutineContext().job
        signInJob?.cancel()
        signInJob = job
        if (isEndDetectable) isInProgress = true

        return try {
            block()
        } finally {
            if (isEndDetectable) isInProgress = false
            if (signInJob === job) signInJob = null
        }
    }
}

@Composable
internal fun rememberLoginPlatformSignInState(): LoginPlatformSignInState = remember { LoginPlatformSignInState() }
