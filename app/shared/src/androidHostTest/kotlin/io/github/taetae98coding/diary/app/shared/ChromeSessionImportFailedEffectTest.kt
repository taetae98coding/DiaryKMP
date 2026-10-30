package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChromeSessionImportFailedEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-366137ee-c354-4e7a-9adc-8327e08a602a 가져오기가 실패로 끝나면 안내를 한 번 짧게 표시한다`() {
        val effectChannel = Channel<AppChromeSessionEffect>(capacity = Channel.BUFFERED)
        val snackbarHostState = setEffect(effectChannel = effectChannel)

        composeRule.runOnIdle { effectChannel.trySend(AppChromeSessionEffect.ImportFailed).getOrThrow() }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onAllNodesWithText(DEFAULT_IMPORT_FAILED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.runOnIdle {
            val visuals = snackbarHostState.currentSnackbarData?.visuals

            visuals?.message shouldBe DEFAULT_IMPORT_FAILED_MESSAGE
            visuals?.duration shouldBe SnackbarDuration.Short
            visuals?.actionLabel shouldBe null
            visuals?.withDismissAction shouldBe false
        }
    }

    @Test
    fun `TC-37cd7760-bb69-4a46-9d5f-123779a5fbea 다시 실패하면 앞선 안내를 닫고 안내를 한 번 더 표시한다`() {
        val effectChannel = Channel<AppChromeSessionEffect>(capacity = Channel.BUFFERED)
        val snackbarHostState = setEffect(effectChannel = effectChannel)
        composeRule.runOnIdle { effectChannel.trySend(AppChromeSessionEffect.ImportFailed).getOrThrow() }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeByFrame()
        var firstSnackbarData: SnackbarData? = null
        composeRule.runOnIdle { firstSnackbarData = snackbarHostState.currentSnackbarData }

        composeRule.runOnIdle { effectChannel.trySend(AppChromeSessionEffect.ImportFailed).getOrThrow() }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.runOnIdle {
            firstSnackbarData shouldNotBe null
            snackbarHostState.currentSnackbarData shouldNotBe firstSnackbarData
            snackbarHostState.currentSnackbarData?.visuals?.message shouldBe DEFAULT_IMPORT_FAILED_MESSAGE
        }
    }

    private fun setEffect(effectChannel: Channel<AppChromeSessionEffect>): SnackbarHostState {
        val snackbarHostState = SnackbarHostState()
        // 안내가 짧게 표시되었다가 사라지기 전에 확인하도록 시간을 직접 넘긴다.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            ChromeSessionImportFailedEffect(
                snackbarHostState = snackbarHostState,
                effect = effectChannel.receiveAsFlow(),
            )
            SnackbarHost(hostState = snackbarHostState)
        }
        composeRule.mainClock.advanceTimeByFrame()

        return snackbarHostState
    }

    private companion object {
        private const val DEFAULT_IMPORT_FAILED_MESSAGE = "Couldn't get Chrome logins."
    }
}
