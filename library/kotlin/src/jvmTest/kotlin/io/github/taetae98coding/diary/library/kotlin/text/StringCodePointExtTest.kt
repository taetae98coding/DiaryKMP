package io.github.taetae98coding.diary.library.kotlin.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringCodePointExtTest :
    FunSpec({
        test("글자 수는 UTF-16 단위가 아니라 유니코드 문자 단위로 센다") {
            listOf(
                "" to 0,
                "ml" to 2,
                "가나다" to 3,
                "💧" to 1,
                "😀".repeat(255) to 255,
                "👨‍👩‍👧" to 5,
            ).forEach { (text, expected) ->
                text.codePointLength() shouldBe expected
            }
        }

        test("자바의 codePointCount와 같은 값을 센다") {
            listOf("", "a가💧", "👨‍👩‍👧", "a\uD83D", "\uDCA7a", "\uD83D💧").forEach { text ->
                text.codePointLength() shouldBe text.codePointCount(0, text.length)
            }
        }

        test("짝이 없는 서로게이트는 한 글자로 센다") {
            "\uD83D".codePointLength() shouldBe 1
            "a\uD83D".codePointLength() shouldBe 2
            "\uDCA7a".codePointLength() shouldBe 2
        }
    })
