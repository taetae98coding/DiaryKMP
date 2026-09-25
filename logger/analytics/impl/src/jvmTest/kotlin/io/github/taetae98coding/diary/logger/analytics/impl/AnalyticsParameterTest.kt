package io.github.taetae98coding.diary.logger.analytics.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AnalyticsParameterTest :
    FunSpec({
        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-012 필드가 25개를 넘으면 앞의 25개 필드만 남는다") {
            val parameters = JsonObject((1..26).associate { index -> "field$index" to JsonPrimitive("value$index") })

            parameters.toAnalyticsParameterMap().keys.toList() shouldContainExactly (1..25).map { index -> "field$index" }
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-012 41자인 필드 이름은 앞 40자 이름으로 남는다") {
            val name = "a".repeat(41)

            JsonObject(mapOf(name to JsonPrimitive("value"))).toAnalyticsParameterMap() shouldBe mapOf("a".repeat(40) to "value")
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-012 101자인 문자열 값은 앞 100자 값으로 남는다") {
            val value = "b".repeat(101)

            JsonObject(mapOf("field" to JsonPrimitive(value))).toAnalyticsParameterMap() shouldBe mapOf("field" to "b".repeat(100))
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-004 숫자는 숫자로, 문자열은 문자열로 바꾼다") {
            val parameters =
                JsonObject(
                    mapOf(
                        "integer" to JsonPrimitive(34),
                        "decimal" to JsonPrimitive(1.5),
                        "text" to JsonPrimitive("1617893780"),
                    ),
                )

            parameters.toAnalyticsParameterMap() shouldBe
                mapOf(
                    "integer" to 34L,
                    "decimal" to 1.5,
                    "text" to "1617893780",
                )
        }
    })
