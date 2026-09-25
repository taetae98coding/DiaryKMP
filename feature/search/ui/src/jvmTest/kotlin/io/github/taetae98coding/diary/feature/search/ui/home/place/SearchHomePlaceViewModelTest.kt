@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.place

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.failurePagingDataFlow
import io.github.taetae98coding.diary.feature.search.ui.home.searchPlace
import io.github.taetae98coding.diary.feature.search.ui.home.successPagingDataFlowOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SearchHomePlaceViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SEARCH-HOME-FEATURE-003 질의를 바꾸면 바뀐 질의의 결과를 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchPlace(title = "여행 항목"))
                val otherItemList = listOf(searchPlace(title = "회의 항목"))
                val viewModel =
                    viewModel(
                        searchPlaceUseCase(
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

        test("TC-SEARCH-HOME-FEATURE-017 장소 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchPlaceUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("조회한 장소를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchPlace(title = "여행 항목"))
                val viewModel = viewModel(searchPlaceUseCase(successPagingDataFlowOf(itemList)))

                flowOf(viewModel.pagingData.first()).asSnapshot() shouldBe itemList
            }
        }
    }

    public companion object {
        private fun viewModel(searchPlaceUseCase: SearchPlaceUseCase): SearchHomePlaceViewModel =
            SearchHomePlaceViewModel(
                searchPlaceUseCase = searchPlaceUseCase,
                deletePlaceUseCase = mockk(),
                restorePlaceUseCase = mockk(),
            )

        private fun searchPlaceUseCase(flow: Flow<Result<PagingData<Place>>>): SearchPlaceUseCase =
            mockk<SearchPlaceUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        private fun searchPlaceUseCase(queryToItemList: Map<String, List<Place>>): SearchPlaceUseCase =
            mockk<SearchPlaceUseCase>().apply {
                every { this@apply(any()) } answers {
                    flowOf(Result.success(PagingData.from(queryToItemList[firstArg<SearchPlaceUseCase.Parameter>().query].orEmpty())))
                }
            }
    }
}
