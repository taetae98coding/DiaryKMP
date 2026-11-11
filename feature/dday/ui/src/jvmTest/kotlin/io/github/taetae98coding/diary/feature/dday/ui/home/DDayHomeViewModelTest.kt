@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import kotlin.uuid.Uuid

class DDayHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("조회한 디데이 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val ddayList = List(3) { Arb.dday(isDeleted = false).next() }
                val viewModel = viewModel(pageFlow = flowOf(Result.success(PagingData.from(ddayList))))

                flowOf(viewModel.ddayPagingData.first()).asSnapshot() shouldBe ddayList
            }
        }

        test("TC-22f92e42-b828-4b15-ac9f-7447050f4090 현재 계정을 확인할 수 없으면 노출하는 디데이가 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageFlow = flowOf(Result.failure(IllegalStateException())))

                flowOf(viewModel.ddayPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-2a38eccb-d6fd-4c10-b3ba-ef1fb0e3e885 목록에서 삭제하면 그 디데이의 삭제를 한 번 요청하고 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val ddayId = Arb.kotlinUuid().next()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(deleteDDayUseCase = deleteDDayUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = ddayId)
                    advanceUntilIdle()

                    awaitItem() shouldBe DDayListEffect.Deleted(id = ddayId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteDDayUseCase(parameter = ddayId) }
            }
        }

        test("TC-c4afd5d9-8a2a-4ffa-8934-2b0786c16f03 실행 취소가 저장되지 않아도 별도 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val ddayId = Arb.kotlinUuid().next()
                val restoreDDayUseCase = mockk<RestoreDDayUseCase>()
                coEvery { restoreDDayUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(restoreDDayUseCase = restoreDDayUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = ddayId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreDDayUseCase(parameter = ddayId) }
            }
        }

        test("TC-dba071a6-eb8f-4196-979e-76536b2fdc50 저장된 선택이 모두 선택할 수 없게 되면 필터가 적용되지 않은 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(emptyList())),
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe DDayHomeScaffoldFilterUiState(isLoaded = false)
                    advanceUntilIdle()

                    val uiState = awaitItem()
                    uiState.isApplied shouldBe false
                    uiState.selectedTagIdSet.shouldBeEmpty()
                    uiState.listQueryFilter shouldBe setOf(tag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-911975f0-716c-451b-95bf-aa91e58af35b 태그가 다시 선택할 수 있게 되면 필터가 적용된 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val filterFlow = MutableStateFlow<Result<List<Tag>>>(Result.success(emptyList()))
                val viewModel =
                    viewModel(
                        filterFlow = filterFlow,
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe DDayHomeScaffoldFilterUiState(isLoaded = false)
                    advanceUntilIdle()
                    awaitItem().isApplied shouldBe false

                    filterFlow.value = Result.success(listOf(tag))
                    advanceUntilIdle()

                    val uiState = awaitItem()
                    uiState.isApplied shouldBe true
                    uiState.selectedTagIdSet shouldBe setOf(tag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        pageFlow: Flow<Result<PagingData<DDay>>> = flowOf(Result.success(PagingData.empty())),
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        filterTagIdFlow: Flow<Result<Set<Uuid>>> = flowOf(Result.success(emptySet())),
        deleteDDayUseCase: DeleteDDayUseCase = mockk(),
        restoreDDayUseCase: RestoreDDayUseCase = mockk(),
    ): DDayHomeViewModel {
        val pageDDayUseCase = mockk<PageDDayUseCase>()
        every { pageDDayUseCase() } returns pageFlow
        val getDDayFilterUseCase = mockk<GetDDayFilterUseCase>()
        every { getDDayFilterUseCase() } returns filterFlow
        val getDDayFilterTagIdUseCase = mockk<GetDDayFilterTagIdUseCase>()
        every { getDDayFilterTagIdUseCase() } returns filterTagIdFlow

        return DDayHomeViewModel(
            getDDayFilterUseCase = getDDayFilterUseCase,
            getDDayFilterTagIdUseCase = getDDayFilterTagIdUseCase,
            pageDDayUseCase = pageDDayUseCase,
            deleteDDayUseCase = deleteDDayUseCase,
            restoreDDayUseCase = restoreDDayUseCase,
        )
    }
}
