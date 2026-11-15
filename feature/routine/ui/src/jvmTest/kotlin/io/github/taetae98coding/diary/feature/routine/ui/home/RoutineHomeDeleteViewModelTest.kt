@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class RoutineHomeDeleteViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-b2aa114d-4bae-48c8-96e0-c52dca307491 목록에서 삭제하면 그 루틴의 삭제를 한 번 요청하고 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = routineId)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Deleted(id = routineId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-00e20165-67a0-4248-9139-014e8422cc4c 삭제가 저장되지 못하면 삭제 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = routineId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-86e61dbb-b1cb-4bd0-b9af-03d709d0b17c 실행 취소하면 그 루틴을 되돌리는 요청을 한 번 하고 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()
                coEvery { restoreRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(restoreRoutineUseCase = restoreRoutineUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = routineId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreRoutineUseCase(parameter = routineId) }
            }
        }

        test("실행 취소가 저장되지 않아도 별도 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()
                coEvery { restoreRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(restoreRoutineUseCase = restoreRoutineUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = routineId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-02029f5d-0bca-4746-92aa-8fb254bf6768 목록을 조회하지 못하면 노출하는 루틴이 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageFlow = flowOf(Result.failure(IllegalStateException())))
                viewModel.updateToday(today = Arb.kotlinxLocalDate().next())

                flowOf(viewModel.routinePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 저장된 선택이 모두 선택할 수 없게 되면 필터가 적용되지 않은 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(emptyList())),
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe RoutineHomeScaffoldFilterUiState(isLoaded = false)
                    advanceUntilIdle()

                    val uiState = awaitItem()
                    uiState.isApplied shouldBe false
                    uiState.selectedTagIdSet.shouldBeEmpty()
                    uiState.listQueryFilter shouldBe RoutineHomeListQueryFilter(storedTagIdSet = setOf(tag.id), viewScope = RoutineViewScope.TODAY)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 선택할 수 있는 태그를 선택해 두면 필터가 적용된 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(listOf(tag))),
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe RoutineHomeScaffoldFilterUiState(isLoaded = false)
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
        pageFlow: Flow<Result<PagingData<RoutineSummary>>> = flowOf(Result.success(PagingData.empty())),
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        filterTagIdFlow: Flow<Result<Set<Uuid>>> = flowOf(Result.success(emptySet())),
        deleteRoutineUseCase: DeleteRoutineUseCase = mockk(),
        restoreRoutineUseCase: RestoreRoutineUseCase = mockk(),
    ): RoutineHomeViewModel {
        val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
        every { pageRoutineUseCase(parameter = any()) } returns pageFlow
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns filterFlow
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns filterTagIdFlow

        return RoutineHomeViewModel(
            getRoutineFilterUseCase = getRoutineFilterUseCase,
            getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
            getRoutineViewScopeUseCase = routineViewScopeUseCase(),
            getUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(),
            pageViewScopeRoutineUseCase = pageRoutineUseCase,
            finishRoutineUseCase = mockk(),
            restartRoutineUseCase = mockk(),
            deleteRoutineUseCase = deleteRoutineUseCase,
            restoreRoutineUseCase = restoreRoutineUseCase,
            addQuickRoutineRecordUseCase = mockk(),
            deleteRoutineRecordUseCase = mockk(),
        )
    }
}
