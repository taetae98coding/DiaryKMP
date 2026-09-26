@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.tag

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.OTHER_QUERY
import io.github.taetae98coding.diary.feature.search.ui.home.QUERY
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

        test("TC-SEARCH-HOME-FEATURE-003 질의를 바꾸면 바뀐 질의의 결과를 노출한다") {
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

        test("TC-SEARCH-HOME-FEATURE-017 태그 결과를 조회하지 못하면 빈 결과를 노출한다") {
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
        // 화면은 유형 결과가 나타나면 지금 질의를 먼저 알리므로, 기본으로 진입할 때의 빈 질의를 알린 상태로 만든다.
        private fun viewModel(
            searchTagUseCase: SearchTagUseCase,
            isQueryShown: Boolean = true,
        ): SearchHomeTagViewModel =
            SearchHomeTagViewModel(
                searchTagUseCase = searchTagUseCase,
                finishTagUseCase = mockk(),
                restartTagUseCase = mockk(),
                deleteTagUseCase = mockk(),
                restoreTagUseCase = mockk(),
            ).also { viewModel -> if (isQueryShown) viewModel.showQuery("") }

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
