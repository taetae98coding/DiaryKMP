@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared.analytics

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-001 화면을 표시하면 그 화면의 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        setScreenViewEffect(log)

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = CALENDAR_HOME)) }
        }
    }

    @Test
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-002 다른 화면으로 이동하면 이동한 화면의 화면 조회가 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setScreenViewEffect(log)

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME)) }
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-003 두 화면을 함께 표시해도 가장 나중에 진입한 화면만 남는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-004 겹쳐 표시하는 화면도 화면 조회로 남는다`() {
        val log = mockk<(ScreenViewLog) -> Unit>(relaxed = true)
        val appState = setScreenViewEffect(log)

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }
        composeRule.runOnIdle { appState.backStack.add(MemoHomeFilterNavKey) }

        composeRule.runOnIdle {
            verify(exactly = 1) { log(ScreenViewLog(screenName = MEMO_HOME_FILTER)) }
        }
    }

    @Test
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-014 화면 안에서 연 다이얼로그는 화면 조회로 남지 않는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-005 겹쳐 표시한 화면을 닫으면 아래 화면의 화면 조회가 다시 남는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-006 백그라운드에서 돌아오면 같은 화면의 화면 조회가 다시 남는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-007 화면이 재생성되어 복원되면 화면 조회가 남는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-008 현재 화면이 바뀌지 않으면 화면 조회가 더 남지 않는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-012 화면 조회는 오류 보고로 남지 않는다`() {
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
    fun `TC-SCREEN-VIEW-LOGGING-DOMAIN-013 기록에 실패해도 화면은 그대로 표시된다`() {
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
