@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.detail.record

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class RoutineDetailRecordViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("대상 루틴의 기록을 조회해 그대로 노출한다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val recordList = List(RECORD_COUNT) { Arb.routineRecord().next().copy(routineId = routineId, isDeleted = false) }
                val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
                every { pageRoutineRecordUseCase(parameter = routineId) } returns flowOf(Result.success(loadedPagingData(recordList)))
                val viewModel = viewModel(routineId = routineId, pageRoutineRecordUseCase = pageRoutineRecordUseCase)

                viewModel.recordPagingData.asSnapshot() shouldBe recordList
            }
        }

        test("TC-6719e1a4-585d-463f-9a2f-617b95bdb1e1 다른 경로에서 넣거나 뺀 기록을 조작 없이 노출한다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val before = List(RECORD_COUNT) { Arb.routineRecord().next().copy(routineId = routineId, isDeleted = false) }
                val added = before + Arb.routineRecord().next().copy(routineId = routineId, isDeleted = false)
                val removed = added.drop(1)
                val pagingDataFlow = MutableStateFlow(Result.success(loadedPagingData(before)))
                val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
                every { pageRoutineRecordUseCase(parameter = routineId) } returns pagingDataFlow
                val viewModel = viewModel(routineId = routineId, pageRoutineRecordUseCase = pageRoutineRecordUseCase)

                viewModel.recordPagingData.asSnapshot() shouldBe before

                pagingDataFlow.value = Result.success(loadedPagingData(added))
                viewModel.recordPagingData.asSnapshot() shouldBe added

                pagingDataFlow.value = Result.success(loadedPagingData(removed))
                viewModel.recordPagingData.asSnapshot() shouldBe removed
            }
        }

        test("TC-615c8fe9-5ec2-4def-978c-2159397dbc93 기록 내역을 조회하지 못하면 기록이 없는 목록을 노출한다") {
            runTest(mainDispatcher) {
                val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
                every { pageRoutineRecordUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageRoutineRecordUseCase = pageRoutineRecordUseCase)

                viewModel.recordPagingData.asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-2b467619-0cc1-48f7-af73-e89304f9f6f6 기록 넣기는 고른 날짜와 입력한 양으로 한 번 요청되고 성공을 알린다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val date = Arb.kotlinxLocalDate().next()
                val amount = Arb.routineAmount().next()
                val parameter = AddRoutineRecordUseCase.Parameter(routineId = routineId, date = date, amount = amount)
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = parameter) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(routineId = routineId, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.effect.test {
                    viewModel.add(date = date, amount = amount)

                    awaitItem() shouldBe RoutineDetailRecordEffect.AddSucceeded
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = parameter) }
            }
        }

        test("TC-c913a811-72d3-4dd5-88b2-7d1bd63e17c6 같은 날짜와 같은 양으로 다시 넣어도 새 기록 넣기를 요청한다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val date = Arb.kotlinxLocalDate().next()
                val amount = Arb.routineAmount().next()
                val parameter = AddRoutineRecordUseCase.Parameter(routineId = routineId, date = date, amount = amount)
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = parameter) } answers { Result.success(Arb.kotlinUuid().next()) }
                val viewModel = viewModel(routineId = routineId, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.add(date = date, amount = amount)
                advanceUntilIdle()
                viewModel.add(date = date, amount = amount)
                advanceUntilIdle()

                coVerify(exactly = 2) { addRoutineRecordUseCase(parameter = parameter) }
            }
        }

        test("TC-91fbaabb-32ea-4348-a407-2d25eeea989c 기록 넣기를 처리하는 동안 진행 상태를 알리고 다시 요청하지 않는다") {
            runTest(mainDispatcher) {
                val result = CompletableDeferred<Result<Uuid>>()
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } coAnswers { result.await() }
                val viewModel = viewModel(addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.uiState.test {
                    awaitItem().isAddInProgress shouldBe false

                    viewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                    awaitItem().isAddInProgress shouldBe true

                    viewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                    runCurrent()
                    expectNoEvents()
                    coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }

                    result.complete(Result.success(Arb.kotlinUuid().next()))
                    awaitItem().isAddInProgress shouldBe false
                }
            }
        }

        test("TC-ec9f33e1-5294-4a84-a408-09bd17f5f7c1 기록 넣기를 저장하지 못하면 알리지 않고 진행 상태를 풀어 다시 넣을 수 있다") {
            runTest(mainDispatcher) {
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(addRoutineRecordUseCase = addRoutineRecordUseCase)
                val date = Arb.kotlinxLocalDate().next()
                val amount = Arb.routineAmount().next()

                viewModel.effect.test {
                    viewModel.add(date = date, amount = amount)
                    advanceUntilIdle()
                    expectNoEvents()

                    viewModel.uiState.value.isAddInProgress shouldBe false
                    viewModel.add(date = date, amount = amount)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 2) { addRoutineRecordUseCase(parameter = any()) }
            }
        }

        test("TC-0299a25e-1a9f-4315-b927-ec9c5e79a69c 기록 빼기는 그 기록으로 한 번 요청되고 따로 알리지 않는다") {
            runTest(mainDispatcher) {
                val recordId = Arb.kotlinUuid().next()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = recordId) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.effect.test {
                    viewModel.delete(recordId = recordId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordId) }
            }
        }

        test("TC-47d2739f-e2df-4839-9625-19f106877989 기록 빼기를 저장하지 못하면 알리지 않고 같은 기록을 다시 뺄 수 있다") {
            runTest(mainDispatcher) {
                val recordId = Arb.kotlinUuid().next()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = recordId) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.effect.test {
                    viewModel.delete(recordId = recordId)
                    advanceUntilIdle()
                    viewModel.delete(recordId = recordId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { deleteRoutineRecordUseCase(parameter = recordId) }
            }
        }

        test("한 기록의 빼기를 처리하는 동안 그 기록은 다시 처리하지 않고 다른 기록은 이어서 뺀다") {
            runTest(mainDispatcher) {
                val recordId = Arb.kotlinUuid().next()
                val otherRecordId = Arb.kotlinUuid().next()
                val result = CompletableDeferred<Result<Int>>()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = recordId) } coAnswers { result.await() }
                coEvery { deleteRoutineRecordUseCase(parameter = otherRecordId) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.delete(recordId = recordId)
                runCurrent()
                viewModel.delete(recordId = recordId)
                viewModel.delete(recordId = otherRecordId)
                runCurrent()

                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordId) }
                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = otherRecordId) }

                result.complete(Result.success(1))
                advanceUntilIdle()
                viewModel.delete(recordId = recordId)
                advanceUntilIdle()

                coVerify(exactly = 2) { deleteRoutineRecordUseCase(parameter = recordId) }
            }
        }

        test("기록 빼기를 처리하는 동안에도 기록 넣기를 요청한다") {
            runTest(mainDispatcher) {
                val result = CompletableDeferred<Result<Int>>()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = any()) } coAnswers { result.await() }
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addRoutineRecordUseCase = addRoutineRecordUseCase, deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.delete(recordId = Arb.kotlinUuid().next())
                runCurrent()
                viewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                advanceUntilIdle()

                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }
                result.complete(Result.success(1))
            }
        }
    }

    private fun viewModel(
        routineId: Uuid = Arb.kotlinUuid().next(),
        addRoutineRecordUseCase: AddRoutineRecordUseCase = mockk(),
        deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase = mockk(),
        pageRoutineRecordUseCase: PageRoutineRecordUseCase = emptyPageRoutineRecordUseCase(),
    ): RoutineDetailRecordViewModel =
        RoutineDetailRecordViewModel(
            routineId = routineId,
            addRoutineRecordUseCase = addRoutineRecordUseCase,
            deleteRoutineRecordUseCase = deleteRoutineRecordUseCase,
            pageRoutineRecordUseCase = pageRoutineRecordUseCase,
        )

    private fun emptyPageRoutineRecordUseCase(): PageRoutineRecordUseCase {
        val pagingDataFlow: Flow<Result<PagingData<RoutineRecord>>> = flowOf(Result.success(loadedPagingData(emptyList())))

        return mockk<PageRoutineRecordUseCase>().apply {
            every { this@apply(parameter = any()) } returns pagingDataFlow
        }
    }

    // 불러오기가 끝난 목록이어야 조회 결과를 기다리는 검증이 끝난다.
    private fun loadedPagingData(recordList: List<RoutineRecord>): PagingData<RoutineRecord> =
        PagingData.from(
            data = recordList,
            sourceLoadStates =
                LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
        )

    private companion object {
        const val RECORD_COUNT = 3
    }
}
