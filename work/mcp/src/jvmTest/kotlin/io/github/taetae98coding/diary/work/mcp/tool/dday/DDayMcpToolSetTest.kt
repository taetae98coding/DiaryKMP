package io.github.taetae98coding.diary.work.mcp.tool.dday

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchDDayUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.json.toColorHex
import io.github.taetae98coding.diary.work.mcp.tool.search.SearchMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock

class DDayMcpToolSetTest :
    FunSpec({
        test("TC-e41fa66d-9d70-491e-940d-adb5e945d01e 상세·목록·검색에 오늘로 계산한 값을 담는다") {
            runTest {
                val clock = clockAt(date = LocalDate(2026, 3, 20))
                listOf(
                    Triple(LocalDate(2026, 4, 29), true, countJson(status = "upcoming", remainingDays = 40, years = 0, months = 1)),
                    Triple(LocalDate(2026, 3, 20), true, buildJsonObject { put("status", "today") }),
                    Triple(LocalDate(2025, 1, 15), true, countJson(status = "passed", passedDays = 430, years = 1, months = 2)),
                    Triple(LocalDate(2025, 1, 15), false, countJson(status = "passed", passedDays = 429, years = 1, months = 2)),
                ).forEach { (baseDate, countsBaseDay, expectedCount) ->
                    val dday = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = baseDate, countsBaseDay = countsBaseDay).next())
                    val expected = dday.expectedJson(count = expectedCount)
                    val findDDayUseCase = mockk<FindDDayUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    val pageDDayUseCase = mockk<PageDDayUseCase>()
                    val searchDDayUseCase = mockk<SearchDDayUseCase>()
                    every { findDDayUseCase(parameter = dday.id) } returns flowOf(Result.success(dday))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    every { pageDDayUseCase(parameter = Unit) } returns flowOf(Result.success(PagingData.from(listOf(dday))))
                    every { searchDDayUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(dday))))
                    val toolSet = toolSetOf<DDayMcpToolSet>(findDDayUseCase, getEntityTagUseCase, pageDDayUseCase, clock)
                    val searchToolSet = toolSetOf<SearchMcpToolSet>(searchDDayUseCase, clock)

                    toolSet.tool(name = "dday_get").call(arguments = ddayIdArguments(dday)) shouldBe
                        McpToolCallResult.Success(
                            content =
                                buildJsonObject {
                                    put("dday", expected)
                                    put("tags", JsonArray(emptyList()))
                                },
                        )
                    toolSet.tool(name = "dday_list").call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = pageJson(expected))
                    searchToolSet.tool(name = "search_dday").call(arguments = JsonObject(mapOf("query" to JsonPrimitive(dday.detail.title)))) shouldBe
                        McpToolCallResult.Success(content = pageJson(expected))
                }
            }
        }

        test("TC-cacc0b02-056d-4ed9-87c7-f831d1043236 반복 간격을 기간 표기로 받아 기록하고 정해진 표기로 돌려준다") {
            runTest {
                listOf(
                    Triple(listOf("P100D"), setOf(DateTimeUnit.DayBased(days = 100)), listOf("P100D")),
                    Triple(listOf("P3M"), setOf(DateTimeUnit.MonthBased(months = 3)), listOf("P3M")),
                    Triple(listOf("P1Y"), setOf(DateTimeUnit.MonthBased(months = 12)), listOf("P1Y")),
                    Triple(listOf("P24M"), setOf(DateTimeUnit.MonthBased(months = 24)), listOf("P2Y")),
                    Triple(listOf("P18M"), setOf(DateTimeUnit.MonthBased(months = 18)), listOf("P18M")),
                    Triple(listOf("P1Y6M"), setOf(DateTimeUnit.MonthBased(months = 18)), listOf("P18M")),
                    Triple(listOf("P12M", "P1Y"), setOf(DateTimeUnit.MonthBased(months = 12)), listOf("P1Y")),
                    Triple(emptyList(), emptySet(), emptyList()),
                ).forEach { (requested, recorded, resulted) ->
                    val addedId = Arb.kotlinUuid().next()
                    val parameter = slot<AddDDayUseCase.Parameter>()
                    val addDDayUseCase = mockk<AddDDayUseCase>()
                    coEvery { addDDayUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                    val added = Arb.dday().next().let { dday -> dday.copy(id = addedId, detail = dday.detail.copy(repeats = recorded)) }
                    val findDDayUseCase = mockk<FindDDayUseCase>()
                    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                    every { findDDayUseCase(parameter = addedId) } returns flowOf(Result.success(added))
                    every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    val toolSet = toolSetOf<DDayMcpToolSet>(addDDayUseCase, findDDayUseCase, getEntityTagUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                    toolSet.tool(name = "dday_add").call(arguments = addArguments(title = Arb.nonBlankString().next(), "repeats" to stringArray(requested))) shouldBe
                        McpToolCallResult.Success(content = buildJsonObject { put("id", addedId.toString()) })
                    parameter.captured.input.repeatList
                        .toRepeatSetOrNull() shouldBe recorded

                    val result = toolSet.tool(name = "dday_get").call(arguments = ddayIdArguments(added)).shouldBeInstanceOf<McpToolCallResult.Success>()
                    result.content.jsonObject["dday"]!!.jsonObject["repeats"] shouldBe stringArray(resulted)
                }
            }
        }

        test("TC-e71669ca-85e4-4b4e-aa98-2ccc86bcbb4b 받을 수 없는 반복 간격을 담으면 디데이를 추가하지 않는다") {
            runTest {
                listOf(
                    listOf("P1M10D"),
                    listOf("P0D"),
                    listOf("P100D", "P0M"),
                    listOf("P2W"),
                    listOf("PT24H"),
                    listOf("100"),
                ).forEach { requested ->
                    val addDDayUseCase = mockk<AddDDayUseCase>()
                    val toolSet = toolSetOf<DDayMcpToolSet>(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                    toolSet.tool(name = "dday_add").call(arguments = addArguments(title = Arb.nonBlankString().next(), "repeats" to stringArray(requested))) shouldBe
                        McpToolCallResult.Failure(message = REPEATS_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { addDDayUseCase(parameter = any()) }
                }
            }
        }

        test("TC-c54f1f66-8a6c-46d3-a280-19d9ecd5122e 수정은 담은 정보만 바꾸고 종료일은 지우는 요청으로 지운다") {
            runTest {
                val storedDetail =
                    Arb
                        .ddayDetail(
                            baseDate = LocalDate(2026, 1, 1),
                            repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12)),
                            endDate = LocalDate(2026, 12, 31),
                        ).next()
                        .copy(title = "시험")
                val stored = Arb.dday().next().copy(detail = storedDetail)
                listOf(
                    ("title" to JsonPrimitive("수능")) to storedDetail.copy(title = "수능"),
                    ("clearEndDate" to JsonPrimitive(true)) to storedDetail.copy(endDate = null),
                    ("repeats" to stringArray(listOf("P6M"))) to storedDetail.copy(repeats = setOf(DateTimeUnit.MonthBased(months = 6))),
                    ("repeats" to stringArray(emptyList())) to storedDetail.copy(repeats = emptySet()),
                ).forEach { (requested, expected) ->
                    val changedCount = Arb.int(0..1).next()
                    val parameter = slot<UpdateDDayUseCase.Parameter>()
                    val findDDayUseCase = mockk<FindDDayUseCase>()
                    val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                    every { findDDayUseCase(parameter = stored.id) } returns flowOf(Result.success(stored))
                    coEvery { updateDDayUseCase(parameter = capture(parameter)) } returns Result.success(changedCount)
                    val toolSet = toolSetOf<DDayMcpToolSet>(findDDayUseCase, updateDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                    toolSet.tool(name = "dday_update").call(arguments = JsonObject(mapOf("ddayId" to JsonPrimitive(stored.id.toString()), requested))) shouldBe
                        McpToolCallResult.Success(content = buildJsonObject { put("changedCount", changedCount) })
                    parameter.captured.id shouldBe stored.id
                    parameter.captured.input.recordedDetail() shouldBe expected
                }
            }
        }

        test("TC-da7ab5c0-236c-4d20-af9e-b4740ee1c4f2 종료일이 기준일보다 앞이 되는 요청은 실패하고 바꾸지 않는다") {
            runTest {
                val addDDayUseCase = mockk<AddDDayUseCase>()
                val addToolSet = toolSetOf<DDayMcpToolSet>(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                addToolSet.tool(name = "dday_add").call(
                    arguments = addArguments(title = Arb.nonBlankString().next(), "baseDate" to JsonPrimitive("2026-05-01"), "endDate" to JsonPrimitive("2026-04-30")),
                ) shouldBe McpToolCallResult.Failure(message = END_DATE_FAILURE_MESSAGE)
                coVerify(exactly = 0) { addDDayUseCase(parameter = any()) }

                val stored = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = LocalDate(2026, 1, 1), endDate = LocalDate(2026, 12, 31)).next())
                listOf(
                    mapOf("baseDate" to JsonPrimitive("2027-01-01")),
                    mapOf("endDate" to JsonPrimitive("2025-12-31")),
                    mapOf("baseDate" to JsonPrimitive("2026-06-01"), "endDate" to JsonPrimitive("2026-05-31")),
                ).forEach { requested ->
                    val findDDayUseCase = mockk<FindDDayUseCase>()
                    val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                    every { findDDayUseCase(parameter = stored.id) } returns flowOf(Result.success(stored))
                    val toolSet = toolSetOf<DDayMcpToolSet>(findDDayUseCase, updateDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                    toolSet.tool(name = "dday_update").call(arguments = JsonObject(requested + ("ddayId" to JsonPrimitive(stored.id.toString())))) shouldBe
                        McpToolCallResult.Failure(message = END_DATE_FAILURE_MESSAGE)
                    coVerify(exactly = 0) { updateDDayUseCase(parameter = any()) }
                }
            }
        }

        test("TC-017997f9-0614-4d9e-8cb8-babc5b30714f 제목만 담아 추가하면 DDayAdd가 처음 채우는 값으로 정한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val parameter = slot<AddDDayUseCase.Parameter>()
                val addDDayUseCase = mockk<AddDDayUseCase>()
                coEvery { addDDayUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val toolSet = toolSetOf<DDayMcpToolSet>(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                toolSet.tool(name = "dday_add").call(arguments = addArguments(title = title)).shouldBeInstanceOf<McpToolCallResult.Success>()

                parameter.captured shouldBe
                    AddDDayUseCase.Parameter(
                        input =
                            DDayInput(
                                title = title,
                                description = "",
                                color = parameter.captured.input.color,
                                baseDate = LocalDate(2026, 3, 20),
                                countsBaseDay = true,
                                repeatList = emptyList(),
                                endDate = null,
                                overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                            ),
                        tagIdSet = emptySet(),
                    )
            }
        }

        test("추가는 담은 정보와 태그를 그대로 넘기고, 달력에 없는 날짜 처리가 고를 수 없는 값이면 실패한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val tagId = Arb.kotlinUuid().next()
                val parameter = slot<AddDDayUseCase.Parameter>()
                val addDDayUseCase = mockk<AddDDayUseCase>()
                coEvery { addDDayUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val toolSet = toolSetOf<DDayMcpToolSet>(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                toolSet
                    .tool(name = "dday_add")
                    .call(
                        arguments =
                            addArguments(
                                title = title,
                                "color" to JsonPrimitive("#4285F4"),
                                "baseDate" to JsonPrimitive("2026-01-31"),
                                "countsBaseDay" to JsonPrimitive(false),
                                "endDate" to JsonPrimitive("2026-12-31"),
                                "overflowPolicy" to JsonPrimitive("next_month_first_day"),
                                "tagIds" to stringArray(listOf(tagId.toString())),
                            ),
                    ).shouldBeInstanceOf<McpToolCallResult.Success>()
                toolSet.tool(name = "dday_add").call(arguments = addArguments(title = title, "overflowPolicy" to JsonPrimitive("clamp"))) shouldBe
                    McpToolCallResult.Failure(message = "'overflowPolicy' must be one of clamp_to_month_end, skip, next_month_first_day.")

                coVerify(exactly = 1) { addDDayUseCase(parameter = any()) }
                parameter.captured.tagIdSet shouldBe setOf(tagId)
                parameter.captured.input.recordedDetail() shouldBe
                    DDayDetail(
                        title = title,
                        description = "",
                        color = 0xFF4285F4.toInt().toLong(),
                        baseDate = LocalDate(2026, 1, 31),
                        countsBaseDay = false,
                        repeats = emptySet(),
                        endDate = LocalDate(2026, 12, 31),
                        overflowPolicy = DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY,
                    )
            }
        }

        test("없는 디데이는 상세가 null이고, 수정하면 앱 기능을 실행하지 않고 바뀐 수 0을 돌려준다") {
            runTest {
                val ddayId = Arb.kotlinUuid().next()
                val findDDayUseCase = mockk<FindDDayUseCase>()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                every { findDDayUseCase(parameter = ddayId) } returns flowOf(Result.success(null))
                val toolSet = toolSetOf<DDayMcpToolSet>(findDDayUseCase, updateDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))
                val arguments = JsonObject(mapOf("ddayId" to JsonPrimitive(ddayId.toString()), "title" to JsonPrimitive(Arb.nonBlankString().next())))

                toolSet.tool(name = "dday_get").call(arguments = arguments) shouldBe McpToolCallResult.Success(content = JsonNull)
                toolSet.tool(name = "dday_update").call(arguments = arguments) shouldBe McpToolCallResult.Success(content = buildJsonObject { put("changedCount", 0) })
                coVerify(exactly = 0) { updateDDayUseCase(parameter = any()) }
            }
        }

        test("태그 연결과 해제는 디데이 종류로 앱 기능에 넘긴다") {
            runTest {
                val ddayId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val toolSet = toolSetOf<DDayMcpToolSet>(addEntityTagUseCase, removeEntityTagUseCase, clockAt(date = LocalDate(2026, 3, 20)))
                val arguments = JsonObject(mapOf("ddayId" to JsonPrimitive(ddayId.toString()), "tagId" to JsonPrimitive(tagId.toString())))
                val done = McpToolCallResult.Success(content = buildJsonObject { put("done", true) })

                toolSet.tool(name = "dday_tag_add").call(arguments = arguments) shouldBe done
                toolSet.tool(name = "dday_tag_remove").call(arguments = arguments) shouldBe done

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)) }
            }
        }

        test("디데이 검색은 검색어와 정렬을 넘긴다") {
            runTest {
                val query = Arb.nonBlankString().next()
                val searchDDayUseCase = mockk<SearchDDayUseCase>()
                every { searchDDayUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
                val tool = toolSetOf<SearchMcpToolSet>(searchDDayUseCase, clockAt(date = LocalDate(2026, 3, 20))).tool(name = "search_dday")

                tool.call(arguments = JsonObject(mapOf("query" to JsonPrimitive(query), "sort" to JsonPrimitive("recently_updated")))).shouldBeInstanceOf<McpToolCallResult.Success>()
                verify(exactly = 1) { searchDDayUseCase(parameter = SearchDDayUseCase.Parameter(query = query, sort = ListSort.RECENTLY_UPDATED)) }
            }
        }
    })

private const val REPEATS_FAILURE_MESSAGE = "Each 'repeats' item must be an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D, P3M or P1Y."
private const val END_DATE_FAILURE_MESSAGE = "'endDate' must not be before 'baseDate'."

private fun clockAt(date: LocalDate): Clock {
    val clock = mockk<Clock>()
    every { clock.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

    return clock
}

private fun ddayIdArguments(dday: DDay): JsonObject = JsonObject(mapOf("ddayId" to JsonPrimitive(dday.id.toString())))

private fun addArguments(
    title: String,
    vararg pairs: Pair<String, JsonElement>,
): JsonObject = JsonObject(mapOf("title" to JsonPrimitive(title)) + pairs)

private fun stringArray(valueList: List<String>): JsonArray = JsonArray(valueList.map(::JsonPrimitive))

private fun pageJson(item: JsonObject): JsonObject = buildJsonObject { put("items", JsonArray(listOf(item))) }

private fun countJson(
    status: String,
    remainingDays: Int? = null,
    passedDays: Int? = null,
    years: Int,
    months: Int,
): JsonObject =
    buildJsonObject {
        put("status", status)
        remainingDays?.let { days -> put("remainingDays", days) }
        passedDays?.let { days -> put("passedDays", days) }
        put("years", years)
        put("months", months)
    }

private fun DDay.expectedJson(count: JsonObject): JsonObject =
    buildJsonObject {
        put("id", id.toString())
        put("title", detail.title)
        put("description", detail.description)
        put("color", detail.color.toColorHex())
        put("baseDate", detail.baseDate.toString())
        put("countsBaseDay", detail.countsBaseDay)
        put("repeats", JsonArray(emptyList()))
        put("endDate", detail.endDate?.toString())
        put(
            "overflowPolicy",
            when (detail.overflowPolicy) {
                DDayOverflowPolicy.CLAMP_TO_MONTH_END -> "clamp_to_month_end"
                DDayOverflowPolicy.SKIP -> "skip"
                DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> "next_month_first_day"
            },
        )
        put("createdAt", createdAt.toString())
        put("updatedAt", updatedAt.toString())
        put("count", count)
    }

private fun DDayInput.recordedDetail(): DDayDetail =
    DDayDetail(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = checkNotNull(repeatList.toRepeatSetOrNull()),
        endDate = endDate,
        overflowPolicy = overflowPolicy,
    )
