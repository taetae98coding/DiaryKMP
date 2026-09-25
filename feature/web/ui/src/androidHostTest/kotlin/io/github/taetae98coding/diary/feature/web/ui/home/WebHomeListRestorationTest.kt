package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-DOMAIN-007 화면이 재생성되어도 보던 목록 위치를 유지한다`() {
        val webList =
            List(RESTORATION_WEB_COUNT) { index ->
                testWeb(title = "웹-${index.toString().padStart(length = 2, padChar = '0')}")
            }
        val pagingDataFlow = MutableStateFlow(webPagingDataOf(webList))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                WebHomeList(
                    onEvent = {},
                    webPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.onNodeWithTag(WEB_HOME_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(webList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(webList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(webList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(webList.first().detail.title).assertDoesNotExist()
    }

    private companion object {
        private const val RESTORATION_WEB_COUNT = 60
        private const val RESTORATION_SCROLL_INDEX = 50
    }
}
