@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.dday

import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.search.usecase.SearchDDayUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.appendFailingPagingData
import io.github.taetae98coding.diary.feature.search.ui.home.failurePagingDataFlow
import io.github.taetae98coding.diary.feature.search.ui.home.successPagingDataFlowOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SearchHomeDDayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-0b1f20fb-8af3-4b2e-b580-2732e779f48e 이어서 조회하지 못하면 이미 나타난 결과를 그대로 둔다") {
            runTest(mainDispatcher) {
                val firstPage = listOf(searchDDay(), searchDDay())
                val useCase =
                    mockk<SearchDDayUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchDDayUseCase.Parameter>().query == QUERY) {
                                appendFailingPagingData(firstPage = firstPage).map { pagingData -> Result.success(pagingData) }
                            } else {
                                successPagingDataFlowOf(emptyList())
                            }
                        }
                    }
                val viewModel = viewModel(useCase)

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    val itemList =
                        flowOf(awaitItem()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                            appendScrollWhile { true }
                        }

                    itemList shouldBe firstPage
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-bfe7428a-4630-4061-a412-7e2dfb88cd9a 질의를 바꾸면 바뀐 질의의 결과를 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchDDay())
                val otherItemList = listOf(searchDDay())
                val viewModel =
                    viewModel(
                        searchDDayUseCase(
                            queryToItemList =
                                mapOf(
                                    QUERY to itemList,
                                    OTHER_QUERY to otherItemList,
                                ),
                        ),
                    )

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    viewModel.updateQuery(OTHER_QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe otherItemList

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-07661b59-0122-4916-8758-afa28bf8bdbd 디데이 결과가 보이는 상태에서 질의를 바꿔 다시 조회하지 못하면 이전 결과를 남기지 않는다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchDDay())
                val useCase =
                    mockk<SearchDDayUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchDDayUseCase.Parameter>().query == QUERY) successPagingDataFlowOf(itemList) else failurePagingDataFlow()
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

        test("TC-a463251f-bc66-4f34-b83a-184e76b8e745 디데이 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchDDayUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("조회한 디데이를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchDDay())
                val viewModel = viewModel(searchDDayUseCase(successPagingDataFlowOf(itemList)))

                flowOf(viewModel.pagingData.first()).asSnapshot() shouldBe itemList
            }
        }
    }

    public companion object {
        private fun searchDDay(): DDay = Arb.dday().next().copy(isDeleted = false)

        private fun viewModel(searchDDayUseCase: SearchDDayUseCase): SearchHomeDDayViewModel =
            SearchHomeDDayViewModel(
                searchDDayUseCase = searchDDayUseCase,
                deleteDDayUseCase = mockk(),
                restoreDDayUseCase = mockk(),
            )

        private fun searchDDayUseCase(flow: Flow<Result<PagingData<DDay>>>): SearchDDayUseCase =
            mockk<SearchDDayUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        private fun searchDDayUseCase(queryToItemList: Map<String, List<DDay>>): SearchDDayUseCase =
            mockk<SearchDDayUseCase>().apply {
                every { this@apply(any()) } answers {
                    flowOf(Result.success(PagingData.from(queryToItemList[firstArg<SearchDDayUseCase.Parameter>().query].orEmpty())))
                }
            }
    }
}
