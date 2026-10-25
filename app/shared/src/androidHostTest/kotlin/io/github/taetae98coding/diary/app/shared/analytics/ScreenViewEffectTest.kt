@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared.analytics

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ScreenViewEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c175092c-a776-4cb0-997b-00bf151a20f0 화면을 표시하면 그 화면의 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        setScreenViewEffect(log)

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
        }
    }

    @Test
    fun `TC-6ff1e76d-1d6a-4b2f-9930-dfc929d20f95 다른 화면으로 이동하면 이동한 화면의 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setScreenViewEffect(log)

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME)) }
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-0fd2aa7e-724c-4acc-9910-e8063e45219c 두 화면을 함께 표시해도 가장 나중에 진입한 화면만 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setListDetailScreenViewEffect(log)

        composeRule.runOnIdle { appState.backStack.add(TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { appState.backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_DETAIL)) }
            verify(exactly = 1) { log(ScreenViewLog(screenName = TAG_MEMO_FINISHED_LIST)) }
        }
    }

    @Test
    fun `TC-1cbbab70-4991-4025-866a-06f957913cbe 겹쳐 표시하는 화면도 화면 조회로 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setScreenViewEffect(log)

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }
        composeRule.runOnIdle { appState.backStack.add(MemoHomeFilterNavKey) }

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME_FILTER)) }
        }
    }

    @Test
    fun `TC-e41ebef2-9037-4cf7-ab7c-4abf4694364b 화면 안에서 연 다이얼로그는 화면 조회로 남지 않는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val dialogText = "dialog${fixtureMonkey.giveMeOne<Int>()}"
        var isDialogVisible by mutableStateOf(false)

        composeRule.setContent {
            ScreenViewEffect(log = log)
            if (isDialogVisible) {
                Dialog(onDismissRequest = { isDialogVisible = false }) { BasicText(text = dialogText) }
            }
        }

        composeRule.runOnIdle { isDialogVisible = true }
        composeRule.onNodeWithText(dialogText).assertIsDisplayed()
        composeRule.runOnIdle { isDialogVisible = false }
        composeRule.onNodeWithText(dialogText).assertDoesNotExist()

        composeRule.runOnIdle {
            verify(exactly = 1) { log(any()) }
            verify(exactly = 1) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
        }
    }

    @Test
    fun `TC-58209d4e-67f5-473c-9ed0-4d735894bcac 겹쳐 표시한 화면을 닫으면 아래 화면의 화면 조회가 다시 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setScreenViewEffect(log)

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }
        composeRule.runOnIdle { appState.backStack.add(MemoHomeFilterNavKey) }
        composeRule.runOnIdle { appState.backStack.removeAt(appState.backStack.lastIndex) }

        composeRule.runOnIdle {
            verify(exactly = 2) { log(ScreenViewLog(screenName = MEMO_HOME)) }
        }
    }

    @Test
    fun `TC-dee14294-e838-4a4d-95b2-4ccbf91fc8a6 백그라운드에서 돌아오면 같은 화면의 화면 조회가 다시 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)
        setScreenViewEffect(log, lifecycleOwner)

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 2) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
        }
    }

    @Test
    fun `TC-77ed6712-737b-4d2b-828f-b08e20b97c36 화면이 재생성되어 복원되면 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState

        restorationTester.setContent {
            appState = rememberAppState()
            ScreenViewEffect(log = log, appState = appState)
        }

        composeRule.runOnIdle { appState.backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_DETAIL)) }
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            verify(exactly = 2) { log(ScreenViewLog(screenName = MEMO_DETAIL)) }
        }
    }

    @Test
    fun `TC-fd5e53b3-3cc2-450e-b18b-89d98275929e 앱을 다시 실행하면 처음 보이는 화면의 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        var launchCount by mutableIntStateOf(0)
        lateinit var appState: AppState

        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 앱 상태와 기록을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                appState = rememberAppState()
                ScreenViewEffect(log = log, appState = appState)
            }
        }
        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }
        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME)) }
        }

        composeRule.runOnIdle { launchCount += 1 }

        composeRule.runOnIdle {
            verify(exactly = 2) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME)) }
        }
    }

    @Test
    fun `TC-d741d32e-60b9-4492-903c-9c04ef19a18a 화면을 떠난 사건은 따로 남기지 않는다`() {
        val logList = mutableListOf<ScreenViewLog>()
        val appState = setScreenViewEffect(log = { screenViewLog -> logList += screenViewLog })

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }

        composeRule.runOnIdle {
            logList shouldContainExactly listOf(ScreenViewLog(screenName = CALENDAR_HOME), ScreenViewLog(screenName = MEMO_HOME))
        }
    }

    @Test
    fun `TC-5aaffc7f-b4ed-45b0-82a4-6ea04e4f4f60 현재 화면이 바뀌지 않으면 화면 조회가 더 남지 않는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val content = mutableStateOf(value = fixtureMonkey.giveMeOne<String>())

        composeRule.setContent {
            BasicText(text = content.value)
            ScreenViewEffect(log = log)
        }

        composeRule.runOnIdle { content.value = "changed ${content.value}" }

        composeRule.runOnIdle {
            verify(exactly = 1) { log(any()) }
        }
    }

    @Test
    fun `TC-bb3a7d32-a89e-4aee-9c2c-d8ca4545a065 화면 조회는 오류 보고로 남지 않는다`() {
        val logList = mutableListOf<DiaryLog>()
        val delegate = mockk<DiaryLoggerDelegate>()
        every { delegate.log(log = any()) } answers { logList += firstArg<DiaryLog>() }
        DiaryLogger.add(delegate = delegate)

        composeRule.setContent {
            ScreenViewEffect(log = DiaryLogger::log)
        }

        composeRule.runOnIdle {
            logList.filterIsInstance<ScreenViewLog>() shouldContainExactly listOf(ScreenViewLog(screenName = CALENDAR_HOME))
            logList.filterIsInstance<CrashlyticsLog>().shouldBeEmpty()
        }
    }

    @Test
    fun `TC-93c21a25-8e9c-4873-bc82-f6dde6365764 기록에 실패해도 화면은 그대로 표시된다`() {
        val failingDelegate = mockk<DiaryLoggerDelegate>()
        every { failingDelegate.log(log = any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
        DiaryLogger.add(delegate = failingDelegate)
        lateinit var appState: AppState

        composeRule.setContent {
            appState = rememberAppState()
            BasicText(text = fixtureMonkey.giveMeOne<String>(), modifier = Modifier.testTag(CONTENT_TEST_TAG))
            ScreenViewEffect(log = DiaryLogger::log, appState = appState)
        }

        composeRule.onNodeWithTag(CONTENT_TEST_TAG).assertExists()

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }

        composeRule.onNodeWithTag(CONTENT_TEST_TAG).assertExists()
    }

    private fun setScreenViewEffect(
        log: (ScreenViewLog) -> Unit,
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED),
    ): AppState {
        lateinit var appState: AppState

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                appState = rememberAppState()
                ScreenViewEffect(log = log, appState = appState)
            }
        }

        return appState
    }

    // 목록과 상세가 함께 놓이는지는 메모·태그 기능이 앱에 등록한 배치 정보가 정하므로, 그 배치 정보를 그대로 쓰고 두 화면의 본문만 이름이 보이는 대역으로 바꾼다.
    private fun setListDetailScreenViewEffect(log: (ScreenViewLog) -> Unit): AppState {
        lateinit var appState: AppState

        composeRule.setContent {
            appState = rememberAppState()
            val backStack = appState.backStack
            val appEntryProvider =
                remember(backStack) {
                    entryProvider<ScreenNavKey>(fallback = { key -> NavEntry(key = key) {} }) {
                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                    }
                }

            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { key ->
                        NavEntry(key = key, metadata = appEntryProvider(key).metadata) { BasicText(text = contentOf(key)) }
                    },
                )
            }
            ScreenViewEffect(log = log, appState = appState)
        }

        return appState
    }

    private fun contentOf(key: ScreenNavKey): String =
        when (key) {
            is TagMemoFinishedListNavKey -> LIST_CONTENT
            is MemoDetailNavKey -> DETAIL_CONTENT
            else -> OTHER_CONTENT
        }

    public companion object {
        private const val CALENDAR_HOME: String = "CalendarHome"
        private const val MEMO_HOME: String = "MemoHome"
        private const val MEMO_HOME_FILTER: String = "MemoHomeFilter"
        private const val MEMO_DETAIL: String = "MemoDetail"
        private const val TAG_MEMO_FINISHED_LIST: String = "TagMemoFinishedList"
        private const val CONTENT_TEST_TAG: String = "content"
        private const val LIST_CONTENT: String = "TagMemoFinishedListContent"
        private const val DETAIL_CONTENT: String = "MemoDetailContent"
        private const val OTHER_CONTENT: String = "OtherContent"
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
