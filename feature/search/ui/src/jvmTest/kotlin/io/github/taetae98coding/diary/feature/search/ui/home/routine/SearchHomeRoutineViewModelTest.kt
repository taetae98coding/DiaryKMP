@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.routine

import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.search.usecase.SearchRoutineUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.appendFailingPagingData
import io.github.taetae98coding.diary.feature.search.ui.home.failurePagingDataFlow
import io.github.taetae98coding.diary.feature.search.ui.home.successPagingDataFlowOf
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class SearchHomeRoutineViewModelTest : FunSpec() {
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
                val firstPage = listOf(searchRoutine(), searchRoutine())
                val useCase =
                    mockk<SearchRoutineUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchRoutineUseCase.Parameter>().query == QUERY) {
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
                val itemList = listOf(searchRoutine())
                val otherItemList = listOf(searchRoutine())
                val viewModel =
                    viewModel(
                        searchRoutineUseCase(
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

        test("TC-07661b59-0122-4916-8758-afa28bf8bdbd 루틴 결과가 보이는 상태에서 질의를 바꿔 다시 조회하지 못하면 이전 결과를 남기지 않는다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchRoutine())
                val useCase =
                    mockk<SearchRoutineUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchRoutineUseCase.Parameter>().query == QUERY) successPagingDataFlowOf(itemList) else failurePagingDataFlow()
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

        test("TC-a463251f-bc66-4f34-b83a-184e76b8e745 루틴 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchRoutineUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("조회한 루틴을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchRoutine())
                val viewModel = viewModel(searchRoutineUseCase(successPagingDataFlowOf(itemList)))

                flowOf(viewModel.pagingData.first()).asSnapshot() shouldBe itemList
            }
        }

        test("오늘이 정해지기 전에는 루틴 결과를 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = searchRoutineUseCase(successPagingDataFlowOf(listOf(searchRoutine())))
                val viewModel = SearchHomeRoutineViewModel(searchRoutineUseCase = useCase, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())

                viewModel.pagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 0) { useCase(parameter = any()) }
            }
        }

        test("TC-fb0e7fba-331b-4e48-a5b3-b0797711bf95 오늘이 바뀌면 같은 질의를 바뀐 오늘로 다시 조회한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val nextDay = today.plus(1, DateTimeUnit.DAY)
                val todayList = listOf(searchRoutine(), searchRoutine())
                val nextDayList = todayList.reversed()
                val useCase =
                    mockk<SearchRoutineUseCase>().apply {
                        every { this@apply(any()) } returns successPagingDataFlowOf(emptyList())
                        every { this@apply(SearchRoutineUseCase.Parameter(query = QUERY, today = today)) } returns successPagingDataFlowOf(todayList)
                        every { this@apply(SearchRoutineUseCase.Parameter(query = QUERY, today = nextDay)) } returns successPagingDataFlowOf(nextDayList)
                    }
                val viewModel = SearchHomeRoutineViewModel(searchRoutineUseCase = useCase, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe todayList

                    viewModel.updateToday(today = nextDay)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe nextDayList

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-d0b9a5ea-0346-4f67-8d8d-0febfbf57a92 같은 오늘을 다시 받으면 루틴 결과를 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val useCase = searchRoutineUseCase(successPagingDataFlowOf(listOf(searchRoutine())))
                val viewModel = SearchHomeRoutineViewModel(searchRoutineUseCase = useCase, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.pagingData.test {
                    awaitItem()

                    viewModel.updateToday(today = today)
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }
    }

    public companion object {
        private fun searchRoutine(): RoutineSummary = Arb.routineSummary().next().let { summary -> summary.copy(routine = summary.routine.copy(isDeleted = false)) }

        private fun viewModel(searchRoutineUseCase: SearchRoutineUseCase): SearchHomeRoutineViewModel =
            SearchHomeRoutineViewModel(
                searchRoutineUseCase = searchRoutineUseCase,
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = mockk(),
                restoreRoutineUseCase = mockk(),
            ).apply { updateToday(today = Arb.kotlinxLocalDate().next()) }

        private fun searchRoutineUseCase(flow: Flow<Result<PagingData<RoutineSummary>>>): SearchRoutineUseCase =
            mockk<SearchRoutineUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        private fun searchRoutineUseCase(queryToItemList: Map<String, List<RoutineSummary>>): SearchRoutineUseCase =
            mockk<SearchRoutineUseCase>().apply {
                every { this@apply(any()) } answers {
                    flowOf(Result.success(PagingData.from(queryToItemList[firstArg<SearchRoutineUseCase.Parameter>().query].orEmpty())))
                }
            }
    }
}
