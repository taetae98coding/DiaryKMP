package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.feature.memo.ui.refreshableList
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 첫 조회는 앞쪽 메모만 돌려주고 뒤쪽은 아직 준비되지 않은 자리로 남긴다.
// 이어지는 조회는 끝나지 않게 두어, 준비되지 않은 메모가 로딩 상태로 계속 표시되게 한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeDateHeaderPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-44b31ce2-1c9c-40ca-a88d-8653bd80868c 아직 준비되지 않은 메모에는 날짜 헤더가 표시되지 않는다`() {
        val loadedMemoList =
            List(LOADED_MEMO_COUNT) { index ->
                fixtureMonkey.memo(title = "LoadedMemo$index", dateTime = MemoDateTime.AllDay(dateRange = LOADED_DATE..LOADED_DATE))
            }
        setMemoHomeScreen(loadedMemoList = loadedMemoList)
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(loadedMemoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        // 날짜 헤더 하나, 불러온 메모, 준비되지 않은 메모 순서로 놓이므로 마지막 자리는 준비되지 않은 메모다.
        composeRule.refreshableList().performScrollToIndex(LOADED_MEMO_COUNT + PLACEHOLDER_COUNT)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(loadedMemoList.last().detail.title).assertDoesNotExist()
        // 화면 맨 위에 고정되는 헤더는 앞서 불러온 메모의 날짜 헤더이고, 준비되지 않은 메모에 붙은 헤더는 없다.
        composeRule
            .onAllNodes(hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and !hasText(LOADED_DATE_HEADER))
            .fetchSemanticsNodes()
            .shouldBeEmpty()
        composeRule
            .onAllNodes(hasTestTag(MEMO_DATE_HEADER_TEST_TAG))
            .fetchSemanticsNodes()
            .size shouldBeLessThanOrEqual 1
    }

    private fun setMemoHomeScreen(loadedMemoList: List<Memo>) {
        val viewModel = realViewModel(loadedMemoList = loadedMemoList)

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
    }

    private fun realViewModel(loadedMemoList: List<Memo>): MemoHomeViewModel {
        val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
        every { getMemoFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val getMemoFilterTagIdUseCase = mockk<GetMemoFilterTagIdUseCase>()
        every { getMemoFilterTagIdUseCase(parameter = Unit) } returns flowOf(Result.success(emptySet()))
        val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
        every { getMemoExistenceFilterUseCase(parameter = Unit) } returns flowOf(Result.success(MemoExistenceFilter()))
        val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
        every { pageMemoHomeUseCase(parameter = any()) } answers {
            Pager(
                config = PagingConfig(pageSize = LOADED_MEMO_COUNT, initialLoadSize = LOADED_MEMO_COUNT, enablePlaceholders = true),
                pagingSourceFactory = { partiallyLoadedPagingSource(loadedMemoList = loadedMemoList) },
            ).flow.map { pagingData -> Result.success(pagingData) }
        }

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

    private fun partiallyLoadedPagingSource(loadedMemoList: List<Memo>): PagingSource<Int, Memo> {
        val pagingSource = mockk<PagingSource<Int, Memo>>(relaxed = true)
        every { pagingSource.getRefreshKey(any()) } returns null
        every { pagingSource.invalid } returns false
        coEvery { pagingSource.load(any()) } coAnswers {
            if (firstArg<PagingSource.LoadParams<Int>>() is PagingSource.LoadParams.Refresh) {
                PagingSource.LoadResult.Page(
                    data = loadedMemoList,
                    prevKey = null,
                    nextKey = 1,
                    itemsBefore = 0,
                    itemsAfter = PLACEHOLDER_COUNT,
                )
            } else {
                awaitCancellation()
            }
        }
        return pagingSource
    }

    private companion object {
        private const val LOADED_MEMO_COUNT = 5
        private const val PLACEHOLDER_COUNT = 40
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val LOADED_DATE_HEADER = "Jan 2, 2020"
        private val LOADED_DATE = LocalDate(year = 2020, month = 1, day = 2)
    }
}
