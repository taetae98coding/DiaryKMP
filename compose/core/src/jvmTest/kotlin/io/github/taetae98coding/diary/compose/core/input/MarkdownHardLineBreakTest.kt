package io.github.taetae98coding.diary.compose.core.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MarkdownHardLineBreakTest :
    FunSpec({
        test("엔터 한 번으로 이어지는 줄 끝에 hard line break를 붙인다") {
            markdownOf("FirstLine", "SecondLine").withMarkdownHardLineBreak() shouldBe markdownOf("FirstLine  ", "SecondLine")
        }

        test("다음 줄이 없는 줄에는 붙이지 않는다") {
            "# MarkdownHeading".withMarkdownHardLineBreak() shouldBe "# MarkdownHeading"
        }

        test("빈 줄로 구분된 문단은 바꾸지 않는다") {
            val source = markdownOf("FirstParagraph", "", "SecondParagraph")

            source.withMarkdownHardLineBreak() shouldBe source
        }

        test("빈 문자열은 그대로 반환한다") {
            "".withMarkdownHardLineBreak() shouldBe ""
        }

        test("공백만 있는 줄과 그 앞줄은 바꾸지 않는다") {
            val source = markdownOf("FirstLine", "   ", "SecondLine")

            source.withMarkdownHardLineBreak() shouldBe source
        }

        test("세 줄이 이어지면 마지막 줄을 제외한 줄 끝에 붙인다") {
            markdownOf("FirstLine", "SecondLine", "ThirdLine").withMarkdownHardLineBreak() shouldBe
                markdownOf("FirstLine  ", "SecondLine  ", "ThirdLine")
        }

        test("백틱 코드 블록 내부는 바꾸지 않는다") {
            val source = markdownOf("```", "code1", "code2", "```")

            source.withMarkdownHardLineBreak() shouldBe source
        }

        test("언어가 지정된 코드 블록 내부는 바꾸지 않는다") {
            val source = markdownOf("```kotlin", "code1", "code2", "```")

            source.withMarkdownHardLineBreak() shouldBe source
        }

        test("물결 코드 블록 내부는 바꾸지 않는다") {
            val source = markdownOf("~~~", "code1", "code2", "~~~")

            source.withMarkdownHardLineBreak() shouldBe source
        }

        test("닫히지 않은 코드 블록 이후는 모두 바꾸지 않는다") {
            markdownOf("line", "```", "code1", "code2").withMarkdownHardLineBreak() shouldBe
                markdownOf("line  ", "```", "code1", "code2")
        }

        test("코드 블록이 닫힌 뒤 이어지는 줄은 다시 바꾼다") {
            markdownOf("```", "code", "```", "FirstLine", "SecondLine").withMarkdownHardLineBreak() shouldBe
                markdownOf("```", "code", "```", "FirstLine  ", "SecondLine")
        }

        test("여는 표시보다 긴 표시로도 코드 블록이 닫힌다") {
            markdownOf("```", "code", "`````", "FirstLine", "SecondLine").withMarkdownHardLineBreak() shouldBe
                markdownOf("```", "code", "`````", "FirstLine  ", "SecondLine")
        }

        test("다른 종류의 표시로는 코드 블록이 닫히지 않는다") {
            val source = markdownOf("~~~", "code", "```", "code2", "~~~")

            source.withMarkdownHardLineBreak() shouldBe source
        }
    })

private fun markdownOf(vararg lineList: String): String = lineList.joinToString(separator = "\n")
