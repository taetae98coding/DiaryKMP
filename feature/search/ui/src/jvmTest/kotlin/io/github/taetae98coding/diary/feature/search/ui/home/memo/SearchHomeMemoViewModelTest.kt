@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.memo

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.currentUiState
import io.github.taetae98coding.diary.feature.search.ui.home.failurePagingDataFlow
import io.github.taetae98coding.diary.feature.search.ui.home.searchMemo
import io.github.taetae98coding.diary.feature.search.ui.home.successPagingDataFlowOf
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

        test("TC-346988d0-4bef-426f-84c5-0f9054e4072e 질의를 입력하면 그 질의로 조회한 결과를 노출한다") {
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

        test("TC-bfe7428a-4630-4061-a412-7e2dfb88cd9a 질의를 바꾸면 바뀐 질의의 결과를 노출한다") {
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

        test("TC-c4fe9d3f-01c4-4b1a-a7bb-2fd1ca334fdb 질의를 비우면 결과가 비워진다") {
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

        test("TC-9dc996e0-4713-42ff-825c-75ab1f1e7c8d 질의를 공백만 남기면 기다리지 않고 곧바로 결과가 비워진다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val viewModel = viewModel(searchMemoUseCase(queryToMemoList = mapOf(QUERY to memoList)))

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe memoList

                    viewModel.updateQuery(BLANK_QUERY)
                    runCurrent()

                    currentUiState(viewModel).appliedQuery shouldBe BLANK_QUERY
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-9ca2b6b2-0572-4388-84c2-b00f983d3a92 메모리 정리 뒤 새로 만든 유형 결과는 복원한 질의를 입력을 멈춘 뒤와 같이 반영한다") {
            runTest(mainDispatcher) {
                val memoList = listOf(searchMemo(title = "여름 여행 계획"))
                val viewModel = viewModel(searchMemoUseCase(queryToMemoList = mapOf(QUERY to memoList)))
                backgroundScope.launch { viewModel.pagingData.collect {} }

                viewModel.uiState.map { uiState -> uiState.appliedQuery }.distinctUntilChanged().test {
                    awaitItem() shouldBe ""
                    viewModel.updateQuery(QUERY)
                    runCurrent()
                    expectNoEvents()

                    advanceTimeBy(INPUT_IDLE_DELAY)
                    runCurrent()
                    awaitItem() shouldBe QUERY
                }
            }
        }

        test("TC-07661b59-0122-4916-8758-afa28bf8bdbd 메모 결과가 보이는 상태에서 질의를 바꿔 다시 조회하지 못하면 이전 결과를 남기지 않는다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchMemo(title = "여름 여행 계획"))
                val useCase =
                    mockk<SearchMemoUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchMemoUseCase.Parameter>().query == QUERY) successPagingDataFlowOf(itemList) else failurePagingDataFlow()
                        }
                    }
                val viewModel = viewModel(useCase)

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    viewModel.updateQuery(OTHER_QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-a463251f-bc66-4f34-b83a-184e76b8e745 메모 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchMemoUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-55b41830-5dd2-442f-ad02-4fb73a8e0e4f 저장된 항목이 바뀌면 보고 있는 유형의 결과가 갱신된다") {
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
        private const val BLANK_QUERY = "   "

        private fun viewModel(searchMemoUseCase: SearchMemoUseCase): SearchHomeMemoViewModel =
            SearchHomeMemoViewModel(
                searchMemoUseCase = searchMemoUseCase,
                finishMemoUseCase = mockk(),
                restartMemoUseCase = mockk(),
                deleteMemoUseCase = mockk(),
                restoreMemoUseCase = mockk(),
            )

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
