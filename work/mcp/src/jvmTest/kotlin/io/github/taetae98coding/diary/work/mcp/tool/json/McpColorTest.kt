package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.random.Random

class McpColorTest :
    FunSpec({
        test("색을 16진수 표기로 바꾸고 다시 읽으면 불투명한 같은 색이 된다") {
            val rgb = Arb.int(0..0xFFFFFF).next()
            val color = 0xFF000000L or rgb.toLong()

            val hex = color.toColorHex()

            hex shouldMatch Regex("#[0-9A-F]{6}")
            colorArguments(hex).colorOrNull() shouldBe color.toInt().toLong()
        }

        test("투명도가 있는 색도 RGB만 16진수로 바꾼다") {
            val color = Arb.long().next()

            color.toColorHex() shouldBe "#" + (color.toInt() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
        }

        test("# 없이 받은 소문자 16진수 색도 읽는다") {
            colorArguments("4285f4").colorOrNull() shouldBe 0xFF4285F4.toInt().toLong()
        }

        test("색을 받지 않으면 비어 있는 것으로 읽고 무작위 색은 불투명하다") {
            val arguments = McpToolArguments(JsonObject(emptyMap()))

            arguments.colorOrNull() shouldBe null
            val seed = Arb.int().next()
            val random = arguments.colorOrRandom(random = Random(seed))
            (random.toInt() ushr 24) shouldBe 0xFF
            arguments.colorOrRandom(random = Random(seed)) shouldBe random
        }

        test("16진수 색이 아니면 실패한다") {
            listOf("", "#", "#12345", "#1234567", "#GGGGGG", "red", "+12345", "#-12345").forEach { text ->
                shouldThrowExactly<McpToolArgumentException> { colorArguments(text).colorOrNull() }.message shouldBe
                    "'color' must be a hex color like #4285F4."
            }
        }
    })

private fun colorArguments(text: String): McpToolArguments = McpToolArguments(JsonObject(mapOf("color" to JsonPrimitive(text))))
