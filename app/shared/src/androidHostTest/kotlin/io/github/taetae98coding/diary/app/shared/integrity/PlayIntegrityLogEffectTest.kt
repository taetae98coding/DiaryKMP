package io.github.taetae98coding.diary.app.shared.integrity

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
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

    @Test
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-011 확인 중 앱이 백그라운드로 가도 끝까지 진행한다`() {
        val serverResponse = CompletableDeferred<Unit>()
        var finishedCount = 0
        val useCase = mockk<LogPlayIntegrityUseCase>()
        coEvery { useCase(Unit) } coAnswers {
            serverResponse.await()
            finishedCount += 1
            Result.success(Unit)
        }
        val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = useCase)
        val lifecycleOwner = setEffect(log = viewModel::log, initialState = Lifecycle.State.STARTED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { serverResponse.complete(Unit) }
        composeRule.waitUntil { finishedCount == 1 }

        finishedCount shouldBe 1
        coVerify(exactly = 1) { useCase(Unit) }
    }

    @Test
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-016 화면이 재생성되어 다시 보이게 되면 한 번 더 확인한다`() {
        val log = mockk<() -> Unit>(relaxed = true)
        val restorationTester = setRestorableEffect(log = log)

        composeRule.runOnIdle { verify(exactly = 1) { log() } }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { verify(exactly = 2) { log() } }
    }

    @Test
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-020 확인 중 화면이 재생성되어도 끝까지 진행한다`() {
        val serverResponse = CompletableDeferred<Unit>()
        var firstFinished = false
        var callCount = 0
        val useCase = mockk<LogPlayIntegrityUseCase>()
        coEvery { useCase(Unit) } coAnswers {
            callCount += 1
            if (callCount == 1) {
                serverResponse.await()
                firstFinished = true
            }
            Result.success(Unit)
        }
        val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = useCase)
        val restorationTester = setRestorableEffect(log = viewModel::log)

        composeRule.runOnIdle { callCount shouldBe 1 }
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { serverResponse.complete(Unit) }
        composeRule.waitUntil { firstFinished }

        firstFinished shouldBe true
    }

    private fun setRestorableEffect(log: () -> Unit): StateRestorationTester {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                PlayIntegrityLogEffect(log = log)
            }
        }

        return restorationTester
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
