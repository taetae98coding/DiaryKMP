package io.github.taetae98coding.diary.app.shared.integrity

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayIntegrityLogEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-010 앱이 활성 상태가 될 때마다 확인한다`() {
        val log = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setEffect(log = log, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { verify(exactly = 0) { log() } }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { verify(exactly = 1) { log() } }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { verify(exactly = 1) { log() } }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { verify(exactly = 2) { log() } }
    }

    @Test
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-013 앱이 보이는 동안 포커스만 오가는 것은 계기가 아니다`() {
        val log = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setEffect(log = log, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle { verify(exactly = 1) { log() } }
    }

    private fun setEffect(
        log: () -> Unit,
        initialState: Lifecycle.State,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                PlayIntegrityLogEffect(log = log)
            }
        }

        return lifecycleOwner
    }
}
