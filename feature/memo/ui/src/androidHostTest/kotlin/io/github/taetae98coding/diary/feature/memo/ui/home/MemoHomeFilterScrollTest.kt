package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
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
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 필터를 바꾸면 목록 조회를 새 조건으로 다시 하고, 좁힌 결과가 목록에 놓일 때 목록을 처음으로 되돌린다.
// 필터 선택과 조회 결과는 저장소에서 오는 값이므로 그 흐름만 테스트가 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeFilterScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-059 유무 필터 축을 바꾸면 목록을 처음부터 다시 본다`() {
        val environment = setScrolledMemoHome()

        changeQuery(environment = environment, filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(place = MemoFilterExistence.EXIST)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 선택되어 있지 않은 태그를 선택하면 목록을 처음부터 다시 본다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val environment = setScrolledMemoHome()

        changeQuery(environment = environment, filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 선택되어 있는 태그의 선택을 해제하면 목록을 처음부터 다시 본다`() {
        val firstTagId = fixtureMonkey.giveMeOne<Uuid>()
        val secondTagId = fixtureMonkey.giveMeOne<Uuid>()
        val environment =
            setScrolledMemoHome(
                filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(firstTagId, secondTagId), storedTagIdSet = setOf(firstTagId, secondTagId)),
            )

        changeQuery(environment = environment, filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(firstTagId), storedTagIdSet = setOf(firstTagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 선택을 전체 해제하면 목록을 처음부터 다시 본다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val environment = setScrolledMemoHome(filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        changeQuery(environment = environment, filterUiState = MemoHomeScaffoldFilterUiState())

        assertBackToTop(environment)
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-017 이미 고른 값을 다시 고르면 목록 위치를 유지한다`() {
        val filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(place = MemoFilterExistence.EXIST))
        val environment = setScrolledMemoHome(filterUiState = filterUiState)

        // 같은 값을 다시 고르면 필터 선택은 그대로이고, 같은 조건의 조회 결과가 다시 전달될 뿐이다.
        composeRule.runOnIdle {
            environment.filterUiStateFlow.value = filterUiState.copy()
            environment.pagingDataFlow.value = memoPagingDataOf(environment.memoList.map { memo -> MemoListItem.Content(memo = memo) })
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(environment.memoList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                environment.memoList
                    .first()
                    .detail.title,
            ).assertDoesNotExist()
    }

    private fun setScrolledMemoHome(filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState()): Environment {
        val memoList = List(MEMO_COUNT) { index -> fixtureMonkey.memo(title = "$TITLE_PREFIX${fixtureMonkey.giveMeOne<Int>()}Index$index") }
        val filterUiStateFlow = MutableStateFlow(filterUiState)
        val pagingDataFlow = MutableStateFlow(memoPagingDataOf(memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        val viewModel = mockk<MemoHomeViewModel>(relaxed = true)
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)
        every { viewModel.memoPagingData } returns pagingDataFlow
        every { viewModel.filterUiState } returns filterUiStateFlow
        every { viewModel.effect } returns emptyFlow()
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
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()

        return Environment(memoList = memoList, filterUiStateFlow = filterUiStateFlow, pagingDataFlow = pagingDataFlow)
    }

    // 필터를 바꾼 뒤 좁힌 결과가 이어서 도착한다. 좁힌 결과에도 보던 메모가 남아 있어 그 자리를 따라가지 않는지 확인할 수 있다.
    private fun changeQuery(
        environment: Environment,
        filterUiState: MemoHomeScaffoldFilterUiState,
    ) {
        composeRule.runOnIdle { environment.filterUiStateFlow.value = filterUiState }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = memoPagingDataOf(environment.narrowedList.map { memo -> MemoListItem.Content(memo = memo) }) }
        composeRule.waitForIdle()
    }

    private fun assertBackToTop(environment: Environment) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(
                    environment.narrowedList
                        .first()
                        .detail.title,
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithText(
                environment.narrowedList
                    .first()
                    .detail.title,
            ).assertIsDisplayed()
        composeRule.onNodeWithText(environment.memoList[SCROLLED_INDEX].detail.title).assertDoesNotExist()
    }

    private class Environment(
        val memoList: List<Memo>,
        val filterUiStateFlow: MutableStateFlow<MemoHomeScaffoldFilterUiState>,
        val pagingDataFlow: MutableStateFlow<PagingData<MemoListItem>>,
    ) {
        // 보던 메모가 들어 있는 좁힌 결과다.
        val narrowedList: List<Memo> = memoList.filterIndexed { index, _ -> index % 2 == 0 }
    }

    private companion object {
        private const val MEMO_COUNT = 60
        private const val SCROLLED_INDEX = 50
        private const val TIMEOUT_MILLIS = 5_000L
        private const val TITLE_PREFIX = "FilterScrollMemo"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
