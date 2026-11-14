package io.github.taetae98coding.diary.feature.web.ui.detail

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebDetailShareContentTest :
    FunSpec({
        test("TC-54f52242-03ce-4491-b3ad-73d33c58f730 입력한 제목과 URL만 두 줄로 보낸다") {
            val saved = Arb.webDetail().next()
            val input = Arb.webDetail().next().copy(title = "제목-${Arb.nonBlankString().next()}", url = "https://${Arb.nonBlankString().next()}")

            val content = webShareContentOf(input = input, saved = saved)

            content shouldBe ShareContent.Text(text = "${input.title.trim()}\n${input.url.trim()}", title = input.title.trim())
        }

        test("TC-cd3f36c4-bf03-4fdb-bd90-797384268c46 제목이나 URL 입력이 비면 저장된 값으로 보낸다") {
            val saved = Arb.webDetail().next().copy(title = "저장-${Arb.nonBlankString().next()}", url = "https://saved.example.com")
            val typedTitle = "입력-${Arb.nonBlankString().next()}"
            val typedUrl = "https://typed.example.com"

            listOf(
                Triple(saved.copy(title = "", url = typedUrl), saved.title, typedUrl),
                Triple(saved.copy(title = "   ", url = typedUrl), saved.title, typedUrl),
                Triple(saved.copy(title = typedTitle, url = ""), typedTitle, saved.url),
                Triple(saved.copy(title = typedTitle, url = "   "), typedTitle, saved.url),
            ).forEach { (input, title, url) ->
                webShareContentOf(input = input, saved = saved) shouldBe ShareContent.Text(text = "${title.trim()}\n$url", title = title.trim())
            }
        }
    })
