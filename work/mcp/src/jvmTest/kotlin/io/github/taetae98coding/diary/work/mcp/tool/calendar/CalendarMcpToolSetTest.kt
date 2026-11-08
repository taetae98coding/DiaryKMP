package io.github.taetae98coding.diary.work.mcp.tool.calendar

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.json.toColorHex
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CalendarMcpToolSetTest :
    FunSpec({
        test("TC-1ae0dfa7-c26c-4917-b610-cd65398d40ac 캘린더 디데이 결과에 문구의 종류와 숫자를 날짜순으로 담는다") {
            runTest {
                val dday = Arb.dday().next()
                val rowList =
                    listOf(
                        Triple(LocalDate(2026, 1, 1), CalendarDDayLabel.BaseDay, buildJsonObject { put("kind", "base_day") }),
                        Triple(LocalDate(2026, 4, 10), CalendarDDayLabel.Days(days = 100), labelJson(kind = "days", count = 100)),
                        Triple(LocalDate(2026, 7, 19), CalendarDDayLabel.Days(days = 200), labelJson(kind = "days", count = 200)),
                        Triple(LocalDate(2026, 10, 27), CalendarDDayLabel.Days(days = 300), labelJson(kind = "days", count = 300)),
                        Triple(LocalDate(2027, 1, 1), CalendarDDayLabel.Years(years = 1), labelJson(kind = "years", count = 1)),
                    )
                val calendarDDayList =
                    rowList.map { (date, label, _) ->
                        CalendarDDay(ddayId = dday.id, title = dday.detail.title, color = dday.detail.color, date = date, label = label)
                    }
                val getCalendarDDayUseCase = mockk<GetCalendarDDayUseCase>()
                every { getCalendarDDayUseCase(parameter = any()) } returns flowOf(Result.success(calendarDDayList.shuffled()))
                val tool = toolSetOf<CalendarMcpToolSet>(getCalendarDDayUseCase).tool(name = "calendar_dday_list")

                val result = tool.call(arguments = JsonObject(mapOf("startDate" to JsonPrimitive("2026-01-01"), "endDate" to JsonPrimitive("2027-01-01"))))

                result shouldBe
                    McpToolCallResult.Success(
                        content =
                            JsonArray(
                                rowList.map { (date, _, label) ->
                                    buildJsonObject {
                                        put("ddayId", dday.id.toString())
                                        put("title", dday.detail.title)
                                        put("color", dday.detail.color.toColorHex())
                                        put("date", date.toString())
                                        put("label", label)
                                    }
                                },
                            ),
                    )
                verify(exactly = 1) { getCalendarDDayUseCase(parameter = LocalDate(2026, 1, 1)..LocalDate(2027, 1, 1)) }
            }
        }
    })

private fun labelJson(
    kind: String,
    count: Int,
): JsonObject =
    buildJsonObject {
        put("kind", kind)
        put("count", count)
    }
