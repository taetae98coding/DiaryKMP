package io.github.taetae98coding.diary.compose.credential

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

@Stable
public class PlatformSignInState {
    private var signInJob: Job? = null

    public suspend fun <T> signIn(block: suspend () -> T): T {
        val job = currentCoroutineContext().job
        signInJob?.cancel()
        signInJob = job

        return try {
            block()
        } finally {
            if (signInJob === job) signInJob = null
        }
    }

    public fun cancel() {
        signInJob?.cancel()
    }
}

@Composable
public fun rememberPlatformSignInState(): PlatformSignInState = remember { PlatformSignInState() }
