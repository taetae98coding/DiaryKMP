package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-018 화면이 회전하거나 재생성되어도 필터 선택, 정렬 선택과 보던 자리가 그대로다`() {
        val memoList = memoList()
        // 필터 선택은 기기에 저장된 값으로 조회되므로 그 조회 결과만 정하고, 정렬과 목록 표시는 실제 상태 객체가 맡는다.
        val viewModel =
            realViewModel(
                memoList = memoList,
                existence = MemoExistenceFilter(place = MemoFilterExistence.EXIST),
            )
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { Home(viewModel = viewModel) }
        waitUntilMemoIsDisplayed(memoList)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        scrollList(memoList)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        assertScrolledPosition(memoList)
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-019 앱이 백그라운드에 다녀와도 보던 자리가 그대로다`() {
        val memoList = memoList()
        val viewModel = screenTestViewModel(memoList = memoList)
        composeRule.setContent { Home(viewModel = viewModel) }
        scrollList(memoList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledPosition(memoList)
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-020 메모 상세, 검색이나 완료된 메모 확인에 다녀와도 보던 자리가 그대로다`() {
        val memoList = memoList()
        val viewModel = screenTestViewModel(memoList = memoList)
        var isMemoHomeOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isMemoHomeOnTop) {
                saveableStateHolder.SaveableStateProvider(key = MEMO_HOME_ENTRY_KEY) {
                    Home(viewModel = viewModel)
                }
            }
        }
        scrollList(memoList)

        composeRule.runOnIdle { isMemoHomeOnTop = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isMemoHomeOnTop = true }
        composeRule.waitForIdle()

        assertScrolledPosition(memoList)
    }

    @Composable
    private fun Home(viewModel: MemoHomeViewModel) {
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

    private fun waitUntilMemoIsDisplayed(memoList: List<Memo>) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollList(memoList: List<Memo>) {
        waitUntilMemoIsDisplayed(memoList)
        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
    }

    private fun assertScrolledPosition(memoList: List<Memo>) {
        composeRule.onNodeWithText(memoList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun realViewModel(
        memoList: List<Memo>,
        existence: MemoExistenceFilter,
    ): MemoHomeViewModel {
        val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
        every { getMemoFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val getMemoFilterTagIdUseCase = mockk<GetMemoFilterTagIdUseCase>()
        every { getMemoFilterTagIdUseCase(parameter = Unit) } returns flowOf(Result.success(emptySet()))
        val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
        every { getMemoExistenceFilterUseCase(parameter = Unit) } returns flowOf(Result.success(existence))
        val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
        every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))

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

    private fun screenTestViewModel(
        memoList: List<Memo>,
        sort: ListSort = ListSort.DEFAULT,
        filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState(),
    ): MemoHomeViewModel {
        val viewModel = mockk<MemoHomeViewModel>(relaxed = true)
        every { viewModel.sort } returns MutableStateFlow(sort)
        every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.filterUiState } returns MutableStateFlow(filterUiState)
        every { viewModel.effect } returns emptyFlow()
        return viewModel
    }

    private fun memoList(): List<Memo> {
        val titlePrefix = "MemoHomeMemo${fixtureMonkey.giveMeOne<Int>()}"

        return List(MEMO_COUNT) { index -> fixtureMonkey.memo(title = "${titlePrefix}Index$index") }
    }

    private companion object {
        private const val MEMO_HOME_ENTRY_KEY = "MemoHome"
        private const val MEMO_COUNT = 60
        private const val SCROLLED_INDEX = 50
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
