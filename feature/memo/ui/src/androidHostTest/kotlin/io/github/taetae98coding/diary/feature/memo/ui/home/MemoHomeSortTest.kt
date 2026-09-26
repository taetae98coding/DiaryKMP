package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

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
        val sortSheetState = DialogState(isVisible = true)
        setMemoHomeScaffold(onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.SelectSort(sort = ListSort.RECENTLY_UPDATED))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-052 정렬을 바꿔도 필터로 걸러진 같은 메모가 순서만 바뀌어 표시된다`() {
        val titlePrefix = "SortMemo${fixtureMonkey.giveMeOne<Int>()}"
        val firstMemo = fixtureMonkey.memo(title = "${titlePrefix}First")
        val secondMemo = fixtureMonkey.memo(title = "${titlePrefix}Second")
        val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
        every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.from(listOf(firstMemo, secondMemo))))
        every { pageMemoHomeUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns
            flowOf(Result.success(PagingData.from(listOf(secondMemo, firstMemo))))
        val viewModel =
            realViewModel(
                pageMemoHomeUseCase = pageMemoHomeUseCase,
                existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST),
            )
        composeRule.setContent {
            DiaryTheme {
                MemoHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    navigateToSearch = {},
                    componentVisibleProvider = { MemoHomeScaffoldComponentVisible() },
                    listState = rememberLazyListState(),
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        waitUntilAbove(upperTitle = firstMemo.detail.title, lowerTitle = secondMemo.detail.title)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        waitUntilAbove(upperTitle = secondMemo.detail.title, lowerTitle = firstMemo.detail.title)

        composeRule.onAllNodesWithTag(MEMO_CARD_TEST_TAG).assertCountEquals(2)
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
    }

    private fun waitUntilAbove(
        upperTitle: String,
        lowerTitle: String,
    ) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            val upper = composeRule.onAllNodesWithText(upperTitle).fetchSemanticsNodes().firstOrNull()
            val lower = composeRule.onAllNodesWithText(lowerTitle).fetchSemanticsNodes().firstOrNull()

            upper != null && lower != null && upper.positionInRoot.y < lower.positionInRoot.y
        }
    }

    private fun realViewModel(
        pageMemoHomeUseCase: PageMemoHomeUseCase,
        existence: MemoExistenceFilter,
    ): MemoHomeViewModel {
        val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
        every { getMemoFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val getMemoFilterTagIdUseCase = mockk<GetMemoFilterTagIdUseCase>()
        every { getMemoFilterTagIdUseCase(parameter = Unit) } returns flowOf(Result.success(emptySet()))
        val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
        every { getMemoExistenceFilterUseCase(parameter = Unit) } returns flowOf(Result.success(existence))

        return MemoHomeViewModel(
            getMemoFilterUseCase = getMemoFilterUseCase,
            getMemoFilterTagIdUseCase = getMemoFilterTagIdUseCase,
            getMemoExistenceFilterUseCase = getMemoExistenceFilterUseCase,
            pageMemoHomeUseCase = pageMemoHomeUseCase,
            finishMemoUseCase = mockk(),
            restartMemoUseCase = mockk(),
            deleteMemoUseCase = mockk(),
            restoreMemoUseCase = mockk(),
        )
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-063 목록이 비어 있으면 정렬 컨트롤 없이 완료된 메모 확인 동작만 남는다`() {
        setMemoHomeScaffold(memoPagingData = memoPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `목록을 처음 준비하느라 메모가 아직 없으면 정렬 컨트롤을 표시하지 않는다`() {
        setMemoHomeScaffold(memoPagingData = loadingMemoPagingData())

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
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
        memoPagingData: PagingData<MemoListItem> = memoPagingDataOf(listOf(memoListItem())),
        filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState(),
        onEvent: (MemoHomeScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.DEFAULT,
    ) {
        val memoPagingDataFlow = MutableStateFlow(memoPagingData)

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    onEvent = onEvent,
                    onMemoListEvent = {},
                    sortSheetState = sortSheetState,
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                    sortProvider = { sort },
                )
            }
        }
    }

    private fun memoListItem(): MemoListItem =
        MemoListItem.Content(
            memo =
                fixtureMonkey
                    .giveMeKotlinBuilder<Memo>()
                    .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = null))
                    .setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample(),
        )

    private companion object {
        private const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val KOREAN_SORT_DESCRIPTION = "목록 정렬"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val KOREAN_DEFAULT_SORT = "기본순"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
