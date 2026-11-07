package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.shuffle
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class McpListSortTest :
    FunSpec({
        test("정렬을 받지 않으면 그 목록의 처음 정렬을 쓴다") {
            val sortList = sortList()

            McpToolArguments(JsonObject(emptyMap())).sort(sortList) shouldBe sortList.first()
        }

        test("목록이 제공하는 정렬을 이름으로 고른다") {
            val sortList = sortList()

            sortParameter(sortList).allowedValueList.zip(sortList).forEach { (argument, sort) ->
                sortArguments(argument).sort(sortList) shouldBe sort
            }
        }

        test("목록이 제공하지 않는 정렬은 고를 수 있는 이름을 담아 실패한다") {
            val sortList = listOf(ListSort.DEFAULT, ListSort.TITLE)

            listOf("name", "recently_updated", "unknown", "").forEach { argument ->
                shouldThrowExactly<McpToolArgumentException> { sortArguments(argument).sort(sortList) }.message shouldBe
                    "'sort' must be one of default, title."
            }
        }

        test("정렬 매개변수는 목록이 제공하는 정렬만 고를 수 있게 알린다") {
            val parameter = sortParameter(listOf(ListSort.NAME, ListSort.RECENTLY_UPDATED))

            parameter.name shouldBe "sort"
            parameter.allowedValueList shouldBe listOf("name", "recently_updated")
            parameter.isRequired shouldBe false
        }
    })

private fun sortList(): List<ListSort> = Arb.shuffle(ListSort.entries).next().take(Arb.int(1..ListSort.entries.size).next())

private fun sortArguments(argument: String): McpToolArguments = McpToolArguments(JsonObject(mapOf("sort" to JsonPrimitive(argument))))
