package io.github.taetae98coding.diary.work.appaction.routine

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineSummaryUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.appaction.dday.clockAt
import io.github.taetae98coding.diary.work.appaction.search.searchAppActionOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RoutineAppActionTest :
    FunSpec({
        test("TC-68c32f9f-68c5-4b5c-8ac5-74e6e6bff138 상세·목록·검색에 오늘로 계산한 진행 상태, 합계와 달성률을 담는다") {
            runTest {
                listOf(
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 1500, expected = AppActionRoutineProgress(status = "in_progress", cycleAmount = 1500, achievementRate = 75)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 2500, expected = AppActionRoutineProgress(status = "in_progress", cycleAmount = 2500, achievementRate = 100)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = null, cycleAmount = 1500, expected = AppActionRoutineProgress(status = "in_progress", cycleAmount = 1500, achievementRate = null)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 0, expected = AppActionRoutineProgress(status = "in_progress", cycleAmount = 0, achievementRate = 0)),
                    ProgressCase(startDate = LocalDate(2026, 3, 21), goalAmount = 2000, cycleAmount = 0, expected = AppActionRoutineProgress(status = "not_started", cycleAmount = null, achievementRate = null)),
                ).forEach { case ->
                    val routine =
                        Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = case.startDate, cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = case.goalAmount)).next()
                    val summary = RoutineSummary(routine = routine, cycleAmount = case.cycleAmount)
                    val expected = routine.expected(progress = case.expected)
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    val pageRoutineUseCase = mockk<PageRoutineUseCase>()
                    val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                    every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = TODAY)) } returns flowOf(Result.success(summary))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    every { pageRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = routine.detail.title, today = TODAY)) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    val action = routineAppActionOf(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))
                    val listAction = routineListAppActionOf(pageRoutineUseCase, clockAt(date = TODAY))
                    val searchAction = searchAppActionOf(searchRoutineUseCase, clockAt(date = TODAY))

                    action.get(routineId = routine.id.toString()) shouldBe AppActionRoutineDetail(routine = expected, tags = emptyList())
                    listAction.list(cursor = null) shouldBe AppActionPage(items = listOf(expected), nextCursor = null)
                    searchAction.routine(query = routine.detail.title, cursor = null) shouldBe AppActionPage(items = listOf(expected), nextCursor = null)
                }
            }
        }

        test("TC-55b4db15-225b-4ee1-862f-dcdfa753279a 완료된 루틴의 결과에는 완료 여부와 진행 상태만 담는다") {
            runTest {
                listOf(
                    Triple(LocalDate(2026, 3, 1), 1500L, "in_progress"),
                    Triple(LocalDate(2026, 3, 1), 0L, "in_progress"),
                    Triple(LocalDate(2026, 3, 21), 0L, "not_started"),
                ).forEach { (startDate, cycleAmount, status) ->
                    val routine =
                        Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = startDate, cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = 2000), isFinished = true).next()
                    val summary = RoutineSummary(routine = routine, cycleAmount = cycleAmount)
                    val expected = routine.expected(progress = AppActionRoutineProgress(status = status, cycleAmount = null, achievementRate = null))
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    val pageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>()
                    val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                    every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = TODAY)) } returns flowOf(Result.success(summary))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    every { pageFinishedRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = routine.detail.title, today = TODAY)) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    val action = routineAppActionOf(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))
                    val listAction = routineListAppActionOf(pageFinishedRoutineUseCase, clockAt(date = TODAY))
                    val searchAction = searchAppActionOf(searchRoutineUseCase, clockAt(date = TODAY))

                    expected.isFinished shouldBe true
                    action.get(routineId = routine.id.toString()) shouldBe AppActionRoutineDetail(routine = expected, tags = emptyList())
                    listAction.finishedList(cursor = null) shouldBe AppActionPage(items = listOf(expected), nextCursor = null)
                    searchAction.routine(query = routine.detail.title, cursor = null) shouldBe AppActionPage(items = listOf(expected), nextCursor = null)
                }
            }
        }

        test("TC-9c80fb54-58d6-42ab-8d3f-15f6c838b996 루틴 하나를 지정해 완료하고 다시 시작한다") {
            runTest {
                val unfinished = Arb.routine(isDeleted = false, isFinished = false).next()
                val finished = Arb.routine(isDeleted = false, isFinished = true).next()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = unfinished.id) } returns Result.success(1)
                coEvery { restartRoutineUseCase(parameter = finished.id) } returns Result.success(1)

                routineAppActionOf(foundRoutineUseCase(unfinished), finishRoutineUseCase, restartRoutineUseCase, updateRoutineUseCase)
                    .finish(routineId = unfinished.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)
                routineAppActionOf(foundRoutineUseCase(finished), finishRoutineUseCase, restartRoutineUseCase, updateRoutineUseCase)
                    .restart(routineId = finished.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)

                coVerify(exactly = 1) { finishRoutineUseCase(parameter = unfinished.id) }
                coVerify(exactly = 1) { restartRoutineUseCase(parameter = finished.id) }
                coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-e408a0ff-1873-49ab-8f51-d7ca57065fee 완료되지 않은 루틴 목록과 완료된 루틴 목록을 따로 받는다") {
            runTest {
                val unfinished = RoutineSummary(routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = "가"), isFinished = false).next(), cycleAmount = 0)
                val finished = RoutineSummary(routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = "나"), isFinished = true).next(), cycleAmount = 0)
                val pageRoutineUseCase = mockk<PageRoutineUseCase>()
                val pageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>()
                every { pageRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(unfinished))))
                every { pageFinishedRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(finished))))
                val action = routineListAppActionOf(pageRoutineUseCase, pageFinishedRoutineUseCase, clockAt(date = TODAY))

                action.list(cursor = null).items.map { routine -> routine.title to routine.isFinished } shouldBe listOf("가" to false)
                action.finishedList(cursor = null).items.map { routine -> routine.title to routine.isFinished } shouldBe listOf("나" to true)
            }
        }

        test("TC-7f3e54c7-f49d-41d9-9591-35371e99e146 루틴 검색 결과에는 완료된 루틴도 함께 담긴다") {
            runTest {
                val summaryList =
                    listOf("운동 가" to false, "운동 나" to true).map { (title, isFinished) ->
                        RoutineSummary(routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = title, startDate = LocalDate(2026, 3, 1)), isFinished = isFinished).next(), cycleAmount = 0)
                    }
                val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = "운동", today = TODAY)) } returns flowOf(Result.success(PagingData.from(summaryList)))

                val page = searchAppActionOf(searchRoutineUseCase, clockAt(date = TODAY)).routine(query = "운동", cursor = null)

                page.items.map { routine -> routine.title to routine.isFinished } shouldBe listOf("운동 가" to false, "운동 나" to true)
            }
        }

        test("TC-a176d57f-1a89-444a-98bc-a6837f8e5a3e 완료된 루틴에도 기록을 넣고 뺀다") {
            runTest {
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1), amountPerRecord = 250), isFinished = true).next()
                val record = Arb.routineRecord().next().copy(routineId = routine.id, isDeleted = false)
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                val findRoutineRecordUseCase = mockk<FindRoutineRecordUseCase>()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                every { findRoutineRecordUseCase(parameter = record.id) } returns flowOf(Result.success(record))
                coEvery { deleteRoutineRecordUseCase(parameter = record.id) } returns Result.success(1)
                val action = routineRecordAppActionOf(foundRoutineUseCase(routine), addRoutineRecordUseCase, findRoutineRecordUseCase, deleteRoutineRecordUseCase, clockAt(date = TODAY))

                action.add(routineId = routine.id.toString(), date = null, amount = null) shouldBe AppActionChangedCount(changedCount = 1)
                action.add(routineId = routine.id.toString(), date = "2026-03-10", amount = 500) shouldBe AppActionChangedCount(changedCount = 1)
                action.remove(recordId = record.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)

                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routine.id, date = TODAY, amount = 250)) }
                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routine.id, date = LocalDate(2026, 3, 10), amount = 500)) }
                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = record.id) }
            }
        }

        test("TC-1b1d24f6-3f9f-43b7-81fa-8b07eff9083d 목록과 검색은 오늘로 정한 목록 순서 그대로 돌려준다") {
            runTest {
                val summaryList =
                    listOf(
                        "운동 다" to LocalDate(2026, 3, 20),
                        "운동 라" to LocalDate(2026, 1, 1),
                        "운동 가" to LocalDate(2026, 4, 1),
                        "운동 나" to LocalDate(2026, 3, 21),
                    ).map { (title, startDate) ->
                        RoutineSummary(routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = title, startDate = startDate)).next(), cycleAmount = 0)
                    }
                val pageRoutineUseCase = mockk<PageRoutineUseCase>()
                val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                every { pageRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(summaryList)))
                every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = "운동", today = TODAY)) } returns flowOf(Result.success(PagingData.from(summaryList)))

                listOf(
                    routineListAppActionOf(pageRoutineUseCase, clockAt(date = TODAY)).list(cursor = null),
                    searchAppActionOf(searchRoutineUseCase, clockAt(date = TODAY)).routine(query = "운동", cursor = null),
                ).forEach { page ->
                    page.items.map { routine -> routine.title to routine.progress.status } shouldBe
                        listOf("운동 다" to "in_progress", "운동 라" to "in_progress", "운동 가" to "not_started", "운동 나" to "not_started")
                }
            }
        }

        test("TC-800ef520-7297-4fce-b0cd-f9c470739250 주기를 정해진 다섯 값으로 주고받는다") {
            runTest {
                listOf(
                    "FREQ=DAILY" to RoutineCycle.DAILY,
                    "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR" to RoutineCycle.WEEKDAYS,
                    "FREQ=DAILY;BYDAY=SA,SU" to RoutineCycle.WEEKENDS,
                    "FREQ=WEEKLY;WKST=SU" to RoutineCycle.WEEKLY,
                    "FREQ=MONTHLY" to RoutineCycle.MONTHLY,
                ).forEach { (cycleValue, recorded) ->
                    val addedId = Arb.kotlinUuid().next()
                    val parameter = slot<AddRoutineUseCase.Parameter>()
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                    val added = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(cycle = recorded)).next().copy(id = addedId)
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    every { findRoutineSummaryUseCase(parameter = any()) } returns flowOf(Result.success(RoutineSummary(routine = added, cycleAmount = 0)))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    val action = routineAppActionOf(addRoutineUseCase, findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))

                    action.add(title = Arb.nonBlankString().next(), cycle = cycleValue) shouldBe AppActionId(id = addedId.toString())
                    parameter.captured.input.cycle shouldBe recorded

                    action.get(routineId = addedId.toString())?.routine?.cycle shouldBe cycleValue
                }
            }
        }

        test("TC-303d5da6-b7a9-4de1-9642-a37ed2f38262 단위를 글자로 주고받고 담지 않으면 빈 단위로 추가한다") {
            runTest {
                // 요청에 담은 단위에서 앞뒤 공백을 빼 기록하는 것은 추가 요청이 맡고 RoutineAddRoutineUseCaseTest가 확인한다.
                listOf(
                    "회" to "회",
                    "ml" to "ml",
                    "ML" to "ML",
                    "L" to "L",
                    "분 걷기" to "분 걷기",
                    " ml " to "ml",
                    "kilometers" to "kilometers",
                    " kilometers " to "kilometers",
                    "" to "",
                    "  " to "",
                    null to "",
                ).forEach { (unitValue, recorded) ->
                    val addedId = Arb.kotlinUuid().next()
                    val parameter = slot<AddRoutineUseCase.Parameter>()
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                    val added = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(unit = recorded)).next().copy(id = addedId)
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    every { findRoutineSummaryUseCase(parameter = any()) } returns flowOf(Result.success(RoutineSummary(routine = added, cycleAmount = 0)))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    val action = routineAppActionOf(addRoutineUseCase, findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))

                    action.add(title = Arb.nonBlankString().next(), unit = unitValue) shouldBe AppActionId(id = addedId.toString())
                    parameter.captured.input.unit shouldBe unitValue.orEmpty()
                    parameter.captured.input.unit
                        .trim() shouldBe recorded

                    action.get(routineId = addedId.toString())?.routine?.unit shouldBe recorded
                }
            }
        }

        test("TC-9906df56-12e2-4162-b857-8ddcb290493d 단위가 10자를 넘거나 줄바꿈을 담고 있으면 요청이 실패하고 잘라서 받아들이지 않는다") {
            runTest {
                listOf("kilometerss", "가나다라마바사아자차카", "kilometers per hour", "밀리\n리터").forEach { unit ->
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    val action = routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> {
                        action.add(title = Arb.nonBlankString().next(), unit = unit)
                    }.message shouldBe UNIT_FAILURE_MESSAGE
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(unit = "ml")).next()
                listOf("kilometerss", "밀리\n리터").forEach { unit ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val action = routineAppActionOf(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> {
                        action.update(routineId = stored.id.toString(), unit = unit)
                    }.message shouldBe UNIT_FAILURE_MESSAGE
                    coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
                }
            }
        }

        test("TC-3f6211f4-30b1-4ce1-88de-c9be0a5fb7a5 받을 수 없는 주기를 담으면 요청이 실패한다") {
            runTest {
                listOf(
                    "FREQ=WEEKLY",
                    "freq=daily",
                    "FREQ=WEEKLY;INTERVAL=2",
                    "매일",
                    "평일만",
                    "FREQ=DAILY;BYDAY=MO,WE,FR",
                    "FREQ=DAILY;BYDAY=SU,SA",
                    "FREQ=WEEKLY;WKST=SU;BYDAY=SA,SU",
                ).forEach { cycle ->
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    val action = routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> {
                        action.add(title = Arb.nonBlankString().next(), cycle = cycle)
                    }.message shouldBe CYCLE_FAILURE_MESSAGE
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(cycle = RoutineCycle.DAILY)).next()
                listOf("FREQ=YEARLY", "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR,SA").forEach { cycle ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val action = routineAppActionOf(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> {
                        action.update(routineId = stored.id.toString(), cycle = cycle)
                    }.message shouldBe CYCLE_FAILURE_MESSAGE
                    coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
                }
            }
        }

        test("TC-f3d2b737-4227-4136-a021-1cc493cd40ac 수정은 담은 정보만 바꾸고 목표량은 지우는 요청으로, 단위는 빈 글자를 담아 비운다") {
            runTest {
                val stored =
                    Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = "물 마시기", startDate = LocalDate(2026, 1, 1), cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = 2000, amountPerRecord = 250)).next()
                val storedDetail = stored.detail
                val id = stored.id.toString()
                listOf<Pair<suspend (RoutineAppAction) -> AppActionChangedCount, RoutineDetail>>(
                    Pair({ action -> action.update(routineId = id, title = "물") }, storedDetail.copy(title = "물")),
                    Pair({ action -> action.update(routineId = id, clearGoalAmount = true) }, storedDetail.copy(goalAmount = null)),
                    Pair({ action -> action.update(routineId = id, goalAmount = 3000) }, storedDetail.copy(goalAmount = 3000)),
                    Pair({ action -> action.update(routineId = id, cycle = "FREQ=WEEKLY;WKST=SU") }, storedDetail.copy(cycle = RoutineCycle.WEEKLY)),
                    Pair({ action -> action.update(routineId = id, cycle = "FREQ=DAILY;BYDAY=SA,SU") }, storedDetail.copy(cycle = RoutineCycle.WEEKENDS)),
                    Pair({ action -> action.update(routineId = id, unit = "L") }, storedDetail.copy(unit = "L")),
                    Pair({ action -> action.update(routineId = id, unit = "") }, storedDetail.copy(unit = "")),
                    Pair({ action -> action.update(routineId = id, title = "물", unit = null) }, storedDetail.copy(title = "물")),
                ).forEach { (request, expected) ->
                    val changedCount = Arb.int(0..1).next()
                    val parameter = slot<UpdateRoutineUseCase.Parameter>()
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    coEvery { updateRoutineUseCase(parameter = capture(parameter)) } returns Result.success(changedCount)
                    val action = routineAppActionOf(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    request(action) shouldBe AppActionChangedCount(changedCount = changedCount)
                    parameter.captured.id shouldBe stored.id
                    parameter.captured.input.recordedDetail() shouldBe expected
                }
            }
        }

        test("TC-17e554d0-2e98-4d72-af3d-e552bbbf6a2a 평일만과 주말만 루틴의 상세도 하는 요일이 아닌 날에 오늘의 합계와 달성률을 담는다") {
            runTest {
                // 이번 주기의 합계는 저장소가 오늘 날짜의 기록만 더해 넘기고 AccountRoutineLocalDataSourceImplTest가 확인한다.
                listOf(
                    Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 20), 500L to 25),
                    Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 21), 1500L to 75),
                    Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 3, 21), 1500L to 75),
                    Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 3, 20), 500L to 25),
                ).forEach { (cycle, today, expected) ->
                    val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1), cycle = cycle, unit = "ml", goalAmount = 2000)).next()
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = today)) } returns
                        flowOf(Result.success(RoutineSummary(routine = routine, cycleAmount = expected.first)))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    val action = routineAppActionOf(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = today))

                    action.get(routineId = routine.id.toString())?.routine?.progress shouldBe
                        AppActionRoutineProgress(status = "in_progress", cycleAmount = expected.first, achievementRate = expected.second)
                }
            }
        }

        test("TC-eaca35b0-4e82-4806-8e6d-1304d4811e22 완료되지 않은 루틴의 목록에는 RoutineHome의 보기 범위를 적용하지 않는다") {
            runTest {
                // 목록은 보기 범위를 입력으로 받지 않는 조회만 쓰므로, 저장된 보기 범위가 무엇이든 같은 조회를 한 것과 같다.
                val saturday = LocalDate(2026, 3, 21)
                val summaryList =
                    listOf(
                        Triple("가", RoutineCycle.DAILY, LocalDate(2026, 3, 1)),
                        Triple("나", RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 1)),
                        Triple("다", RoutineCycle.WEEKENDS, LocalDate(2026, 3, 1)),
                        Triple("라", RoutineCycle.DAILY, LocalDate(2026, 3, 22)),
                    ).map { (title, cycle, startDate) ->
                        RoutineSummary(routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = title, startDate = startDate, cycle = cycle)).next(), cycleAmount = 0)
                    }
                val pageRoutineUseCase = mockk<PageRoutineUseCase>()
                every { pageRoutineUseCase(parameter = saturday) } returns flowOf(Result.success(PagingData.from(summaryList)))
                val action = routineListAppActionOf(pageRoutineUseCase, clockAt(date = saturday))

                action.list(cursor = null).items.map { routine -> routine.title } shouldBe listOf("가", "나", "다", "라")
            }
        }

        test("TC-4472dc4c-882b-4282-a630-d1d0338a9507 기록을 넣을 때 날짜의 요일이 하는 요일에 드는지는 확인하지 않는다") {
            runTest {
                listOf(
                    Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 20), "2026-03-14") to LocalDate(2026, 3, 14),
                    Triple(RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 21), null) to LocalDate(2026, 3, 21),
                    Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 3, 20), "2026-03-18") to LocalDate(2026, 3, 18),
                    Triple(RoutineCycle.WEEKENDS, LocalDate(2026, 3, 20), null) to LocalDate(2026, 3, 20),
                ).forEach { (given, expectedDate) ->
                    val (cycle, today, date) = given
                    val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1), cycle = cycle, amountPerRecord = 250)).next()
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                    val action = routineRecordAppActionOf(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = today))

                    action.add(routineId = routine.id.toString(), date = date, amount = null) shouldBe AppActionChangedCount(changedCount = 1)
                    coVerify(exactly = 1) {
                        addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routine.id, date = expectedDate, amount = 250))
                    }
                }
            }
        }

        test("TC-44e4d402-c828-4487-91c1-f5482d13bce6 유효 범위를 벗어나는 요청은 실패하고 루틴을 추가하거나 바꾸지 않는다") {
            runTest {
                val title = Arb.nonBlankString().next()
                listOf<Pair<suspend (RoutineAppAction) -> Any, String>>(
                    Pair({ action -> action.add(title = title, goalAmount = 0) }, GOAL_AMOUNT_FAILURE_MESSAGE),
                    Pair({ action -> action.add(title = title, goalAmount = 10_000_000) }, GOAL_AMOUNT_FAILURE_MESSAGE),
                    Pair({ action -> action.add(title = title, amountPerRecord = 0) }, AMOUNT_PER_RECORD_FAILURE_MESSAGE),
                    Pair({ action -> action.add(title = title, amountPerRecord = 10_000_000) }, AMOUNT_PER_RECORD_FAILURE_MESSAGE),
                ).forEach { (request, message) ->
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    val action = routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> { request(action) }.message shouldBe message
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(goalAmount = 2000, amountPerRecord = 250)).next()
                val id = stored.id.toString()
                listOf<Pair<suspend (RoutineAppAction) -> Any, String>>(
                    Pair({ action -> action.update(routineId = id, goalAmount = 0) }, GOAL_AMOUNT_FAILURE_MESSAGE),
                    Pair({ action -> action.update(routineId = id, amountPerRecord = 10_000_000) }, AMOUNT_PER_RECORD_FAILURE_MESSAGE),
                ).forEach { (request, message) ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val action = routineAppActionOf(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> { request(action) }.message shouldBe message
                    coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
                }
            }
        }

        test("TC-54884f3b-f5c2-40c7-8728-e94bebdac8b6 유효 범위의 경계에 있는 값으로는 루틴을 추가한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                listOf<suspend (RoutineAppAction) -> AppActionId>(
                    { action -> action.add(title = title, goalAmount = 1, amountPerRecord = 1) },
                    { action -> action.add(title = title, goalAmount = 9_999_999, amountPerRecord = 9_999_999) },
                ).zip(
                    listOf<(RoutineInput) -> Unit>(
                        { input -> (input.goalAmount to input.amountPerRecord) shouldBe (1 to 1) },
                        { input -> (input.goalAmount to input.amountPerRecord) shouldBe (9_999_999 to 9_999_999) },
                    ),
                ).forEach { (request, verifyInput) ->
                    val addedId = Arb.kotlinUuid().next()
                    val parameter = slot<AddRoutineUseCase.Parameter>()
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(addedId)

                    request(routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY))) shouldBe AppActionId(id = addedId.toString())
                    verifyInput(parameter.captured.input)
                }
            }
        }

        test("TC-e7062502-ec61-4ed1-9cc6-9810ce13b13f 제목만 담아 추가하면 RoutineAdd가 처음 채우는 값으로 정한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())

                routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY)).add(title = title)

                parameter.captured shouldBe
                    AddRoutineUseCase.Parameter(
                        input =
                            RoutineInput(
                                title = title,
                                description = "",
                                color = parameter.captured.input.color,
                                startDate = TODAY,
                                cycle = RoutineCycle.DAILY,
                                unit = "",
                                goalAmount = null,
                                amountPerRecord = 1,
                            ),
                        tagIdSet = emptySet(),
                    )
            }
        }

        test("TC-fbc419a9-5103-4eff-9f2c-243636501457 추가는 무작위 컬러로 정한다") {
            runTest {
                val color = Arb.long().next()
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                val randomColor = mockk<AppActionRandomColor>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                every { randomColor.next() } returns color

                routineAppActionOf(addRoutineUseCase, randomColor, clockAt(date = TODAY)).add(title = Arb.nonBlankString().next())

                parameter.captured.input.color shouldBe color
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가는 담은 정보와 태그를 그대로 넘긴다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())

                routineAppActionOf(addRoutineUseCase, clockAt(date = TODAY)).add(
                    title = title,
                    description = "하루 2L",
                    startDate = "2026-01-31",
                    cycle = "FREQ=MONTHLY",
                    unit = "ml",
                    goalAmount = 2000,
                    amountPerRecord = 250,
                    tagIds = tagIdSet.map { tagId -> tagId.toString() },
                )

                parameter.captured.tagIdSet shouldBe tagIdSet
                parameter.captured.input.recordedDetail() shouldBe
                    RoutineDetail(
                        title = title,
                        description = "하루 2L",
                        color = parameter.captured.input.color,
                        startDate = LocalDate(2026, 1, 31),
                        cycle = RoutineCycle.MONTHLY,
                        unit = "ml",
                        goalAmount = 2000,
                        amountPerRecord = 250,
                    )
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 상세는 연결된 태그와 함께 돌려주고, 없는 루틴은 null을 돌려준다") {
            runTest {
                val routine = Arb.routine(isDeleted = false).next()
                val missingId = Arb.kotlinUuid().next()
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = TODAY)) } returns
                    flowOf(Result.success(RoutineSummary(routine = routine, cycleAmount = 0)))
                every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = missingId, today = TODAY)) } returns flowOf(Result.success(null))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.ROUTINE, entityId = routine.id)) } returns flowOf(Result.success(tagList))
                val action = routineAppActionOf(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))

                action.get(routineId = routine.id.toString())?.tags?.map { tag -> tag.id } shouldBe tagList.map { tag -> tag.id.toString() }
                action.get(routineId = missingId.toString()) shouldBe null
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 루틴의 상세는 null이고, 삭제된 항목을 포함한 조회에서만 나온다") {
            runTest {
                val deleted = Arb.routine().next().copy(isDeleted = true)
                val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findRoutineSummaryUseCase(parameter = any()) } returns flowOf(Result.success(RoutineSummary(routine = deleted, cycleAmount = 0)))
                every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                val action = routineAppActionOf(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))

                action.get(routineId = deleted.id.toString()) shouldBe null
                action.getIncludingDeleted(routineId = deleted.id.toString())?.routine?.id shouldBe deleted.id.toString()
            }
        }

        test("TC-77c32c1d-a54a-480f-9654-066a1f73fa1f 삭제와 되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val routine = Arb.routine(isDeleted = false).next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = routine.id) } returns Result.success(1)
                coEvery { restoreRoutineUseCase(parameter = routine.id) } returns Result.success(1)
                val action = routineAppActionOf(foundRoutineUseCase(routine), deleteRoutineUseCase, restoreRoutineUseCase)

                action.delete(routineId = routine.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)
                action.restore(routineId = routine.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)
            }
        }

        test("TC-3ac1d111-84b2-4444-aeec-c2464eaf5912 기록 조회는 그 루틴의 기록 내역을 받은 순서대로 날짜와 양과 함께 돌려준다") {
            runTest {
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(unit = "ml")).next()
                val recordList =
                    listOf(LocalDate(2026, 3, 20) to 500, LocalDate(2026, 3, 20) to 250, LocalDate(2026, 3, 19) to 300)
                        .map { (date, amount) -> Arb.routineRecord().next().copy(routineId = routine.id, date = date, amount = amount, isDeleted = false) }
                val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
                every { pageRoutineRecordUseCase(parameter = routine.id) } returns flowOf(Result.success(PagingData.from(recordList)))
                val action = routineRecordAppActionOf(foundRoutineUseCase(routine), pageRoutineRecordUseCase)

                action.list(routineId = routine.id.toString(), cursor = null) shouldBe AppActionPage(items = recordList.map { record -> record.expected() }, nextCursor = null)
            }
        }

        test("TC-ac3ef6dd-ea4d-4fd3-80f2-489adacf3019 기록을 넣을 때 담지 않은 날짜와 양은 오늘과 한 번에 기록할 양으로 정한다") {
            runTest {
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1), amountPerRecord = 250)).next()
                listOf(
                    Triple(null, null, LocalDate(2026, 3, 20) to 250),
                    Triple("2026-03-10", null, LocalDate(2026, 3, 10) to 250),
                    Triple(null, 500, LocalDate(2026, 3, 20) to 500),
                    Triple("2026-03-31", 9_999_999, LocalDate(2026, 3, 31) to 9_999_999),
                    Triple("2026-03-01", 1, LocalDate(2026, 3, 1) to 1),
                ).forEach { (date, amount, expected) ->
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                    val action = routineRecordAppActionOf(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    action.add(routineId = routine.id.toString(), date = date, amount = amount) shouldBe AppActionChangedCount(changedCount = 1)
                    coVerify(exactly = 1) {
                        addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routine.id, date = expected.first, amount = expected.second))
                    }
                }
            }
        }

        test("TC-2fc11cd2-1116-495e-9302-157b96f38032 기록할 수 없는 날짜의 기록을 넣는 요청은 실패하고 기록을 넣지 않는다") {
            runTest {
                listOf(
                    LocalDate(2026, 3, 1) to "2026-02-28",
                    LocalDate(2026, 3, 21) to "2026-03-20",
                    LocalDate(2026, 3, 21) to null,
                ).forEach { (startDate, date) ->
                    val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = startDate)).next()
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    val action = routineRecordAppActionOf(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> { action.add(routineId = routine.id.toString(), date = date, amount = null) }.message shouldBe RECORD_DATE_FAILURE_MESSAGE
                    coVerify(exactly = 0) { addRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("TC-2f59b5b8-e95d-4901-96ad-c99a3fb3d381 유효 범위를 벗어난 양의 기록을 넣는 요청은 실패하고 기록을 넣지 않는다") {
            runTest {
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1))).next()
                listOf(0, -1, 10_000_000).forEach { amount ->
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    val action = routineRecordAppActionOf(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    shouldThrow<AppActionException.InvalidArgument> { action.add(routineId = routine.id.toString(), date = null, amount = amount) }.message shouldBe RECORD_AMOUNT_FAILURE_MESSAGE
                    coVerify(exactly = 0) { addRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("TC-959d66d2-645c-4919-bddf-4b973f57edc6 기록을 빼면 그 기록이 기록 조회에서 빠진다") {
            runTest {
                val routine = Arb.routine(isDeleted = false).next()
                val savedRecordList = List(2) { Arb.routineRecord().next().copy(routineId = routine.id, isDeleted = false) }.toMutableList()
                val removed = savedRecordList.first()
                val kept = savedRecordList.last()
                val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
                val findRoutineRecordUseCase = mockk<FindRoutineRecordUseCase>()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                every { pageRoutineRecordUseCase(parameter = routine.id) } answers { flowOf(Result.success(PagingData.from(savedRecordList.toList()))) }
                every { findRoutineRecordUseCase(parameter = removed.id) } returns flowOf(Result.success(removed))
                coEvery { deleteRoutineRecordUseCase(parameter = removed.id) } answers {
                    savedRecordList.remove(removed)
                    Result.success(1)
                }
                val action = routineRecordAppActionOf(foundRoutineUseCase(routine), pageRoutineRecordUseCase, findRoutineRecordUseCase, deleteRoutineRecordUseCase)

                action.list(routineId = routine.id.toString(), cursor = null).items shouldBe listOf(removed.expected(), kept.expected())
                action.remove(recordId = removed.id.toString()) shouldBe AppActionChangedCount(changedCount = 1)
                action.list(routineId = routine.id.toString(), cursor = null).items shouldBe listOf(kept.expected())
            }
        }

        test("TC-36b8ea86-c81f-45f3-a1ce-19c3e539d7cb 없거나 삭제된 루틴에 넣을 수 없는 기록을 넣으면 실패 대신 넣은 기록이 없다는 결과를 받는다") {
            runTest {
                val deleted =
                    Arb.routine().next().let { routine ->
                        routine.copy(isDeleted = true, detail = routine.detail.copy(startDate = LocalDate(2026, 3, 10)))
                    }
                listOf<Triple<Routine?, String?, Int?>>(
                    Triple(null, null, 0),
                    Triple(deleted, null, 0),
                    Triple(deleted, "2026-03-09", null),
                    Triple(deleted, "2026-03-10", 250),
                ).forEach { (routine, date, amount) ->
                    val routineId = routine?.id ?: Arb.kotlinUuid().next()
                    val findRoutineUseCase = mockk<FindRoutineUseCase>()
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    every { findRoutineUseCase(parameter = routineId) } returns flowOf(Result.success(routine))
                    val action = routineRecordAppActionOf(findRoutineUseCase, addRoutineRecordUseCase, clockAt(date = TODAY))

                    action.add(routineId = routineId.toString(), date = date, amount = amount) shouldBe AppActionChangedCount(changedCount = 0)
                    coVerify(exactly = 0) { addRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("TC-31b4b63f-09f6-48e4-9f73-6d2c095a695f 이미 빠졌거나 없거나 삭제된 루틴에 속한 기록을 빼면 바뀐 것이 없다는 결과를 받는다") {
            runTest {
                val activeRoutine = Arb.routine(isDeleted = false).next()
                val deletedRoutine = Arb.routine().next().copy(isDeleted = true)
                listOf<Pair<RoutineRecord?, Routine?>>(
                    null to activeRoutine,
                    Arb.routineRecord().next().copy(routineId = activeRoutine.id, isDeleted = true) to activeRoutine,
                    Arb.routineRecord().next().copy(routineId = deletedRoutine.id, isDeleted = false) to deletedRoutine,
                    Arb.routineRecord().next().copy(isDeleted = false) to null,
                ).forEach { (record, routine) ->
                    val recordId = record?.id ?: Arb.kotlinUuid().next()
                    val findRoutineRecordUseCase = mockk<FindRoutineRecordUseCase>()
                    val findRoutineUseCase = mockk<FindRoutineUseCase>()
                    val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                    every { findRoutineRecordUseCase(parameter = recordId) } returns flowOf(Result.success(record))
                    every { findRoutineUseCase(parameter = any()) } returns flowOf(Result.success(routine))
                    val action = routineRecordAppActionOf(findRoutineRecordUseCase, findRoutineUseCase, deleteRoutineRecordUseCase)

                    action.remove(recordId = recordId.toString()) shouldBe AppActionChangedCount(changedCount = 0)
                    coVerify(exactly = 0) { deleteRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("id나 날짜가 형식에 맞지 않으면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val action = routineAppActionOf(clockAt(date = TODAY))
                val recordAction = routineRecordAppActionOf(clockAt(date = TODAY))

                shouldThrow<AppActionException.InvalidArgument> { action.get(routineId = "not-id") }.message shouldBe "'routineId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(title = "제목", startDate = "2026/01/31") }.message shouldBe "'startDate' must be a date like 2026-01-31."
                shouldThrow<AppActionException.InvalidArgument> { recordAction.remove(recordId = "not-id") }.message shouldBe "'recordId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> {
                    recordAction.add(routineId = Arb.kotlinUuid().next().toString(), date = "tomorrow", amount = null)
                }.message shouldBe "'date' must be a date like 2026-01-31."
            }
        }

        test("주기는 매일, 평일만, 주말만, 매주, 매월 순서로 알린다") {
            appActionRoutineCycleList shouldBe listOf(RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKENDS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY)
        }

        test("주기 목록은 모든 주기를 한 번씩 담는다") {
            appActionRoutineCycleList shouldContainExactlyInAnyOrder RoutineCycle.entries
        }
    })

private val TODAY: LocalDate = LocalDate(2026, 3, 20)

private const val CYCLE_FAILURE_MESSAGE = "'cycle' must be one of FREQ=DAILY, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR, FREQ=DAILY;BYDAY=SA,SU, FREQ=WEEKLY;WKST=SU, FREQ=MONTHLY."
private const val UNIT_FAILURE_MESSAGE = "'unit' must be at most 10 characters without line breaks."
private const val GOAL_AMOUNT_FAILURE_MESSAGE = "'goalAmount' must be between 1 and 9999999."
private const val AMOUNT_PER_RECORD_FAILURE_MESSAGE = "'amountPerRecord' must be between 1 and 9999999."
private const val RECORD_AMOUNT_FAILURE_MESSAGE = "'amount' must be between 1 and 9999999."
private const val RECORD_DATE_FAILURE_MESSAGE = "'date' must be on or after the start date of the routine."

private class ProgressCase(
    val startDate: LocalDate,
    val goalAmount: Int?,
    val cycleAmount: Long,
    val expected: AppActionRoutineProgress,
)

private fun foundRoutineUseCase(routine: Routine): FindRoutineUseCase {
    val findRoutineUseCase = mockk<FindRoutineUseCase>()
    every { findRoutineUseCase(parameter = routine.id) } returns flowOf(Result.success(routine))

    return findRoutineUseCase
}

private suspend fun RoutineAppAction.add(
    title: String,
    description: String? = null,
    startDate: String? = null,
    cycle: String? = null,
    unit: String? = null,
    goalAmount: Int? = null,
    amountPerRecord: Int? = null,
    tagIds: List<String> = emptyList(),
): AppActionId =
    add(
        title = title,
        description = description,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
        tagIds = tagIds,
    )

private suspend fun RoutineAppAction.update(
    routineId: String,
    title: String? = null,
    startDate: String? = null,
    cycle: String? = null,
    unit: String? = null,
    goalAmount: Int? = null,
    clearGoalAmount: Boolean? = null,
    amountPerRecord: Int? = null,
): AppActionChangedCount =
    update(
        routineId = routineId,
        title = title,
        description = null,
        color = null,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        clearGoalAmount = clearGoalAmount,
        amountPerRecord = amountPerRecord,
    )

private fun Routine.expected(progress: AppActionRoutineProgress): AppActionRoutine =
    AppActionRoutine(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        startDate = detail.startDate.toString(),
        cycle =
            when (detail.cycle) {
                RoutineCycle.DAILY -> "FREQ=DAILY"
                RoutineCycle.WEEKDAYS -> "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"
                RoutineCycle.WEEKENDS -> "FREQ=DAILY;BYDAY=SA,SU"
                RoutineCycle.WEEKLY -> "FREQ=WEEKLY;WKST=SU"
                RoutineCycle.MONTHLY -> "FREQ=MONTHLY"
            },
        unit = detail.unit,
        goalAmount = detail.goalAmount,
        amountPerRecord = detail.amountPerRecord,
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        progress = progress,
    )

private fun RoutineRecord.expected(): AppActionRoutineRecord =
    AppActionRoutineRecord(
        id = id.toString(),
        routineId = routineId.toString(),
        date = date.toString(),
        amount = amount,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun RoutineInput.recordedDetail(): RoutineDetail =
    RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

private const val RGB = 0xFFFFFFL

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)
