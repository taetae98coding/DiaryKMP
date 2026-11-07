package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class McpToolCallResultTest :
    FunSpec({
        test("기능이 돌려준 내용을 성공으로 전달하고 받은 값을 넘긴다") {
            val content = JsonPrimitive(Arb.string().next())
            val value = Arb.string().next()
            var received: String? = null
            val tool =
                tool { arguments ->
                    received = arguments.string("value")
                    content
                }

            tool.call(arguments = JsonObject(mapOf("value" to JsonPrimitive(value)))) shouldBe McpToolCallResult.Success(content = content)
            received shouldBe value
        }

        test("기능이 실패하면 예외 이름과 내용을 담은 실패로 바꾼다") {
            val message = Arb.alphanumericString(prefix = "failure").next()

            tool { throw IllegalStateException(message) }.call(arguments = JsonObject(emptyMap())) shouldBe
                McpToolCallResult.Failure(message = "IllegalStateException: $message")
        }

        test("내용이 없는 실패는 예외 이름만 담는다") {
            tool { throw IllegalStateException() }.call(arguments = JsonObject(emptyMap())) shouldBe
                McpToolCallResult.Failure(message = "IllegalStateException")
        }

        test("받은 값이 잘못되어 실패하면 안내 문장만 담는다") {
            tool { arguments -> JsonPrimitive(arguments.string("title")) }.call(arguments = JsonObject(emptyMap())) shouldBe
                McpToolCallResult.Failure(message = "'title' is required.")
        }

        test("취소는 실패로 바꾸지 않고 그대로 전달한다") {
            val cancellation = CancellationException(Arb.string().next())

            shouldThrowExactly<CancellationException> {
                tool { throw cancellation }.call(arguments = JsonObject(emptyMap()))
            } shouldBeSameInstanceAs cancellation
        }
    })

private fun tool(execute: suspend (McpToolArguments) -> kotlinx.serialization.json.JsonElement): McpTool =
    McpTool(
        name = Arb.alphanumericString(prefix = "tool").next(),
        description = Arb.string().next(),
        kind = McpToolKind.READ,
        execute = execute,
    )
