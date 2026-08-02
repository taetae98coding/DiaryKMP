package io.github.taetae98coding.diary.feature.tag.ui.detail

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
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailMemoRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<MemoListEvent>()
        setTagDetailMemoTab(
            pagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = MEMO_TITLE)))),
            onMemoListEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.Refresh)
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-002 진행 표시 상태이면 진행 표시가 나타난다`() {
        setTagDetailMemoTab(memoListUiStateProvider = { MemoListUiState(isRefreshing = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-004 진행 표시 상태가 아니면 진행 표시가 나타나지 않는다`() {
        setTagDetailMemoTab(memoListUiStateProvider = { MemoListUiState(isRefreshing = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-005 동기화가 끝나면 진행 표시가 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setTagDetailMemoTab(memoListUiStateProvider = { MemoListUiState(isRefreshing = isRefreshing.value) })
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setTagDetailMemoTab(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
        onMemoListEvent: (MemoListEvent) -> Unit = {},
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = onMemoListEvent,
                    modifier = Modifier.fillMaxSize(),
                    state = rememberMemoListState(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = memoListUiStateProvider,
                )
            }
        }
    }

    private companion object {
        const val MEMO_TITLE = "TagMemoTitle"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
