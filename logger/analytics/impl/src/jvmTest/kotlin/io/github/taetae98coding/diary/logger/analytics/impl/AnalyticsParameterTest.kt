package io.github.taetae98coding.diary.logger.analytics.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AnalyticsParameterTest :
    FunSpec({
        test("TC-d6278947-afea-410b-9c8f-b52d5fa26ddf 값 25개는 모두 담긴다") {
            val parameters = JsonObject((1..25).associate { index -> "field$index" to JsonPrimitive(textValue()) })

            parameters.toAnalyticsParameterMap().keys.toList() shouldContainExactly (1..25).map { index -> "field$index" }
        }

        test("TC-d6278947-afea-410b-9c8f-b52d5fa26ddf TC-24de6c0f-cb72-49c9-96ab-3ed18e7b2be8 값이 25개를 넘으면 앞의 25개만 담긴다") {
            val parameters = JsonObject((1..26).associate { index -> "field$index" to JsonPrimitive(textValue()) })

            parameters.toAnalyticsParameterMap().keys.toList() shouldContainExactly (1..25).map { index -> "field$index" }
        }

        test("TC-d6278947-afea-410b-9c8f-b52d5fa26ddf 빈 값은 25개에 세지 않는다") {
            val parameters =
                JsonObject(
                    mapOf("empty" to JsonNull) +
                        (1..25).associate { index -> "field$index" to JsonPrimitive(textValue()) },
                )

            parameters.toAnalyticsParameterMap().keys.toList() shouldContainExactly (1..25).map { index -> "field$index" }
        }

        test("TC-9ca25520-098d-42a8-8338-c39c0c9ad24d 40자인 이름은 그대로 남는다") {
            val name = "a".repeat(40)
            val value = textValue()

            JsonObject(mapOf(name to JsonPrimitive(value))).toAnalyticsParameterMap() shouldBe mapOf(name to value)
        }

        test("TC-9ca25520-098d-42a8-8338-c39c0c9ad24d TC-24de6c0f-cb72-49c9-96ab-3ed18e7b2be8 41자인 이름은 앞 40자 이름으로 남는다") {
            val name = "a".repeat(41)
            val value = textValue()

            JsonObject(mapOf(name to JsonPrimitive(value))).toAnalyticsParameterMap() shouldBe mapOf("a".repeat(40) to value)
        }

        test("TC-9ca25520-098d-42a8-8338-c39c0c9ad24d 100자인 글 값은 그대로 남는다") {
            val value = "b".repeat(100)

            JsonObject(mapOf("field" to JsonPrimitive(value))).toAnalyticsParameterMap() shouldBe mapOf("field" to value)
        }

        test("TC-9ca25520-098d-42a8-8338-c39c0c9ad24d TC-24de6c0f-cb72-49c9-96ab-3ed18e7b2be8 101자인 글 값은 앞 100자 값으로 남는다") {
            val value = "b".repeat(101)

            JsonObject(mapOf("field" to JsonPrimitive(value))).toAnalyticsParameterMap() shouldBe mapOf("field" to "b".repeat(100))
        }

        test("TC-69d43d67-847e-4019-8a1a-1efd4eb11a47 빈 값은 담지 않고 내용이 없는 글은 빈 글로 담는다") {
            val text = textValue()
            val parameters =
                JsonObject(
                    mapOf(
                        "empty" to JsonNull,
                        "blank" to JsonPrimitive(""),
                        "text" to JsonPrimitive(text),
                    ),
                )

            parameters.toAnalyticsParameterMap() shouldBe mapOf("blank" to "", "text" to text)
        }

        test("TC-986c0ea8-697c-45cf-8f3d-ed2d68a44392 숫자는 숫자로, 글은 글로, 참·거짓은 글로 남긴다") {
            val parameters =
                JsonObject(
                    mapOf(
                        "integer" to JsonPrimitive(34),
                        "decimal" to JsonPrimitive(1.5),
                        "text" to JsonPrimitive("1617893780"),
                        "true" to JsonPrimitive(true),
                        "false" to JsonPrimitive(false),
                    ),
                )

            parameters.toAnalyticsParameterMap() shouldBe
                mapOf(
                    "integer" to 34L,
                    "decimal" to 1.5,
                    "text" to "1617893780",
                    "true" to "true",
                    "false" to "false",
                )
        }

        test("TC-1d780626-bba1-4134-964f-d905d93f6119 100자 이하로 바뀌는 묶은 값은 한 줄의 글 그대로 남는다") {
            val text = "c".repeat(10)
            val grouped = JsonObject(mapOf("inner" to JsonArray(listOf(JsonPrimitive(text), JsonPrimitive(1)))))

            JsonObject(mapOf("group" to grouped)).toAnalyticsParameterMap() shouldBe
                mapOf("group" to """{"inner":["$text",1]}""")
        }

        test("TC-1d780626-bba1-4134-964f-d905d93f6119 100자를 넘는 묶은 값은 한 줄의 글 앞 100자로 남는다") {
            val grouped = JsonArray(listOf(JsonPrimitive("d".repeat(200))))
            val line = grouped.toString()

            JsonObject(mapOf("group" to grouped)).toAnalyticsParameterMap() shouldBe mapOf("group" to line.take(100))
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun textValue(): String = "value${fixtureMonkey.giveMeOne<Int>()}"
    }
}
