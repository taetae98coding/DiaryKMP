package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.memoDateTimeAllDay
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class McpDateJsonTest :
    FunSpec({
        test("하루 종일 기간을 내보낸 형식 그대로 다시 읽는다") {
            val dateTime = Arb.memoDateTimeAllDay().next()

            dateTimeArguments(dateTime.toJson()).memoDateTimeOrNull() shouldBe dateTime
        }

        test("시각 기간을 내보낸 형식 그대로 다시 읽는다") {
            val (start, end) = listOf(Arb.kotlinxLocalDateTime().next(), Arb.kotlinxLocalDateTime().next()).sorted()
            val dateTime = MemoDateTime.DateTime(start = start, endInclusive = end)

            dateTimeArguments(dateTime.toJson()).memoDateTimeOrNull() shouldBe dateTime
        }

        test("기간을 받지 않으면 비어 있는 것으로 읽는다") {
            McpToolArguments(JsonObject(emptyMap())).memoDateTimeOrNull() shouldBe null
        }

        test("하루 종일 여부가 없으면 실패한다") {
            val range = Arb.localDateRange().next()
            val arguments =
                dateTimeArguments(
                    buildJsonObject {
                        put("start", range.start.toString())
                        put("endInclusive", range.endInclusive.toString())
                    },
                )

            shouldThrowExactly<McpToolArgumentException> { arguments.memoDateTimeOrNull() }.message shouldBe "'dateTime.allDay' is required."
        }

        test("끝이 시작보다 앞서면 실패한다") {
            val date = Arb.kotlinxLocalDate().next()
            val time = Arb.kotlinxLocalDateTime().next().time
            val previousDate = date.minus(1, DateTimeUnit.DAY)
            val cases =
                listOf(
                    buildJsonObject {
                        put("allDay", true)
                        put("start", date.toString())
                        put("endInclusive", previousDate.toString())
                    },
                    buildJsonObject {
                        put("allDay", false)
                        put("start", LocalDateTime(date, time).toString())
                        put("endInclusive", LocalDateTime(previousDate, time).toString())
                    },
                )

            cases.forEach { jsonObject ->
                shouldThrowExactly<McpToolArgumentException> { dateTimeArguments(jsonObject).memoDateTimeOrNull() }.message shouldBe
                    "'dateTime.endInclusive' must not be before 'dateTime.start'."
            }
        }

        test("시각 기간의 시작이나 끝이 없으면 실패한다") {
            val time = Arb.kotlinxLocalDateTime().next().toString()

            shouldThrowExactly<McpToolArgumentException> {
                dateTimeArguments(
                    buildJsonObject {
                        put("allDay", false)
                        put("endInclusive", time)
                    },
                ).memoDateTimeOrNull()
            }.message shouldBe "'dateTime.start' is required."
            shouldThrowExactly<McpToolArgumentException> {
                dateTimeArguments(
                    buildJsonObject {
                        put("allDay", false)
                        put("start", time)
                    },
                ).memoDateTimeOrNull()
            }.message shouldBe "'dateTime.endInclusive' is required."
        }

        test("목록 기간의 끝이 시작보다 앞서면 실패하고 아니면 그대로 읽는다") {
            val range = Arb.localDateRange().next()

            McpToolArguments(
                JsonObject(mapOf(DATE_RANGE_START to JsonPrimitive(range.start.toString()), DATE_RANGE_END to JsonPrimitive(range.endInclusive.toString()))),
            ).dateRange() shouldBe range
            shouldThrowExactly<McpToolArgumentException> {
                McpToolArguments(
                    JsonObject(
                        mapOf(
                            DATE_RANGE_START to JsonPrimitive(range.start.toString()),
                            DATE_RANGE_END to JsonPrimitive(range.start.minus(1, DateTimeUnit.DAY).toString()),
                        ),
                    ),
                ).dateRange()
            }.message shouldBe "'endDate' must not be before 'startDate'."
        }
    })

private fun dateTimeArguments(jsonObject: JsonObject): McpToolArguments = McpToolArguments(JsonObject(mapOf("dateTime" to jsonObject)))
