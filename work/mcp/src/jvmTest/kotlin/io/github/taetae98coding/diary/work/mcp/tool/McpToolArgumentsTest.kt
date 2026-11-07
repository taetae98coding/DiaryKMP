package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class McpToolArgumentsTest :
    FunSpec({
        test("받은 값을 형식에 맞게 읽는다") {
            val text = Arb.string().next()
            val number = Arb.int().next()
            val flag = Arb.boolean().next()
            val id = Arb.kotlinUuid().next()
            val idList = listOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
            val date = Arb.kotlinxLocalDate().next()
            val dateTime = Arb.kotlinxLocalDateTime().next()
            val arguments =
                McpToolArguments(
                    JsonObject(
                        mapOf(
                            "text" to JsonPrimitive(text),
                            "number" to JsonPrimitive(number),
                            "flag" to JsonPrimitive(flag),
                            "id" to JsonPrimitive(id.toString()),
                            "ids" to JsonArray(idList.map { JsonPrimitive(it.toString()) }),
                            "date" to JsonPrimitive(date.toString()),
                            "dateTime" to JsonPrimitive(dateTime.toString()),
                            "object" to buildJsonObject { },
                        ),
                    ),
                )

            arguments.string("text") shouldBe text
            arguments.intOrNull("number") shouldBe number
            arguments.boolean("flag") shouldBe flag
            arguments.uuid("id") shouldBe id
            arguments.uuidList("ids") shouldBe idList
            arguments.uuidSet("ids") shouldBe idList.toSet()
            arguments.localDate("date") shouldBe date
            arguments.localDateTimeOrNull("dateTime") shouldBe dateTime
            arguments.objectOrNull("object") shouldBe JsonObject(emptyMap())
            arguments.has("text") shouldBe true
        }

        test("없거나 null인 값은 비어 있는 것으로 읽는다") {
            val arguments = McpToolArguments(JsonObject(mapOf("nullValue" to JsonNull)))

            listOf("missing", "nullValue").forEach { name ->
                arguments.has(name) shouldBe false
                arguments.stringOrNull(name) shouldBe null
                arguments.intOrNull(name) shouldBe null
                arguments.booleanOrNull(name) shouldBe null
                arguments.uuidOrNull(name) shouldBe null
                arguments.localDateOrNull(name) shouldBe null
                arguments.objectOrNull(name) shouldBe null
                arguments.element(name) shouldBe null
                arguments.stringList(name).shouldBeEmpty()
            }
        }

        test("꼭 필요한 값이 없으면 이름을 담아 실패한다") {
            val name = Arb.alphanumericString(prefix = "argument").next()
            val arguments = McpToolArguments(JsonObject(emptyMap()))

            listOf<() -> Unit>(
                { arguments.string(name) },
                { arguments.boolean(name) },
                { arguments.uuid(name) },
                { arguments.localDate(name) },
            ).forEach { read ->
                shouldThrowExactly<McpToolArgumentException> { read() }.message shouldBe "'$name' is required."
            }
        }

        test("형식에 맞지 않는 값은 기대한 형식을 담아 실패한다") {
            val arguments =
                McpToolArguments(
                    JsonObject(
                        mapOf(
                            "number" to JsonPrimitive("not a number"),
                            "text" to JsonPrimitive(Arb.int().next()),
                            "flag" to JsonPrimitive("yes"),
                            "id" to JsonPrimitive("not an id"),
                            "ids" to JsonArray(listOf(JsonPrimitive(1))),
                            "list" to JsonPrimitive("not a list"),
                            "date" to JsonPrimitive("2026-13-40"),
                            "dateTime" to JsonPrimitive("2026-01-01"),
                            "object" to JsonPrimitive("not an object"),
                        ),
                    ),
                )

            shouldThrowExactly<McpToolArgumentException> { arguments.intOrNull("number") }.message shouldBe "'number' must be an integer."
            shouldThrowExactly<McpToolArgumentException> { arguments.stringOrNull("text") }.message shouldBe "'text' must be a string."
            shouldThrowExactly<McpToolArgumentException> { arguments.booleanOrNull("flag") }.message shouldBe "'flag' must be a boolean."
            shouldThrowExactly<McpToolArgumentException> { arguments.uuidOrNull("id") }.message shouldBe "'id' must be an id."
            shouldThrowExactly<McpToolArgumentException> { arguments.stringList("ids") }.message shouldBe "'ids' must be a list of strings."
            shouldThrowExactly<McpToolArgumentException> { arguments.stringList("list") }.message shouldBe "'list' must be a list of strings."
            shouldThrowExactly<McpToolArgumentException> { arguments.localDateOrNull("date") }.message shouldBe "'date' must be a date like 2026-01-31."
            shouldThrowExactly<McpToolArgumentException> { arguments.localDateTimeOrNull("dateTime") }.message shouldBe
                "'dateTime' must be a date time like 2026-01-31T09:30."
            shouldThrowExactly<McpToolArgumentException> { arguments.objectOrNull("object") }.message shouldBe "'object' must be an object."
        }
    })
