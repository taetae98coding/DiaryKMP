@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.tag

import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.appendFailingPagingData
import io.github.taetae98coding.diary.feature.search.ui.home.failurePagingDataFlow
import io.github.taetae98coding.diary.feature.search.ui.home.searchTag
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SearchHomeTagViewModelTest : FunSpec() {
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
                val firstPage = listOf(searchTag(), searchTag())
                val useCase =
                    mockk<SearchTagUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchTagUseCase.Parameter>().query == QUERY) {
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
                val itemList = listOf(searchTag(title = "여행 항목"))
                val otherItemList = listOf(searchTag(title = "회의 항목"))
                val viewModel =
                    viewModel(
                        searchTagUseCase(
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

        test("TC-07661b59-0122-4916-8758-afa28bf8bdbd 태그 결과가 보이는 상태에서 질의를 바꿔 다시 조회하지 못하면 이전 결과를 남기지 않는다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchTag(title = "여행 항목"))
                val useCase =
                    mockk<SearchTagUseCase>().apply {
                        every { this@apply(any()) } answers {
                            if (firstArg<SearchTagUseCase.Parameter>().query == QUERY) successPagingDataFlowOf(itemList) else failurePagingDataFlow()
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

        test("TC-a463251f-bc66-4f34-b83a-184e76b8e745 태그 결과를 조회하지 못하면 빈 결과를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(searchTagUseCase(failurePagingDataFlow()))

                flowOf(viewModel.pagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("조회한 태그를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val itemList = listOf(searchTag(title = "여행 항목"))
                val viewModel = viewModel(searchTagUseCase(successPagingDataFlowOf(itemList)))

                flowOf(viewModel.pagingData.first()).asSnapshot() shouldBe itemList
            }
        }
    }

    public companion object {
        private fun viewModel(searchTagUseCase: SearchTagUseCase): SearchHomeTagViewModel =
            SearchHomeTagViewModel(
                searchTagUseCase = searchTagUseCase,
                finishTagUseCase = mockk(),
                restartTagUseCase = mockk(),
                deleteTagUseCase = mockk(),
                restoreTagUseCase = mockk(),
            )

        private fun searchTagUseCase(flow: Flow<Result<PagingData<Tag>>>): SearchTagUseCase =
            mockk<SearchTagUseCase>().apply {
                every { this@apply(any()) } returns flow
            }

        private fun searchTagUseCase(queryToItemList: Map<String, List<Tag>>): SearchTagUseCase =
            mockk<SearchTagUseCase>().apply {
                every { this@apply(any()) } answers {
                    flowOf(Result.success(PagingData.from(queryToItemList[firstArg<SearchTagUseCase.Parameter>().query].orEmpty())))
                }
            }
    }
}
