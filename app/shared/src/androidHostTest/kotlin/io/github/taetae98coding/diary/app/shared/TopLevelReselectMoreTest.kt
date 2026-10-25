@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
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
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// `더보기` 화면은 다시 선택을 전달받지 않으므로, 보던 자리가 남는지는 AppState가 전환 이력을 바꾸는 방식과 entry별 저장 상태가 정한다.
// 실제 AppState의 다시 선택과 앱이 쓰는 저장 상태 decorator를 그대로 두고, 목적지 화면의 본문만 아래로 이동할 수 있는 대역으로 바꿔 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TopLevelReselectMoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-a90e5eda-5d5e-4516-be40-57c0db856883 더보기를 다시 선택해도 보던 자리를 유지한다`() {
        val appState =
            AppState(
                backStack = NavBackStack(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
            )
        setNavDisplay(backStack = appState.backStack)
        composeRule.onNodeWithTag(MORE_LIST_TEST_TAG).performScrollToIndex(LAST_INDEX)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(moreRow(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.runOnIdle { appState.navigateTo(TopLevelNavigation.More) }
        composeRule.waitForIdle()

        appState.backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key)
        composeRule.onNodeWithText(moreRow(index = LAST_INDEX)).assertIsDisplayed()
        composeRule.onAllNodesWithText(moreRow(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true
    }

    private fun setNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<CalendarHomeNavKey> { Text(text = DEFAULT_CONTENT) }
                            entry<MoreHomeNavKey> {
                                LazyColumn(
                                    modifier = Modifier.testTag(MORE_LIST_TEST_TAG),
                                    state = rememberLazyListState(),
                                ) {
                                    items(count = MORE_ROW_COUNT) { index -> Text(text = moreRow(index = index)) }
                                }
                            }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val MORE_LIST_TEST_TAG = "MoreList"
        const val MORE_ROW_COUNT = 60
        const val LAST_INDEX = MORE_ROW_COUNT - 1
        const val DEFAULT_CONTENT = "DefaultContent"

        fun moreRow(index: Int): String = "MoreRow$index"
    }
}
