package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-061 목록 위에 현재 정렬을 표시한다`() {
        setMemoHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-062 정렬 컨트롤을 누르면 세 정렬을 고를 수 있다`() {
        setMemoHomeScaffold(sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule.onAllNodesWithText(DEFAULT_DEFAULT_SORT).onLast().assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-049 제목순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<MemoHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setMemoHomeScaffold(onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.SelectSort(sort = ListSort.TITLE))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-050 최근 수정순을 고르면 그 정렬을 전달한다`() {
        val eventList = mutableListOf<MemoHomeScaffoldEvent>()
        setMemoHomeScaffold(onEvent = eventList::add, sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.SelectSort(sort = ListSort.RECENTLY_UPDATED))
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-052 필터가 적용되어 있어도 정렬을 고를 수 있다`() {
        val eventList = mutableListOf<MemoHomeScaffoldEvent>()
        setMemoHomeScaffold(filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.ClickSort)
    }

    @Test
    fun `정렬 컨트롤은 선택한 정렬의 이름을 표시한다`() {
        setMemoHomeScaffold(sort = ListSort.RECENTLY_UPDATED)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 정렬 이름은 기본순이다`() {
        setMemoHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_DEFAULT_SORT).assertExists()
    }

    private fun setMemoHomeScaffold(
        itemList: List<MemoListItem> = emptyList(),
        filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState(),
        onEvent: (MemoHomeScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.DEFAULT,
    ) {
        val memoPagingData = MutableStateFlow(memoPagingDataOf(itemList))

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    onEvent = onEvent,
                    onMemoListEvent = {},
                    sortSheetState = sortSheetState,
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                    sortProvider = { sort },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val KOREAN_SORT_DESCRIPTION = "목록 정렬"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val KOREAN_DEFAULT_SORT = "기본순"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
    }
}
