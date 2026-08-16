@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SearchHomeMemoViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SEARCH-HOME-FEATURE-002 질의를 입력하면 그 질의로 조회한 결과를 노출한다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val viewModel = viewModel(searchMemoUseCase(queryToMemoList = mapOf(QUERY to memoList)))

                viewModel.pagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe memoList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-003 질의를 바꾸면 바뀐 질의의 결과를 노출한다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val otherMemoList = listOf(searchMemo(title = "주간 회의록"))
                val viewModel =
                    viewModel(
                        searchMemoUseCase(
                            queryToMemoList =
                                mapOf(
                                    QUERY to memoList,
                                    OTHER_QUERY to otherMemoList,
                                ),
                        ),
                    )

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe memoList

                    viewModel.updateQuery(OTHER_QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe otherMemoList

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-004 질의를 비우면 결과가 비워진다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val viewModel = viewModel(searchMemoUseCase(queryToMemoList = mapOf(QUERY to memoList)))

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe memoList

                    viewModel.updateQuery("")
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-017 메모 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchMemoUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-SEARCH-HOME-DOMAIN-011 저장된 항목이 바뀌면 보고 있는 유형의 결과가 갱신된다") {
            runTest(mainDispatcher) {
                val first = searchMemo(title = "여름 여행 계획")
                val second = searchMemo(title = "여행 준비물")
                val memoFlow = MutableStateFlow(Result.success(PagingData.from(listOf(first))))
                val viewModel = viewModel(searchMemoUseCase(memoFlow))

                viewModel.pagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(first)

                    memoFlow.value = Result.success(PagingData.from(listOf(first, second)))
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(first, second)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("조회한 메모를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val viewModel = viewModel(searchMemoUseCase(successPagingDataFlowOf(memoList)))

                flowOf(viewModel.pagingData.first()).asSnapshot() shouldBe memoList
            }
        }

        test("처음에는 빈 질의로 조회한다") {
            runTest(mainDispatcher) {
                val searchMemoUseCase = searchMemoUseCase(successPagingDataFlowOf(emptyList<Memo>()))
                val viewModel = viewModel(searchMemoUseCase)

                viewModel.pagingData.first()

                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = "", sort = ListSort.TITLE)) }
            }
        }

        test("질의를 바꾸면 바뀐 질의로 다시 조회한다") {
            runTest(mainDispatcher) {
                val searchMemoUseCase = searchMemoUseCase(successPagingDataFlowOf(emptyList<Memo>()))
                val viewModel = viewModel(searchMemoUseCase)

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = QUERY, sort = ListSort.TITLE)) }
            }
        }
    }

    public companion object {
        private fun viewModel(searchMemoUseCase: SearchMemoUseCase): SearchHomeMemoViewModel = SearchHomeMemoViewModel(searchMemoUseCase = searchMemoUseCase)

        private fun searchMemoUseCase(flow: Flow<Result<PagingData<Memo>>>): SearchMemoUseCase =
            mockk<SearchMemoUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        private fun searchMemoUseCase(queryToMemoList: Map<String, List<Memo>>): SearchMemoUseCase =
            mockk<SearchMemoUseCase>().apply {
                every { this@apply(any()) } answers {
                    flowOf(Result.success(PagingData.from(queryToMemoList[firstArg<SearchMemoUseCase.Parameter>().query].orEmpty())))
                }
            }
    }
}
