@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.SelectDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectAllDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

class DDayHomeFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-46f30852-bdb4-4dd8-86c6-633e7c4a34bc 검색어 없이 조회한 선택할 수 있는 태그를 순서대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = List(3) { Arb.tag().next() }
                val pageTagUseCase = mockk<PageTagUseCase>()
                every { pageTagUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(tagList)))
                val viewModel = viewModel(pageTagUseCase = pageTagUseCase)

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList

                verify(exactly = 1) { pageTagUseCase(parameter = "") }
            }
        }

        test("TC-c8172f51-2b24-4cfe-8e79-b57387a8bbe3 필터를 다시 열면 저장된 두 태그 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val selectedTagList = List(2) { Arb.tag().next() }
                val filterFlow = flowOf(Result.success(selectedTagList))
                val expected = DDayHomeFilterUiState(selectedTagIdSet = selectedTagList.map { tag -> tag.id }.toSet())

                repeat(2) {
                    val viewModel = viewModel(filterFlow = filterFlow)

                    viewModel.uiState.test {
                        awaitItem() shouldBe DDayHomeFilterUiState()
                        advanceUntilIdle()
                        awaitItem() shouldBe expected
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-4407c962-2b91-48d0-a808-15a53c159e25 태그를 선택하면 선택 저장을 한 번 요청한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val selectUseCase = mockk<SelectDDayFilterTagUseCase>()
                coEvery { selectUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(selectDDayFilterTagUseCase = selectUseCase)

                viewModel.selectTag(id = tag.id)
                advanceUntilIdle()

                coVerify(exactly = 1) { selectUseCase(parameter = tag.id) }
            }
        }

        test("TC-ed31695f-825e-45b8-8bbf-4f02a5fa1954 필터 선택을 저장하지 못하면 바꾸기 전의 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagA = Arb.tag().next()
                val tagB = Arb.tag().next()
                val selectUseCase = mockk<SelectDDayFilterTagUseCase>()
                coEvery { selectUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(listOf(tagA))),
                        selectDDayFilterTagUseCase = selectUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayHomeFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe DDayHomeFilterUiState(selectedTagIdSet = setOf(tagA.id))

                    viewModel.selectTag(id = tagB.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe DDayHomeFilterUiState(selectedTagIdSet = setOf(tagA.id))
                }

                coVerify(exactly = 1) { selectUseCase(parameter = tagB.id) }
            }
        }

        test("TC-fa768c1e-19a6-4bf1-9773-caf996cdb89e 선택 전체 해제를 실행하면 전체 해제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val unselectAllUseCase = mockk<UnselectAllDDayFilterTagUseCase>()
                coEvery { unselectAllUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = viewModel(unselectAllDDayFilterTagUseCase = unselectAllUseCase)

                viewModel.unselectAllTag()
                advanceUntilIdle()

                coVerify(exactly = 1) { unselectAllUseCase(parameter = Unit) }
            }
        }

        test("태그 선택을 해제하면 선택 해제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val unselectUseCase = mockk<UnselectDDayFilterTagUseCase>()
                coEvery { unselectUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(unselectDDayFilterTagUseCase = unselectUseCase)

                viewModel.unselectTag(id = tag.id)
                advanceUntilIdle()

                coVerify(exactly = 1) { unselectUseCase(parameter = tag.id) }
            }
        }
    }

    private fun viewModel(
        pageTagUseCase: PageTagUseCase =
            mockk<PageTagUseCase>().also { useCase ->
                every { useCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        selectDDayFilterTagUseCase: SelectDDayFilterTagUseCase = mockk(),
        unselectDDayFilterTagUseCase: UnselectDDayFilterTagUseCase = mockk(),
        unselectAllDDayFilterTagUseCase: UnselectAllDDayFilterTagUseCase = mockk(),
    ): DDayHomeFilterViewModel {
        val getDDayFilterUseCase = mockk<GetDDayFilterUseCase>()
        every { getDDayFilterUseCase(parameter = Unit) } returns filterFlow

        return DDayHomeFilterViewModel(
            pageTagUseCase = pageTagUseCase,
            getDDayFilterUseCase = getDDayFilterUseCase,
            selectDDayFilterTagUseCase = selectDDayFilterTagUseCase,
            unselectDDayFilterTagUseCase = unselectDDayFilterTagUseCase,
            unselectAllDDayFilterTagUseCase = unselectAllDDayFilterTagUseCase,
        )
    }
}
