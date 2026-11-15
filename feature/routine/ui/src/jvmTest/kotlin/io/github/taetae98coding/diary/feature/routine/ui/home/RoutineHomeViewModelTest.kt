@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineUnit
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.AddQuickRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetUnfinishedRoutineExistsUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecordEffect
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.uuid.Uuid

class RoutineHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("오늘이 정해지면 그 오늘로 조회한 루틴 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val summaryList = List(3) { Arb.routineSummary().next() }
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = today) } returns flowOf(Result.success(PagingData.from(summaryList)))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.updateToday(today = today)

                flowOf(viewModel.routinePagingData.first()).asSnapshot() shouldBe summaryList
            }
        }

        test("TC-115d314d-eb03-4b9a-85ed-643f736e6751 오늘을 다시 정하면 바뀐 오늘로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val nextDay = today.plus(1, DateTimeUnit.DAY)
                val nextDaySummaryList = List(2) { Arb.routineSummary().next() }
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = today) } returns flowOf(Result.success(PagingData.from(emptyList())))
                every { pageRoutineUseCase(parameter = nextDay) } returns flowOf(Result.success(PagingData.from(nextDaySummaryList)))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = today)
                    advanceUntilIdle()
                    viewModel.updateToday(today = nextDay)
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe nextDaySummaryList
                }

                verify(exactly = 1) { pageRoutineUseCase(parameter = today) }
                verify(exactly = 1) { pageRoutineUseCase(parameter = nextDay) }
            }
        }

        test("TC-21eae803-7dfa-42d6-adde-57de3d4294c7 오늘을 같은 날짜로 다시 알려도 목록을 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = today) } returns flowOf(Result.success(PagingData.from(emptyList())))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = today)
                    advanceUntilIdle()
                    viewModel.updateToday(today = today)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageRoutineUseCase(parameter = today) }
            }
        }

        test("TC-240ed16d-72da-4867-b62a-6a28da5b6a08 빠른 기록은 양을 정하지 않고 화면의 오늘로 한 번 저장을 요청하고 기록 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val record = record()
                val today = Arb.kotlinxLocalDate().next()
                val recordId = Arb.kotlinUuid().next()
                val addQuickRoutineRecordUseCase = mockk<AddQuickRoutineRecordUseCase>()
                coEvery { addQuickRoutineRecordUseCase(parameter = any()) } returns Result.success(recordId)
                val viewModel = viewModel(addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.quickRecord(record = record, today = today)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)
                    expectNoEvents()
                }

                coVerify(exactly = 1) {
                    addQuickRoutineRecordUseCase(parameter = AddQuickRoutineRecordUseCase.Parameter(routineId = record.routineId, today = today, amount = null))
                }
            }
        }

        test("TC-29093cde-144c-4980-9874-c74cbf93fbe8 양을 입력한 기록은 입력한 양과 화면의 오늘로 한 번 저장을 요청하고 기록 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val record = record()
                val today = Arb.kotlinxLocalDate().next()
                val recordId = Arb.kotlinUuid().next()
                val addQuickRoutineRecordUseCase = mockk<AddQuickRoutineRecordUseCase>()
                coEvery { addQuickRoutineRecordUseCase(parameter = any()) } returns Result.success(recordId)
                val viewModel = viewModel(addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.amountRecord(record = record, today = today)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)
                    expectNoEvents()
                }

                coVerify(exactly = 1) {
                    addQuickRoutineRecordUseCase(parameter = AddQuickRoutineRecordUseCase.Parameter(routineId = record.routineId, today = today, amount = record.amount))
                }
            }
        }

        test("TC-9f044e4d-efb7-48c6-9ca9-61c80f0eb561 기록을 저장하지 못하면 Effect를 보내지 않고 다시 기록할 수 있다") {
            runTest(mainDispatcher) {
                val record = record()
                val today = Arb.kotlinxLocalDate().next()
                val recordId = Arb.kotlinUuid().next()
                val addQuickRoutineRecordUseCase = mockk<AddQuickRoutineRecordUseCase>()
                coEvery { addQuickRoutineRecordUseCase(parameter = any()) } returnsMany listOf(Result.failure(IllegalStateException()), Result.failure(IllegalStateException()), Result.success(recordId))
                val viewModel = viewModel(addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.quickRecord(record = record, today = today)
                    advanceUntilIdle()
                    viewModel.amountRecord(record = record, today = today)
                    advanceUntilIdle()
                    expectNoEvents()

                    viewModel.quickRecord(record = record, today = today)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)
                    expectNoEvents()
                }

                coVerify(exactly = 3) { addQuickRoutineRecordUseCase(parameter = any()) }
            }
        }

        test("TC-74ea6b30-22c7-46f6-bc81-0da504109eac 한 루틴의 기록을 처리하는 동안 같은 루틴의 빠른 기록과 양을 입력한 기록은 다시 처리하지 않는다") {
            runTest(mainDispatcher) {
                val record = record()
                val today = Arb.kotlinxLocalDate().next()
                val recordId = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val addQuickRoutineRecordUseCase = mockk<AddQuickRoutineRecordUseCase>()
                coEvery { addQuickRoutineRecordUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(recordId)
                }
                val viewModel = viewModel(addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.quickRecord(record = record, today = today)
                    advanceUntilIdle()
                    viewModel.quickRecord(record = record, today = today)
                    viewModel.amountRecord(record = record, today = today)
                    advanceUntilIdle()
                    expectNoEvents()

                    gate.complete(Unit)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { addQuickRoutineRecordUseCase(parameter = any()) }
            }
        }

        test("TC-aab9e2bd-2fab-4a45-a960-624c1951c2ff 처리가 끝나면 같은 루틴을 이어서 기록하고 다른 루틴은 처리 중에도 기록한다") {
            runTest(mainDispatcher) {
                val recordA = record()
                val recordB = record()
                val today = Arb.kotlinxLocalDate().next()
                val gate = CompletableDeferred<Unit>()
                val addQuickRoutineRecordUseCase = mockk<AddQuickRoutineRecordUseCase>()
                coEvery { addQuickRoutineRecordUseCase(parameter = match { parameter -> parameter.routineId == recordA.routineId }) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                coEvery { addQuickRoutineRecordUseCase(parameter = match { parameter -> parameter.routineId == recordB.routineId }) } answers { Result.success(Arb.kotlinUuid().next()) }
                val viewModel = viewModel(addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.quickRecord(record = recordA, today = today)
                    viewModel.quickRecord(record = recordB, today = today)
                    advanceUntilIdle()

                    (awaitItem() as RoutineHomeRecordEffect.Recorded).record shouldBe recordB
                    expectNoEvents()

                    gate.complete(Unit)
                    advanceUntilIdle()
                    (awaitItem() as RoutineHomeRecordEffect.Recorded).record shouldBe recordA

                    viewModel.quickRecord(record = recordA, today = today)
                    advanceUntilIdle()
                    (awaitItem() as RoutineHomeRecordEffect.Recorded).record shouldBe recordA
                    expectNoEvents()
                }

                coVerify(exactly = 2) { addQuickRoutineRecordUseCase(parameter = match { parameter -> parameter.routineId == recordA.routineId }) }
                coVerify(exactly = 1) { addQuickRoutineRecordUseCase(parameter = match { parameter -> parameter.routineId == recordB.routineId }) }
            }
        }

        test("TC-0011e20c-a37d-44bf-8c8e-fdb644709145 실행 취소하면 그 기록의 삭제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val recordId = Arb.kotlinUuid().next()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.undoRecord(recordId = recordId)
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordId) }
            }
        }

        test("TC-f8b3d050-ec39-48e9-92ad-a5137d6db0b9 실행 취소를 저장하지 못해도 별도 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val recordId = Arb.kotlinUuid().next()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                viewModel.recordEffect.test {
                    viewModel.effect.test {
                        viewModel.undoRecord(recordId = recordId)
                        advanceUntilIdle()

                        expectNoEvents()
                    }
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordId) }
            }
        }

        test("TC-ada941d6-d2f9-4eaa-b5a4-d313a4615474 오늘을 다시 정하면 필터를 다시 고르지 않아도 바뀐 오늘로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                // 2026-07-10은 금요일, 2026-07-11은 토요일이다. 오늘로 좁힌 결과는 조회가 정하고 RoutineHomeViewScopeDaoTest가 확인한다.
                val friday = LocalDate(2026, 7, 10)
                val saturday = LocalDate(2026, 7, 11)
                val fridayList = listOf(Arb.routineSummary().next())
                val saturdayList = List(2) { Arb.routineSummary().next() }
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = friday) } returns flowOf(Result.success(PagingData.from(fridayList)))
                every { pageRoutineUseCase(parameter = saturday) } returns flowOf(Result.success(PagingData.from(saturdayList)))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = friday)
                    advanceUntilIdle()
                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe fridayList

                    viewModel.updateToday(today = saturday)
                    advanceUntilIdle()
                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe saturdayList
                }
            }
        }

        test("TC-56b5d000-db96-4dd1-a053-d3c19305d889 오늘을 다시 정하기 전에는 요일이 바뀌어도 목록을 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                // 목록은 화면이 알려 준 오늘로만 조회하므로, 기기의 날짜가 2026-07-11이 되어도 알려 주기 전에는 2026-07-10의 조회가 그대로다.
                val friday = LocalDate(2026, 7, 10)
                val fridayList = listOf(Arb.routineSummary().next())
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = friday) } returns flowOf(Result.success(PagingData.from(fridayList)))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = friday)
                    advanceUntilIdle()
                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe fridayList

                    advanceTimeBy(DAY_MILLIS)
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { pageRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 보기 범위와 완료되지 않은 루틴의 유무를 빈 상태를 가르는 상태로 노출한다") {
            runTest(mainDispatcher) {
                val viewScopeFlow = MutableStateFlow(RoutineViewScope.TODAY)
                val existsFlow = MutableStateFlow(false)
                val viewModel = viewModel(viewScopeFlow = viewScopeFlow, unfinishedExistsFlow = existsFlow)

                viewModel.filterUiState.test {
                    awaitItem().isLoaded shouldBe false
                    advanceUntilIdle()
                    awaitItem() shouldBe RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false)

                    existsFlow.value = true
                    advanceUntilIdle()
                    awaitItem().emptyReason shouldBe RoutineHomeEmptyReason.NO_TODAY_ROUTINE

                    viewScopeFlow.value = RoutineViewScope.ALL
                    advanceUntilIdle()
                    awaitItem() shouldBe RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true)

                    existsFlow.value = false
                    advanceUntilIdle()
                    awaitItem().emptyReason shouldBe RoutineHomeEmptyReason.NO_ROUTINE
                }
            }
        }

        test("보기 범위나 완료되지 않은 루틴의 유무를 조회하지 못하면 오늘 할 루틴, 루틴 없음으로 노출한다") {
            runTest(mainDispatcher) {
                val getRoutineViewScopeUseCase = mockk<GetRoutineViewScopeUseCase>()
                every { getRoutineViewScopeUseCase() } returns flowOf(Result.failure(IllegalStateException()))
                val getUnfinishedRoutineExistsUseCase = mockk<GetUnfinishedRoutineExistsUseCase>()
                every { getUnfinishedRoutineExistsUseCase() } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(getRoutineViewScopeUseCase = getRoutineViewScopeUseCase, getUnfinishedRoutineExistsUseCase = getUnfinishedRoutineExistsUseCase)

                viewModel.filterUiState.test {
                    awaitItem().isLoaded shouldBe false
                    advanceUntilIdle()
                    awaitItem() shouldBe RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false)
                }
            }
        }

        test("목록을 조회하지 못하면 노출하는 루틴이 없다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
                every { pageRoutineUseCase(parameter = today) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageRoutineUseCase = pageRoutineUseCase)

                viewModel.updateToday(today = today)

                flowOf(viewModel.routinePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }
    }

    private fun record(): RoutineHomeRecord =
        RoutineHomeRecord(
            routineId = Arb.kotlinUuid().next(),
            title = Arb.nonBlankString().next(),
            unit = Arb.routineUnit().next(),
            amount = Arb.routineAmount().next(),
        )

    private fun viewModel(
        pageRoutineUseCase: PageViewScopeRoutineUseCase = mockk(),
        addQuickRoutineRecordUseCase: AddQuickRoutineRecordUseCase = mockk(),
        deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase = mockk(),
        viewScopeFlow: Flow<RoutineViewScope> = flowOf(RoutineViewScope.TODAY),
        unfinishedExistsFlow: Flow<Boolean> = flowOf(false),
        getRoutineViewScopeUseCase: GetRoutineViewScopeUseCase = routineViewScopeUseCase(viewScope = viewScopeFlow),
        getUnfinishedRoutineExistsUseCase: GetUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(exists = unfinishedExistsFlow),
    ): RoutineHomeViewModel {
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns flowOf(Result.success(emptyList()))
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns flowOf<Result<Set<Uuid>>>(Result.success(emptySet()))

        return RoutineHomeViewModel(
            getRoutineFilterUseCase = getRoutineFilterUseCase,
            getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
            getRoutineViewScopeUseCase = getRoutineViewScopeUseCase,
            getUnfinishedRoutineExistsUseCase = getUnfinishedRoutineExistsUseCase,
            pageViewScopeRoutineUseCase = pageRoutineUseCase,
            finishRoutineUseCase = mockk(),
            restartRoutineUseCase = mockk(),
            deleteRoutineUseCase = mockk<DeleteRoutineUseCase>(),
            restoreRoutineUseCase = mockk<RestoreRoutineUseCase>(),
            addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase,
            deleteRoutineRecordUseCase = deleteRoutineRecordUseCase,
        )
    }
}

private const val DAY_MILLIS = 86_400_000L
