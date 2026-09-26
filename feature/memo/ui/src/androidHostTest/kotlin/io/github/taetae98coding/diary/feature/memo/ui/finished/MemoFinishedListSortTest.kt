package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoFinishedListSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-028 목록 위에 현재 정렬을 표시한다`() {
        setMemoFinishedListScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-028 정렬 컨트롤을 누르면 정렬 선택을 연다`() {
        val eventList = mutableListOf<MemoFinishedListScaffoldEvent>()
        setMemoFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(MemoFinishedListScaffoldEvent.ClickSort)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-029 정렬 컨트롤을 누르면 세 정렬을 고를 수 있다`() {
        setMemoFinishedListScaffold(sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule
            .onAllNodes(isSelectable())
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString(separator = "") { text -> text.text } } shouldBe
            listOf(DEFAULT_DEFAULT_SORT, DEFAULT_TITLE_SORT, DEFAULT_RECENTLY_UPDATED_SORT)
        composeRule.onAllNodes(isSelectable())[0].assertIsSelected()
        composeRule.onAllNodes(isSelectable())[1].assertIsNotSelected()
        composeRule.onAllNodes(isSelectable())[2].assertIsNotSelected()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-030 제목순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        assertSelectSort(label = DEFAULT_TITLE_SORT, sort = ListSort.TITLE)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-030 최근 수정순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        assertSelectSort(label = DEFAULT_RECENTLY_UPDATED_SORT, sort = ListSort.RECENTLY_UPDATED)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-030 고른 정렬의 이름을 정렬 컨트롤에 표시한다`() {
        setMemoFinishedListScaffold(sort = ListSort.RECENTLY_UPDATED)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-032 목록이 비어 있으면 정렬 컨트롤을 표시하지 않는다`() {
        setMemoFinishedListScaffold(memoPagingData = memoPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DOMAIN-008 화면이 재생성되어도 정렬 선택과 보던 자리가 그대로다`() {
        val memoList =
            List(RESTORATION_MEMO_COUNT) { index ->
                memo(title = "완료 메모-${index.toString().padStart(length = 2, padChar = '0')}")
            }
        val sortFlow = MutableStateFlow(ListSort.DEFAULT)
        val viewModel = mockk<MemoFinishedListViewModel>(relaxed = true)
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.memoPagingData } returns
            MutableStateFlow(memoPagingDataOf(memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.effect } returns emptyFlow()
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                MemoFinishedListScreen(
                    navigateUp = {},
                    navigateToDetail = {},
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
        composeRule.onNodeWithTag(MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(memoList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DOMAIN-011 시스템이 앱을 정리한 뒤 다시 만들면 정렬은 처음으로 돌아가고 보던 위치는 다시 보인다`() {
        val memoList =
            List(RESTORATION_MEMO_COUNT) { index ->
                memo(title = "완료 메모-${index.toString().padStart(length = 2, padChar = '0')}")
            }
        var nextViewModel = restorationViewModel(memoList = memoList)
        val restorationTester = StateRestorationTester(composeRule)

        // 시스템이 앱을 정리하면 ViewModel도 사라지므로, 복원으로 컴포지션을 다시 만들 때만 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModel = remember { nextViewModel }
            DiaryTheme {
                MemoFinishedListScreen(
                    navigateUp = {},
                    navigateToDetail = {},
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
        composeRule.onNodeWithTag(MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(memoList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        nextViewModel = restorationViewModel(memoList = memoList)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList[RESTORATION_SCROLL_INDEX].detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun restorationViewModel(memoList: List<Memo>): MemoFinishedListViewModel {
        val sortFlow = MutableStateFlow(ListSort.DEFAULT)
        val viewModel = mockk<MemoFinishedListViewModel>(relaxed = true)
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.memoPagingData } returns
            MutableStateFlow(memoPagingDataOf(memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.effect } returns emptyFlow()

        return viewModel
    }

    private fun assertSelectSort(
        label: String,
        sort: ListSort,
    ) {
        val eventList = mutableListOf<MemoFinishedListScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setMemoFinishedListScaffold(onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoFinishedListScaffoldEvent.SelectSort(sort = sort))
        sortSheetState.isVisible shouldBe false
    }

    private fun setMemoFinishedListScaffold(
        memoPagingData: PagingData<MemoListItem> = memoPagingDataOf(listOf(MemoListItem.Content(memo = memo(title = MEMO_TITLE)))),
        onEvent: (MemoFinishedListScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.DEFAULT,
    ) {
        val memoPagingDataFlow = MutableStateFlow(memoPagingData)

        composeRule.setContent {
            DiaryTheme {
                MemoFinishedListScaffold(
                    onEvent = onEvent,
                    onMemoListEvent = {},
                    sortSheetState = sortSheetState,
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    sortProvider = { sort },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun memo(title: String): Memo {
        val memo = fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = fixtureMonkey.giveMeOne<Uuid>())

        return memo.copy(detail = memo.detail.copy(title = title, dateTime = null))
    }

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val RESTORATION_MEMO_COUNT = 40
        private const val RESTORATION_SCROLL_INDEX = 30
        private const val MEMO_TITLE = "MemoFinishedListSortTitle"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
