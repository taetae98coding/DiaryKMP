@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SearchHomeSortTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SEARCH-HOME-FEATURE-024 한 유형에서 고른 정렬은 다른 유형의 결과 순서를 바꾸지 않는다") {
            runTest(mainDispatcher) {
                val searchMemoUseCase = searchMemoUseCase(successPagingDataFlowOf(emptyList<Memo>()))
                val searchTagUseCase = searchTagUseCase(successPagingDataFlowOf(emptyList<Tag>()))
                val memoViewModel =
                    SearchHomeMemoViewModel(
                        searchMemoUseCase = searchMemoUseCase,
                        finishMemoUseCase = mockk(),
                        restartMemoUseCase = mockk(),
                        deleteMemoUseCase = mockk(),
                        restoreMemoUseCase = mockk(),
                    )
                val tagViewModel =
                    SearchHomeTagViewModel(
                        searchTagUseCase = searchTagUseCase,
                        finishTagUseCase = mockk(),
                        restartTagUseCase = mockk(),
                        deleteTagUseCase = mockk(),
                        restoreTagUseCase = mockk(),
                    )

                memoViewModel.pagingData.test {
                    tagViewModel.pagingData.test {
                        memoViewModel.select(sort = ListSort.RECENTLY_UPDATED)
                        advanceUntilIdle()
                        cancelAndIgnoreRemainingEvents()
                    }
                    cancelAndIgnoreRemainingEvents()
                }

                memoViewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
                tagViewModel.sort.value shouldBe ListSort.TITLE
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = "", sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 0) { searchTagUseCase(parameter = SearchTagUseCase.Parameter(query = "", sort = ListSort.RECENTLY_UPDATED)) }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-025 질의를 바꿔도 그 유형에서 고른 정렬이 유지된다") {
            runTest(mainDispatcher) {
                val searchMemoUseCase = searchMemoUseCase(successPagingDataFlowOf(emptyList<Memo>()))
                val viewModel =
                    SearchHomeMemoViewModel(
                        searchMemoUseCase = searchMemoUseCase,
                        finishMemoUseCase = mockk(),
                        restartMemoUseCase = mockk(),
                        deleteMemoUseCase = mockk(),
                        restoreMemoUseCase = mockk(),
                    )

                viewModel.pagingData.test {
                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)
                    advanceUntilIdle()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()

                    viewModel.updateQuery(OTHER_QUERY)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = QUERY, sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = OTHER_QUERY, sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 0) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = OTHER_QUERY, sort = ListSort.TITLE)) }
            }
        }
    }

    private companion object {
        fun searchMemoUseCase(flow: Flow<Result<PagingData<Memo>>>): SearchMemoUseCase =
            mockk<SearchMemoUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        fun searchTagUseCase(flow: Flow<Result<PagingData<Tag>>>): SearchTagUseCase =
            mockk<SearchTagUseCase>().apply {
                every { this@apply(any()) } returns flow
            }
    }
}
