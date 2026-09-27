package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 화면을 꾸민 뒤 저장된 웹 항목이 바뀌어 목록 조회 결과가 다시 전달되는 전환을 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeListUpdateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-004 웹 항목이 새로 저장되면 별도 조작 없이 목록에 나타난다`() {
        val first = testWeb(title = FIRST_TITLE)
        val second = testWeb(title = SECOND_TITLE)
        val pagingDataFlow = MutableStateFlow(webPagingDataOf(listOf(first)))
        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = {},
                    webPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(SECOND_TITLE).assertDoesNotExist()

        composeRule.runOnIdle { pagingDataFlow.value = webPagingDataOf(listOf(first, second)) }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(SECOND_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
    }

    private companion object {
        const val FIRST_TITLE = "WebHomeUpdateFirst"
        const val SECOND_TITLE = "WebHomeUpdateSecond"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
