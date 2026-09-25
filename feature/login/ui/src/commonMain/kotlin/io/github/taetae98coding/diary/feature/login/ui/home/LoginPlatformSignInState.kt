package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.coroutines.cancellation.CancellationException

@Stable
internal class LoginPlatformSignInState {
    var isInProgress: Boolean by mutableStateOf(false)
        private set

    private var acceptedResultCount: Int = 0

    /**
     * 받아들인 결과를 반환하고, 받지 않는 요청이나 결과면 null을 반환한다.
     * [block]의 실패도 받아들인 결과일 때만 그대로 던진다.
     */
    suspend fun <T : Any> signIn(
        isEndDetectable: Boolean,
        block: suspend () -> T,
    ): T? = if (isInProgress) null else runSignIn(isEndDetectable = isEndDetectable, block = block)

    private suspend fun <T : Any> runSignIn(
        isEndDetectable: Boolean,
        block: suspend () -> T,
    ): T? {
        val acceptedResultCountAtStart = acceptedResultCount
        if (isEndDetectable) isInProgress = true

        val outcome =
            try {
                Result.success(block())
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            } finally {
                if (isEndDetectable) isInProgress = false
            }

        return if (acceptedResultCount == acceptedResultCountAtStart) {
            outcome.getOrThrow().also { acceptedResultCount += 1 }
        } else {
            null
        }
    }
}

@Composable
internal fun rememberLoginPlatformSignInState(): LoginPlatformSignInState = remember { LoginPlatformSignInState() }
