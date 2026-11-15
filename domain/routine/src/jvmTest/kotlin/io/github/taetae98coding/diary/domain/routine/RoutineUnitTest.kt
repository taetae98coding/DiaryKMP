package io.github.taetae98coding.diary.domain.routine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RoutineUnitTest :
    FunSpec({
        test("TC-e2271ac7-d2fb-49ad-929e-91ab22b83107 단위는 앞뒤 공백과 줄바꿈이 없는 10자 이하의 글자만 성립한다") {
            listOf(
                "회" to true,
                "ml" to true,
                "L" to true,
                "ML" to true,
                "분 걷기" to true,
                "" to true,
                "가" to true,
                "kilometers" to true,
                "가나다라마바사아자차" to true,
                "kilometerss" to false,
                "가나다라마바사아자차카" to false,
                " ml" to false,
                "ml " to false,
                " " to false,
                "밀리\n리터" to false,
            ).forEach { (unit, expected) ->
                unit.isValidRoutineUnit() shouldBe expected
            }
        }

        test("단위의 글자 수는 UTF-16 단위가 아니라 유니코드 문자 단위로 센다") {
            val emoji = "💧"

            "".routineUnitLength() shouldBe 0
            "ml".routineUnitLength() shouldBe 2
            "가나다".routineUnitLength() shouldBe 3
            emoji.routineUnitLength() shouldBe 1
            emoji.repeat(10).routineUnitLength() shouldBe 10
            emoji.repeat(10).isValidRoutineUnit() shouldBe true
            emoji.repeat(11).isValidRoutineUnit() shouldBe false
        }

        test("짝이 맞지 않는 서러게이트는 한 글자로 센다") {
            "\uD83D".routineUnitLength() shouldBe 1
            "\uDCA7a".routineUnitLength() shouldBe 2
        }

        test("줄을 바꾸는 글자는 종류와 관계없이 단위에 담을 수 없다") {
            listOf('\n', '\r', '\u000B', '\u000C', '\u0085', ' ', ' ').forEach { lineBreak ->
                lineBreak.isRoutineUnitLineBreak() shouldBe true
                "a${lineBreak}b".isValidRoutineUnit() shouldBe false
            }
            ' '.isRoutineUnitLineBreak() shouldBe false
            '\t'.isRoutineUnitLineBreak() shouldBe false
        }
    })
