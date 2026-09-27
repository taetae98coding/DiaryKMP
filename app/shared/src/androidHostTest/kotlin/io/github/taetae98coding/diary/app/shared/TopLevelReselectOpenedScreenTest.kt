@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 이어진 화면을 닫을 때 목적지 화면의 보던 자리가 남는지는 AppState가 전환 이력을 바꾸는 방식과 entry별 저장 상태가 함께 정한다.
// 실제 AppState의 다시 선택과 앱이 쓰는 저장 상태 decorator를 그대로 두고, 목적지와 열어 둔 화면의 본문만 대역으로 바꿔 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TopLevelReselectOpenedScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-012 메모 화면을 덮는 세부 화면을 닫고 목록의 보던 자리를 유지한다`() {
        assertReselectKeepsListPosition(openedScreen = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-012 메모 화면 위에 겹쳐 표시하는 화면을 닫고 목록의 보던 자리를 유지한다`() {
        assertReselectKeepsListPosition(openedScreen = MemoHomeFilterNavKey)
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp")
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-012 목록과 함께 표시하도록 연 메모 상세를 닫고 목록의 보던 자리를 유지한다`() {
        assertReselectKeepsListPosition(
            openedScreen = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
            isListDetail = true,
        )
    }

    private fun assertReselectKeepsListPosition(
        openedScreen: ScreenNavKey,
        isListDetail: Boolean = false,
    ) {
        val appState =
            AppState(
                backStack = NavBackStack(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
            )
        setNavDisplay(backStack = appState.backStack, isListDetail = isListDetail)
        composeRule.onNodeWithTag(MEMO_LIST_TEST_TAG).performScrollToIndex(LAST_INDEX)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true
        composeRule.runOnIdle { appState.backStack.add(openedScreen) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OPENED_CONTENT).assertIsDisplayed()

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.Memo) }
        composeRule.waitForIdle()

        appState.backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key)
        composeRule.onNodeWithText(OPENED_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(memoTitle(index = LAST_INDEX)).assertIsDisplayed()
        composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true
    }

    // 목록과 함께 표시하는 경우는 앱이 쓰는 목록·상세 scene 전략과 두 영역의 배치 정보를 그대로 둔다.
    private fun setNavDisplay(
        backStack: NavBackStack<ScreenNavKey>,
        isListDetail: Boolean,
    ) {
        val listMetadata = if (isListDetail) ListDetailSceneStrategy.listPane(sceneKey = MemoHomeNavKey) else emptyMap()
        val detailMetadata = if (isListDetail) ListDetailSceneStrategy.detailPane(sceneKey = MemoHomeNavKey) else emptyMap()

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = if (isListDetail) listOf(rememberDiaryListDetailSceneStrategy()) else emptyList(),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<CalendarHomeNavKey> { Text(text = DEFAULT_CONTENT) }
                            entry<MemoHomeNavKey>(metadata = listMetadata) {
                                val listState = rememberLazyListState()

                                LazyColumn(
                                    modifier = Modifier.testTag(MEMO_LIST_TEST_TAG),
                                    state = listState,
                                ) {
                                    items(count = MEMO_COUNT) { index -> Text(text = memoTitle(index = index)) }
                                }
                            }
                            entry<MemoDetailNavKey>(metadata = detailMetadata) { Text(text = OPENED_CONTENT) }
                            entry<MemoHomeFilterNavKey> { Text(text = OPENED_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val MEMO_LIST_TEST_TAG = "MemoList"
        const val MEMO_COUNT = 60
        const val LAST_INDEX = MEMO_COUNT - 1
        const val DEFAULT_CONTENT = "DefaultContent"
        const val OPENED_CONTENT = "OpenedContent"

        fun memoTitle(index: Int): String = "MemoTitle$index"
    }
}
