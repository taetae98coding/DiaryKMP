package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagMemoFinishedListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-DOMAIN-006 화면이 재생성되어도 정렬 선택과 보던 자리가 그대로다`() {
        val titlePrefix = fixtureText(prefix = "FinishedMemo")
        val memoList = List(MEMO_COUNT) { index -> tagMemo(title = "${titlePrefix}Index$index") }
        val sortFlow = MutableStateFlow(ListSort.DEFAULT)
        val viewModel = mockk<TagMemoFinishedListViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(TagMemoFinishedListUiState())
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.memoPagingData } returns
            MutableStateFlow(tagMemoPagingData(itemList = memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.effect } returns emptyFlow()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                TagMemoFinishedListScreen(
                    navigateUp = {},
                    navigateToMemoDetail = {},
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(TAG_MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-DOMAIN-009 시스템이 앱을 정리한 뒤 다시 만들면 정렬은 처음으로 돌아가고 보던 위치는 다시 보인다`() {
        val titlePrefix = fixtureText(prefix = "FinishedMemo")
        val memoList = List(MEMO_COUNT) { index -> tagMemo(title = "${titlePrefix}Index$index") }
        var nextViewModel = sortableViewModel(memoList = memoList)
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 정렬을 들고 있던 ViewModel도 사라지므로, 복원으로 컴포지션을 다시 만들 때만 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModel = remember { nextViewModel }
            DiaryTheme {
                TagMemoFinishedListScreen(
                    navigateUp = {},
                    navigateToMemoDetail = {},
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(TAG_MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        nextViewModel = sortableViewModel(memoList = memoList)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun sortableViewModel(memoList: List<Memo>): TagMemoFinishedListViewModel {
        val sortFlow = MutableStateFlow(ListSort.DEFAULT)
        val viewModel = mockk<TagMemoFinishedListViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(TagMemoFinishedListUiState())
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.memoPagingData } returns
            MutableStateFlow(tagMemoPagingData(itemList = memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.effect } returns emptyFlow()

        return viewModel
    }

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val MEMO_COUNT = 40
        private const val SCROLL_INDEX = 30
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val DEFAULT_TITLE_SORT = "Title"
    }
}
