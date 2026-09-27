package io.github.taetae98coding.diary.app.shared.integrity

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.integrity.repository.PlayIntegrityRepository
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
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
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-011 확인 중 앱이 백그라운드로 가도 끝까지 진행해 로그를 남긴다`() {
        val serverResponse = CompletableDeferred<Unit>()
        val logList = recordPlayIntegrityLog()
        val repository = mockk<PlayIntegrityRepository>()
        coEvery { repository.fetch() } coAnswers {
            serverResponse.await()
            verdict()
        }
        val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = logPlayIntegrityUseCase(repository = repository))
        val lifecycleOwner = setEffect(log = viewModel::log, initialState = Lifecycle.State.STARTED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { serverResponse.complete(Unit) }
        composeRule.waitUntil { logList.isNotEmpty() }

        logList.single().name shouldBe PLAY_INTEGRITY_EVENT
        coVerify(exactly = 1) { repository.fetch() }
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
    fun `TC-PLAY-INTEGRITY-LOGGING-DOMAIN-020 확인 중 화면이 재생성되어도 끝까지 진행해 로그를 남긴다`() {
        val serverResponse = CompletableDeferred<Unit>()
        val logList = recordPlayIntegrityLog()
        var callCount = 0
        val repository = mockk<PlayIntegrityRepository>()
        coEvery { repository.fetch() } coAnswers {
            callCount += 1
            if (callCount == 1) {
                serverResponse.await()
                verdict()
            } else {
                null
            }
        }
        val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = logPlayIntegrityUseCase(repository = repository))
        val restorationTester = setRestorableEffect(log = viewModel::log)

        composeRule.runOnIdle { callCount shouldBe 1 }
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { serverResponse.complete(Unit) }
        composeRule.waitUntil { logList.isNotEmpty() }

        logList.single().name shouldBe PLAY_INTEGRITY_EVENT
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

    // 판정 확인은 서버 응답을 받은 뒤 판정 결과 로그를 남기는 데까지가 한 과정이므로, 로그를 남기는 실제 확인 과정을 쓰고 서버 응답만 제어한다.
    // 확인 과정의 생성자는 다른 모듈에서 부를 수 없게 막혀 있어 Java 리플렉션으로 만든다.
    private fun logPlayIntegrityUseCase(repository: PlayIntegrityRepository): LogPlayIntegrityUseCase =
        LogPlayIntegrityUseCase::class.java
            .getConstructor(PlayIntegrityRepository::class.java)
            .newInstance(repository)

    private fun verdict(): JsonObject = buildJsonObject { putJsonObject("requestDetails") { put("requestHash", fixtureMonkey.giveMeOne<String>()) } }

    private fun recordPlayIntegrityLog(): List<AnalyticsEventLog> {
        val logList = mutableListOf<AnalyticsEventLog>()
        val delegate = mockk<DiaryLoggerDelegate>()
        every { delegate.log(log = any()) } answers {
            val log = firstArg<DiaryLog>()
            if (log is AnalyticsEventLog) logList += log
        }
        DiaryLogger.add(delegate = delegate)

        return logList
    }

    private companion object {
        const val PLAY_INTEGRITY_EVENT: String = "play_integrity"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
