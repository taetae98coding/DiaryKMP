package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MemoDraftProgressMapperTest :
    FunSpec({
        test("끊긴 글 값은 끊긴 곳까지 읽는다") {
            """{"title":"주간 회""".toMemoDraftProgress() shouldBe MemoDraftProgress(title = "주간 회", description = "")
        }

        test("닫힌 글 값과 그 뒤의 끊긴 글 값을 함께 읽는다") {
            """{"title":"주간 회고", "description":"## 이번""".toMemoDraftProgress() shouldBe
                MemoDraftProgress(title = "주간 회고", description = "## 이번")
        }

        test("이스케이프를 풀어 읽는다") {
            """{"description":"첫 줄\n둘째 \"줄\" \\ \u00e9"}""".toMemoDraftProgress() shouldBe
                MemoDraftProgress(title = "", description = listOf("첫 줄", "둘째 ${QUOTE}줄$QUOTE $BACKSLASH é").joinToString(separator = "\n"))
        }

        test("끊긴 이스케이프는 반쯤 읽은 글자를 남기지 않는다") {
            listOf(
                """{"title":"앞\""",
                """{"title":"앞\u00""",
            ).forEach { text ->
                text.toMemoDraftProgress() shouldBe MemoDraftProgress(title = "앞", description = "")
            }
        }

        test("키가 끊겼거나 값이 아직 없으면 그 키는 비어 있다") {
            listOf(
                "",
                "{",
                """{"ti""",
                """{"title"""",
                """{"title":""",
            ).forEach { text ->
                text.toMemoDraftProgress() shouldBe MemoDraftProgress.EMPTY
            }
        }

        test("글이 아닌 값과 안쪽 객체의 같은 이름 키는 건너뛴다") {
            val text = """{"period":{"title":"안쪽","x":[1,{"a":"}"}]},"count":3,"title":"바깥","description":"설명"}"""

            text.toMemoDraftProgress() shouldBe MemoDraftProgress(title = "바깥", description = "설명")
        }

        test("최상위가 객체가 아니면 비어 있다") {
            listOf("생성된 문장", "[\"title\"]").forEach { text ->
                text.toMemoDraftProgress() shouldBe MemoDraftProgress.EMPTY
            }
        }
    })

private const val QUOTE = '"'
private const val BACKSLASH = '\\'
