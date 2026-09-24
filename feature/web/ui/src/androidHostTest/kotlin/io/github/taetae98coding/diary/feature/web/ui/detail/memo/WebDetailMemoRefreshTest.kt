package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemo
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemoPagingData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailMemoRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(
            pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = MEMO_TITLE)))),
            onMemoListEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithTag(WEB_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.Refresh)
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-002 진행 표시 상태이면 진행 표시가 나타난다`() {
        setMemoTab(memoListUiStateProvider = { MemoListUiState(isRefreshing = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-005 동기화가 끝나면 진행 표시가 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setMemoTab(memoListUiStateProvider = { MemoListUiState(isRefreshing = isRefreshing.value) })
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setMemoTab(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
        onMemoListEvent: (MemoListEvent) -> Unit = {},
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                WebDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = onMemoListEvent,
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = memoListUiStateProvider,
                )
            }
        }
    }

    private companion object {
        const val MEMO_TITLE = "WebMemoRefreshTitle"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
