package io.github.taetae98coding.diary.compose.core.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GraphemeTest :
    FunSpec({
        test("사용자에게 하나로 보이는 문자를 통째로 남긴다") {
            listOf(
                "🏃" to "🏃",
                "🏃‍♂️" to "🏃‍♂️",
                "👍🏽" to "👍🏽",
                "🇰🇷" to "🇰🇷",
                "🏴󠁧󠁢󠁳󠁣󠁴󠁿" to "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
                "1️⃣" to "1️⃣",
                "❤️" to "❤️",
                "가" to "가",
                "a" to "a",
            ).forEach { (input, expected) ->
                input.takeLastGrapheme().toString() shouldBe expected
            }
        }

        test("마지막 문자만 남기고 앞 문자는 버린다") {
            listOf(
                "🏃🏊" to "🏊",
                "🏃‍♂️🏊" to "🏊",
                "🏊🏃‍♂️" to "🏃‍♂️",
                "🇰🇷🇯🇵" to "🇯🇵",
                "👍🏽👍🏻" to "👍🏻",
                "🏃1️⃣" to "1️⃣",
                "abc" to "c",
                "가나다" to "다",
                "🏃a" to "a",
                "a🏃" to "🏃",
            ).forEach { (input, expected) ->
                input.takeLastGrapheme().toString() shouldBe expected
            }
        }

        test("빈 문자열은 빈 문자열을 남긴다") {
            "".takeLastGrapheme().toString() shouldBe ""
        }
    })
