package io.github.taetae98coding.diary.work.mcp.tool.routine

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineSummaryUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.calendar.CalendarMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.json.toColorHex
import io.github.taetae98coding.diary.work.mcp.tool.search.SearchMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagItemMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.uuid.Uuid

class RoutineMcpToolSetTest :
    FunSpec({
        test("TC-68c32f9f-68c5-4b5c-8ac5-74e6e6bff138 상세·목록·검색에 오늘로 계산한 진행 상태, 합계와 달성률을 담는다") {
            runTest {
                listOf(
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 1500, expected = progressJson(status = "in_progress", cycleAmount = 1500, achievementRate = 75)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 2500, expected = progressJson(status = "in_progress", cycleAmount = 2500, achievementRate = 100)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = null, cycleAmount = 1500, expected = progressJson(status = "in_progress", cycleAmount = 1500)),
                    ProgressCase(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, cycleAmount = 0, expected = progressJson(status = "in_progress", cycleAmount = 0, achievementRate = 0)),
                    ProgressCase(startDate = LocalDate(2026, 3, 21), goalAmount = 2000, cycleAmount = 0, expected = progressJson(status = "not_started")),
                ).forEach { case ->
                    val routine =
                        Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = case.startDate, cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = case.goalAmount)).next()
                    val summary = RoutineSummary(routine = routine, cycleAmount = case.cycleAmount)
                    val expected = summary.expectedJson(progress = case.expected)
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    val pageRoutineUseCase = mockk<PageRoutineUseCase>()
                    val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                    every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = TODAY)) } returns flowOf(Result.success(summary))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    every { pageRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = routine.detail.title, today = TODAY)) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineSummaryUseCase, getEntityTagUseCase, pageRoutineUseCase, clockAt(date = TODAY))
                    val searchToolSet = toolSetOf<SearchMcpToolSet>(searchRoutineUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_get").call(arguments = routineIdArguments(routine.id)) shouldBe
                        McpToolCallResult.Success(
                            content =
                                buildJsonObject {
                                    put("routine", expected)
                                    put("tags", JsonArray(emptyList()))
                                },
                        )
                    toolSet.tool(name = "routine_list").call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = pageJson(listOf(expected)))
                    searchToolSet.tool(name = "search_routine").call(arguments = arguments("query" to JsonPrimitive(routine.detail.title))) shouldBe
                        McpToolCallResult.Success(content = pageJson(listOf(expected)))
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
                    val expected = summary.expectedJson(progress = progressJson(status = status))
                    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    val pageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>()
                    val searchRoutineUseCase = mockk<SearchRoutineUseCase>()
                    every { findRoutineSummaryUseCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routine.id, today = TODAY)) } returns flowOf(Result.success(summary))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    every { pageFinishedRoutineUseCase(parameter = TODAY) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    every { searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = routine.detail.title, today = TODAY)) } returns flowOf(Result.success(PagingData.from(listOf(summary))))
                    val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineSummaryUseCase, getEntityTagUseCase, pageFinishedRoutineUseCase, clockAt(date = TODAY))
                    val searchToolSet = toolSetOf<SearchMcpToolSet>(searchRoutineUseCase, clockAt(date = TODAY))

                    expected["isFinished"] shouldBe JsonPrimitive(true)
                    toolSet.tool(name = "routine_get").call(arguments = routineIdArguments(routine.id)) shouldBe
                        McpToolCallResult.Success(
                            content =
                                buildJsonObject {
                                    put("routine", expected)
                                    put("tags", JsonArray(emptyList()))
                                },
                        )
                    toolSet.tool(name = "routine_finished_list").call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = pageJson(listOf(expected)))
                    searchToolSet.tool(name = "search_routine").call(arguments = arguments("query" to JsonPrimitive(routine.detail.title))) shouldBe
                        McpToolCallResult.Success(content = pageJson(listOf(expected)))
                }
            }
        }

        test("TC-a582771d-abb6-4cee-b19d-a863bef19971 추가와 수정은 완료 여부와 마치는 날짜를 받지 않는다") {
            runTest {
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                coEvery { addRoutineUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                listOf("routine_add", "routine_update").forEach { name ->
                    toolSet.tool(name = name).parameterList.map { toolParameter -> toolParameter.name } shouldNotContainAnyOf
                        listOf("isFinished", "finished", "endDate", "clearEndDate")
                }

                toolSet
                    .tool(name = "routine_add")
                    .call(
                        arguments = arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "isFinished" to JsonPrimitive(true), "endDate" to JsonPrimitive("2026-12-31")),
                    ).shouldBeInstanceOf<McpToolCallResult.Success>()
                coVerify(exactly = 1) { addRoutineUseCase(parameter = any()) }
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
                val finishTool = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(unfinished), finishRoutineUseCase, restartRoutineUseCase, updateRoutineUseCase).tool(name = "routine_finish")
                val restartTool = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(finished), finishRoutineUseCase, restartRoutineUseCase, updateRoutineUseCase).tool(name = "routine_restart")

                finishTool.call(arguments = routineIdArguments(unfinished.id)) shouldBe McpToolCallResult.Success(content = changedCountJson(1))
                restartTool.call(arguments = routineIdArguments(finished.id)) shouldBe McpToolCallResult.Success(content = changedCountJson(1))

                listOf(finishTool, restartTool).forEach { tool -> tool.kind shouldBe McpToolKind.WRITE }
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
                val toolSet = toolSetOf<RoutineMcpToolSet>(pageRoutineUseCase, pageFinishedRoutineUseCase, clockAt(date = TODAY))

                listOf("routine_list" to ("가" to false), "routine_finished_list" to ("나" to true)).forEach { (name, expected) ->
                    val tool = toolSet.tool(name = name)
                    val itemList =
                        tool
                            .call(arguments = JsonObject(emptyMap()))
                            .shouldBeInstanceOf<McpToolCallResult.Success>()
                            .content.jsonObject["items"]!!
                            .jsonArray

                    tool.kind shouldBe McpToolKind.READ
                    itemList.map { item -> item.jsonObject["title"] to item.jsonObject["isFinished"] } shouldBe listOf(JsonPrimitive(expected.first) to JsonPrimitive(expected.second))
                }
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

                val content =
                    toolSetOf<SearchMcpToolSet>(searchRoutineUseCase, clockAt(date = TODAY))
                        .tool(name = "search_routine")
                        .call(arguments = arguments("query" to JsonPrimitive("운동")))
                        .shouldBeInstanceOf<McpToolCallResult.Success>()
                        .content.jsonObject

                content["items"]!!.jsonArray.map { item -> item.jsonObject["title"] to item.jsonObject["isFinished"] } shouldBe
                    listOf(JsonPrimitive("운동 가") to JsonPrimitive(false), JsonPrimitive("운동 나") to JsonPrimitive(true))
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
                val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), addRoutineRecordUseCase, findRoutineRecordUseCase, deleteRoutineRecordUseCase, clockAt(date = TODAY))

                toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = null, amount = null)) shouldBe
                    McpToolCallResult.Success(content = changedCountJson(1))
                toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = "2026-03-10", amount = 500)) shouldBe
                    McpToolCallResult.Success(content = changedCountJson(1))
                toolSet.tool(name = "routine_record_remove").call(arguments = arguments("recordId" to JsonPrimitive(record.id.toString()))) shouldBe
                    McpToolCallResult.Success(content = changedCountJson(1))

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
                val toolSet = toolSetOf<RoutineMcpToolSet>(pageRoutineUseCase, clockAt(date = TODAY))
                val searchToolSet = toolSetOf<SearchMcpToolSet>(searchRoutineUseCase, clockAt(date = TODAY))

                listOf(
                    toolSet.tool(name = "routine_list").call(arguments = JsonObject(emptyMap())),
                    searchToolSet.tool(name = "search_routine").call(arguments = arguments("query" to JsonPrimitive("운동"))),
                ).forEach { result ->
                    val content = result.shouldBeInstanceOf<McpToolCallResult.Success>().content.jsonObject

                    content["items"]!!.jsonArray.map { item -> item.jsonObject["title"] } shouldBe listOf("운동 다", "운동 라", "운동 가", "운동 나").map(::JsonPrimitive)
                    content["items"]!!.jsonArray.map { item -> item.jsonObject["progress"]!!.jsonObject["status"] } shouldBe
                        listOf("in_progress", "in_progress", "not_started", "not_started").map(::JsonPrimitive)
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_add").call(
                        arguments = arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "cycle" to JsonPrimitive(cycleValue)),
                    ) shouldBe McpToolCallResult.Success(content = buildJsonObject { put("id", addedId.toString()) })
                    parameter.captured.input.cycle shouldBe recorded

                    toolSet
                        .tool(name = "routine_get")
                        .call(arguments = routineIdArguments(addedId))
                        .shouldBeInstanceOf<McpToolCallResult.Success>()
                        .content.jsonObject["routine"]!!
                        .jsonObject["cycle"] shouldBe JsonPrimitive(cycleValue)
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = TODAY))
                    val addArguments =
                        if (unitValue == null) {
                            arguments("title" to JsonPrimitive(Arb.nonBlankString().next()))
                        } else {
                            arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "unit" to JsonPrimitive(unitValue))
                        }

                    toolSet.tool(name = "routine_add").call(arguments = addArguments) shouldBe McpToolCallResult.Success(content = buildJsonObject { put("id", addedId.toString()) })
                    parameter.captured.input.unit shouldBe unitValue.orEmpty()
                    parameter.captured.input.unit
                        .trim() shouldBe recorded

                    toolSet
                        .tool(name = "routine_get")
                        .call(arguments = routineIdArguments(addedId))
                        .shouldBeInstanceOf<McpToolCallResult.Success>()
                        .content.jsonObject["routine"]!!
                        .jsonObject["unit"] shouldBe JsonPrimitive(recorded)
                }
            }
        }

        test("TC-9906df56-12e2-4162-b857-8ddcb290493d 단위가 10자를 넘거나 줄바꿈을 담고 있으면 요청이 실패하고 잘라서 받아들이지 않는다") {
            runTest {
                listOf("kilometerss", "가나다라마바사아자차카", "kilometers per hour", "밀리\n리터").forEach { unit ->
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                    toolSet.tool(name = "routine_add").call(
                        arguments = arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "unit" to JsonPrimitive(unit)),
                    ) shouldBe McpToolCallResult.Failure(message = UNIT_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(unit = "ml")).next()
                listOf("kilometerss", "밀리\n리터").forEach { unit ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_update").call(
                        arguments = arguments("routineId" to JsonPrimitive(stored.id.toString()), "unit" to JsonPrimitive(unit)),
                    ) shouldBe McpToolCallResult.Failure(message = UNIT_FAILURE_MESSAGE)
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                    toolSet.tool(name = "routine_add").call(
                        arguments = arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "cycle" to JsonPrimitive(cycle)),
                    ) shouldBe McpToolCallResult.Failure(message = CYCLE_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(cycle = RoutineCycle.DAILY)).next()
                listOf("FREQ=YEARLY", "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR,SA").forEach { cycle ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_update").call(
                        arguments = arguments("routineId" to JsonPrimitive(stored.id.toString()), "cycle" to JsonPrimitive(cycle)),
                    ) shouldBe McpToolCallResult.Failure(message = CYCLE_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
                }
            }
        }

        test("TC-f3d2b737-4227-4136-a021-1cc493cd40ac 수정은 담은 정보만 바꾸고 목표량은 지우는 요청으로, 단위는 빈 글자를 담아 비운다") {
            runTest {
                val stored =
                    Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(title = "물 마시기", startDate = LocalDate(2026, 1, 1), cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = 2000, amountPerRecord = 250)).next()
                val storedDetail = stored.detail
                listOf<Pair<Pair<String, JsonElement>, RoutineDetail>>(
                    ("title" to JsonPrimitive("물")) to storedDetail.copy(title = "물"),
                    ("clearGoalAmount" to JsonPrimitive(true)) to storedDetail.copy(goalAmount = null),
                    ("goalAmount" to JsonPrimitive(3000)) to storedDetail.copy(goalAmount = 3000),
                    ("cycle" to JsonPrimitive("FREQ=WEEKLY;WKST=SU")) to storedDetail.copy(cycle = RoutineCycle.WEEKLY),
                    ("cycle" to JsonPrimitive("FREQ=DAILY;BYDAY=SA,SU")) to storedDetail.copy(cycle = RoutineCycle.WEEKENDS),
                    ("unit" to JsonPrimitive("L")) to storedDetail.copy(unit = "L"),
                    ("unit" to JsonPrimitive("")) to storedDetail.copy(unit = ""),
                ).forEach { (requested, expected) ->
                    val changedCount = Arb.int(0..1).next()
                    val parameter = slot<UpdateRoutineUseCase.Parameter>()
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    coEvery { updateRoutineUseCase(parameter = capture(parameter)) } returns Result.success(changedCount)
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_update").call(arguments = arguments("routineId" to JsonPrimitive(stored.id.toString()), requested)) shouldBe
                        McpToolCallResult.Success(content = changedCountJson(changedCount))
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineSummaryUseCase, getEntityTagUseCase, clockAt(date = today))

                    toolSet
                        .tool(name = "routine_get")
                        .call(arguments = routineIdArguments(routine.id))
                        .shouldBeInstanceOf<McpToolCallResult.Success>()
                        .content.jsonObject["routine"]!!
                        .jsonObject["progress"] shouldBe progressJson(status = "in_progress", cycleAmount = expected.first, achievementRate = expected.second)
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
                val toolSet = toolSetOf<RoutineMcpToolSet>(pageRoutineUseCase, clockAt(date = saturday))

                toolSet
                    .tool(name = "routine_list")
                    .call(arguments = JsonObject(emptyMap()))
                    .shouldBeInstanceOf<McpToolCallResult.Success>()
                    .content.jsonObject["items"]!!
                    .jsonArray
                    .map { item -> item.jsonObject["title"] } shouldBe listOf("가", "나", "다", "라").map { title -> JsonPrimitive(title) }
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = today))

                    toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = date, amount = null)) shouldBe
                        McpToolCallResult.Success(content = changedCountJson(1))
                    coVerify(exactly = 1) {
                        addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routine.id, date = expectedDate, amount = 250))
                    }
                }
            }
        }

        test("TC-44e4d402-c828-4487-91c1-f5482d13bce6 유효 범위를 벗어나는 요청은 실패하고 루틴을 추가하거나 바꾸지 않는다") {
            runTest {
                listOf(
                    mapOf("goalAmount" to JsonPrimitive(0)) to GOAL_AMOUNT_FAILURE_MESSAGE,
                    mapOf("goalAmount" to JsonPrimitive(10_000_000)) to GOAL_AMOUNT_FAILURE_MESSAGE,
                    mapOf("amountPerRecord" to JsonPrimitive(0)) to AMOUNT_PER_RECORD_FAILURE_MESSAGE,
                    mapOf("amountPerRecord" to JsonPrimitive(10_000_000)) to AMOUNT_PER_RECORD_FAILURE_MESSAGE,
                ).forEach { (requested, message) ->
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                    toolSet.tool(name = "routine_add").call(arguments = JsonObject(requested + ("title" to JsonPrimitive(Arb.nonBlankString().next())))) shouldBe
                        McpToolCallResult.Failure(message = message)
                    coVerify(exactly = 0) { addRoutineUseCase(parameter = any()) }
                }

                val stored = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(goalAmount = 2000, amountPerRecord = 250)).next()
                listOf(
                    mapOf("goalAmount" to JsonPrimitive(0)) to GOAL_AMOUNT_FAILURE_MESSAGE,
                    mapOf("amountPerRecord" to JsonPrimitive(10_000_000)) to AMOUNT_PER_RECORD_FAILURE_MESSAGE,
                ).forEach { (requested, message) ->
                    val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(stored), updateRoutineUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_update").call(arguments = JsonObject(requested + ("routineId" to JsonPrimitive(stored.id.toString())))) shouldBe
                        McpToolCallResult.Failure(message = message)
                    coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
                }
            }
        }

        test("TC-54884f3b-f5c2-40c7-8728-e94bebdac8b6 유효 범위의 경계에 있는 값으로는 루틴을 추가한다") {
            runTest {
                listOf(
                    mapOf("goalAmount" to JsonPrimitive(1), "amountPerRecord" to JsonPrimitive(1)),
                    mapOf("goalAmount" to JsonPrimitive(9_999_999), "amountPerRecord" to JsonPrimitive(9_999_999)),
                ).forEach { requested ->
                    val addedId = Arb.kotlinUuid().next()
                    val parameter = slot<AddRoutineUseCase.Parameter>()
                    val addRoutineUseCase = mockk<AddRoutineUseCase>()
                    coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                    val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                    toolSet.tool(name = "routine_add").call(arguments = JsonObject(requested + ("title" to JsonPrimitive(Arb.nonBlankString().next())))) shouldBe
                        McpToolCallResult.Success(content = buildJsonObject { put("id", addedId.toString()) })

                    val input = parameter.captured.input
                    requested["goalAmount"]?.let { goalAmount -> JsonPrimitive(input.goalAmount) shouldBe goalAmount }
                    requested["amountPerRecord"]?.let { amountPerRecord -> JsonPrimitive(input.amountPerRecord) shouldBe amountPerRecord }
                }
            }
        }

        test("TC-e7062502-ec61-4ed1-9cc6-9810ce13b13f 제목만 담아 추가하면 RoutineAdd가 처음 채우는 값으로 정한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                toolSet.tool(name = "routine_add").call(arguments = arguments("title" to JsonPrimitive(title))).shouldBeInstanceOf<McpToolCallResult.Success>()

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

        test("TC-fbc419a9-5103-4eff-9f2c-243636501457 추가는 컬러를 받지 않고 무작위 컬러로 정한다") {
            runTest {
                val color = Arb.long().next()
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                val randomColor = mockk<AppActionRandomColor>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                every { randomColor.next() } returns color
                val tool = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, randomColor, clockAt(date = TODAY)).tool(name = "routine_add")

                tool.call(arguments = arguments("title" to JsonPrimitive(Arb.nonBlankString().next()), "color" to JsonPrimitive("#4285F4"))).shouldBeInstanceOf<McpToolCallResult.Success>()

                tool.parameterList.map { toolParameter -> toolParameter.name } shouldNotContain "color"
                parameter.captured.input.color shouldBe color
            }
        }

        test("TC-0d4d8880-d9d4-4ca1-ab25-ece33555ffd1 루틴 목록과 루틴 검색은 정렬을 입력으로 받지 않는다") {
            listOf(
                toolSetOf<RoutineMcpToolSet>().tool(name = "routine_list"),
                toolSetOf<SearchMcpToolSet>().tool(name = "search_routine"),
            ).forEach { tool -> tool.parameterList.map { toolParameter -> toolParameter.name } shouldNotContain "sort" }
        }

        test("TC-539bcdd3-24fa-4cc0-9c3b-7ab9cbe0b02c 날짜 범위로 루틴의 날짜별 합계를 돌려주는 도구는 없다") {
            val routineToolList =
                (toolSetOf<RoutineMcpToolSet>().toolList + toolSetOf<SearchMcpToolSet>().toolList + toolSetOf<TagItemMcpToolSet>().toolList + toolSetOf<CalendarMcpToolSet>().toolList)
                    .filter { tool -> "routine" in tool.name }

            routineToolList.map { tool -> tool.name } shouldContainExactlyInAnyOrder
                listOf(
                    "routine_list",
                    "routine_finished_list",
                    "routine_get",
                    "routine_add",
                    "routine_update",
                    "routine_finish",
                    "routine_restart",
                    "routine_delete",
                    "routine_restore",
                    "routine_tag_add",
                    "routine_tag_remove",
                    "routine_record_list",
                    "routine_record_add",
                    "routine_record_remove",
                    "search_routine",
                    "tag_routine_list",
                    "tag_routine_finished_list",
                )
            routineToolList.filter { tool -> tool.kind == McpToolKind.READ }.forEach { tool ->
                tool.parameterList.map { toolParameter -> toolParameter.name } shouldNotContainAnyOf listOf("startDate", "endDate")
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
                val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), pageRoutineRecordUseCase)

                toolSet.tool(name = "routine_record_list").call(arguments = routineIdArguments(routine.id)) shouldBe
                    McpToolCallResult.Success(content = pageJson(recordList.map { record -> record.expectedJson() }))
                verify(exactly = 1) { pageRoutineRecordUseCase(parameter = routine.id) }
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = date, amount = amount)) shouldBe
                        McpToolCallResult.Success(content = changedCountJson(1))
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = date, amount = null)) shouldBe
                        McpToolCallResult.Failure(message = RECORD_DATE_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { addRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("TC-2f59b5b8-e95d-4901-96ad-c99a3fb3d381 유효 범위를 벗어난 양의 기록을 넣는 요청은 실패하고 기록을 넣지 않는다") {
            runTest {
                val routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail().next().copy(startDate = LocalDate(2026, 3, 1))).next()
                listOf(0, -1, 10_000_000).forEach { amount ->
                    val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                    val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), addRoutineRecordUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routine.id, date = null, amount = amount)) shouldBe
                        McpToolCallResult.Failure(message = RECORD_AMOUNT_FAILURE_MESSAGE)
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
                val toolSet = toolSetOf<RoutineMcpToolSet>(foundRoutineUseCase(routine), pageRoutineRecordUseCase, findRoutineRecordUseCase, deleteRoutineRecordUseCase)
                val listTool = toolSet.tool(name = "routine_record_list")

                listTool.call(arguments = routineIdArguments(routine.id)) shouldBe McpToolCallResult.Success(content = pageJson(listOf(removed.expectedJson(), kept.expectedJson())))
                toolSet.tool(name = "routine_record_remove").call(arguments = arguments("recordId" to JsonPrimitive(removed.id.toString()))) shouldBe
                    McpToolCallResult.Success(content = changedCountJson(1))
                listTool.call(arguments = routineIdArguments(routine.id)) shouldBe McpToolCallResult.Success(content = pageJson(listOf(kept.expectedJson())))
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
                    val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineUseCase, addRoutineRecordUseCase, clockAt(date = TODAY))

                    toolSet.tool(name = "routine_record_add").call(arguments = recordAddArguments(routineId = routineId, date = date, amount = amount)) shouldBe
                        McpToolCallResult.Success(content = changedCountJson(0))
                    coVerify(exactly = 0) { addRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("TC-31b4b63f-09f6-48e4-9f73-6d2c095a695f 이미 빠졌거나 없거나 삭제된 루틴에 속한 기록을 빼면 바뀐 것이 없다는 결과를 받는다") {
            runTest {
                val activeRoutine = Arb.routine(isDeleted = false).next()
                val deletedRoutine = Arb.routine().next().copy(isDeleted = true)
                val missingRoutineId = Arb.kotlinUuid().next()
                listOf<Pair<RoutineRecord?, Routine?>>(
                    null to activeRoutine,
                    Arb.routineRecord().next().copy(routineId = activeRoutine.id, isDeleted = true) to activeRoutine,
                    Arb.routineRecord().next().copy(routineId = deletedRoutine.id, isDeleted = false) to deletedRoutine,
                    Arb.routineRecord().next().copy(routineId = missingRoutineId, isDeleted = false) to null,
                ).forEach { (record, routine) ->
                    val recordId = record?.id ?: Arb.kotlinUuid().next()
                    val findRoutineRecordUseCase = mockk<FindRoutineRecordUseCase>()
                    val findRoutineUseCase = mockk<FindRoutineUseCase>()
                    val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                    every { findRoutineRecordUseCase(parameter = recordId) } returns flowOf(Result.success(record))
                    every { findRoutineUseCase(parameter = any()) } returns flowOf(Result.success(routine))
                    val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineRecordUseCase, findRoutineUseCase, deleteRoutineRecordUseCase)

                    toolSet.tool(name = "routine_record_remove").call(arguments = arguments("recordId" to JsonPrimitive(recordId.toString()))) shouldBe
                        McpToolCallResult.Success(content = changedCountJson(0))
                    coVerify(exactly = 0) { deleteRoutineRecordUseCase(parameter = any()) }
                }
            }
        }

        test("추가는 담은 정보와 태그를 그대로 넘긴다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val tagId = Arb.kotlinUuid().next()
                val parameter = slot<AddRoutineUseCase.Parameter>()
                val addRoutineUseCase = mockk<AddRoutineUseCase>()
                coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val toolSet = toolSetOf<RoutineMcpToolSet>(addRoutineUseCase, AppActionRandomColor(), clockAt(date = TODAY))

                toolSet
                    .tool(name = "routine_add")
                    .call(
                        arguments =
                            arguments(
                                "title" to JsonPrimitive(title),
                                "description" to JsonPrimitive("하루 2L"),
                                "startDate" to JsonPrimitive("2026-01-31"),
                                "cycle" to JsonPrimitive("FREQ=MONTHLY"),
                                "unit" to JsonPrimitive("ml"),
                                "goalAmount" to JsonPrimitive(2000),
                                "amountPerRecord" to JsonPrimitive(250),
                                "tagIds" to JsonArray(listOf(JsonPrimitive(tagId.toString()))),
                            ),
                    ).shouldBeInstanceOf<McpToolCallResult.Success>()

                parameter.captured.tagIdSet shouldBe setOf(tagId)
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

        test("없는 루틴은 상세가 null이고, 수정하면 앱 기능을 실행하지 않고 바뀐 수 0을 돌려준다") {
            runTest {
                val routineId = Arb.kotlinUuid().next()
                val findRoutineUseCase = mockk<FindRoutineUseCase>()
                val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                every { findRoutineUseCase(parameter = routineId) } returns flowOf(Result.success(null))
                every { findRoutineSummaryUseCase(parameter = any()) } returns flowOf(Result.success(null))
                val toolSet = toolSetOf<RoutineMcpToolSet>(findRoutineUseCase, findRoutineSummaryUseCase, updateRoutineUseCase, clockAt(date = TODAY))
                val requested = arguments("routineId" to JsonPrimitive(routineId.toString()), "title" to JsonPrimitive(Arb.nonBlankString().next()))

                toolSet.tool(name = "routine_get").call(arguments = requested) shouldBe McpToolCallResult.Success(content = JsonNull)
                toolSet.tool(name = "routine_update").call(arguments = requested) shouldBe McpToolCallResult.Success(content = changedCountJson(0))
                coVerify(exactly = 0) { updateRoutineUseCase(parameter = any()) }
            }
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
    val expected: JsonObject,
)

private fun clockAt(date: LocalDate): Clock {
    val clock = mockk<Clock>()
    every { clock.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

    return clock
}

private fun foundRoutineUseCase(routine: Routine): FindRoutineUseCase {
    val findRoutineUseCase = mockk<FindRoutineUseCase>()
    every { findRoutineUseCase(parameter = routine.id) } returns flowOf(Result.success(routine))

    return findRoutineUseCase
}

private fun arguments(vararg pairs: Pair<String, JsonElement>): JsonObject = JsonObject(mapOf(*pairs))

private fun routineIdArguments(routineId: Uuid): JsonObject = arguments("routineId" to JsonPrimitive(routineId.toString()))

private fun recordAddArguments(
    routineId: Uuid,
    date: String?,
    amount: Int?,
): JsonObject =
    buildJsonObject {
        put("routineId", routineId.toString())
        date?.let { value -> put("date", value) }
        amount?.let { value -> put("amount", value) }
    }

private fun pageJson(itemList: List<JsonObject>): JsonObject = buildJsonObject { put("items", JsonArray(itemList)) }

private fun changedCountJson(count: Int): JsonObject = buildJsonObject { put("changedCount", count) }

private fun progressJson(
    status: String,
    cycleAmount: Long? = null,
    achievementRate: Int? = null,
): JsonObject =
    buildJsonObject {
        put("status", status)
        cycleAmount?.let { amount -> put("cycleAmount", amount) }
        achievementRate?.let { rate -> put("achievementRate", rate) }
    }

private fun RoutineSummary.expectedJson(progress: JsonObject): JsonObject =
    buildJsonObject {
        put("id", routine.id.toString())
        put("title", routine.detail.title)
        put("description", routine.detail.description)
        put("color", routine.detail.color.toColorHex())
        put("startDate", routine.detail.startDate.toString())
        put(
            "cycle",
            when (routine.detail.cycle) {
                RoutineCycle.DAILY -> "FREQ=DAILY"
                RoutineCycle.WEEKDAYS -> "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"
                RoutineCycle.WEEKENDS -> "FREQ=DAILY;BYDAY=SA,SU"
                RoutineCycle.WEEKLY -> "FREQ=WEEKLY;WKST=SU"
                RoutineCycle.MONTHLY -> "FREQ=MONTHLY"
            },
        )
        put("unit", routine.detail.unit)
        put("goalAmount", routine.detail.goalAmount)
        put("amountPerRecord", routine.detail.amountPerRecord)
        put("isFinished", routine.isFinished)
        put("createdAt", routine.createdAt.toString())
        put("updatedAt", routine.updatedAt.toString())
        put("progress", progress)
    }

private fun RoutineRecord.expectedJson(): JsonObject =
    buildJsonObject {
        put("id", id.toString())
        put("routineId", routineId.toString())
        put("date", date.toString())
        put("amount", amount)
        put("createdAt", createdAt.toString())
        put("updatedAt", updatedAt.toString())
    }

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
