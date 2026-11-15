package io.github.taetae98coding.diary.compose.core.share

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ShareTextTest :
    FunSpec({
        test("TC-6e01573a-229f-4195-9e6b-8a0cc4a262ea 값을 정한 순서대로 한 줄씩 잇는다") {
            val title = Arb.alphanumericString(prefix = "Title").next()
            val address = Arb.alphanumericString(prefix = "Address").next()
            val link = "https://${Arb.alphanumericString(prefix = "host").next()}.com"

            shareTextOf(title, link) shouldBe "$title\n$link"
            shareTextOf(title, address, link) shouldBe "$title\n$address\n$link"
        }

        test("TC-683ddd63-7cf5-4196-b072-d3de19254e71 빈 값과 공백만 있는 값은 줄을 만들지 않는다") {
            val title = Arb.alphanumericString(prefix = "Title").next()
            val link = "https://${Arb.alphanumericString(prefix = "host").next()}.com"

            shareTextOf("", link) shouldBe link
            shareTextOf(title, " \t ", link) shouldBe "$title\n$link"
        }

        test("TC-eb8687b3-88db-4bfa-a593-3bf0da7e3823 각 값의 앞뒤 공백 문자를 지운다") {
            val title = Arb.alphanumericString(prefix = "Title").next()
            val link = "https://${Arb.alphanumericString(prefix = "host").next()}.com"

            shareTextOf("  $title\n", "\t$link  ") shouldBe "$title\n$link"
        }

        test("값이 모두 비어 있으면 빈 글이다") {
            shareTextOf("", "  ") shouldBe ""
            shareTextOf() shouldBe ""
        }

        test("값 가운데의 줄바꿈은 그대로 둔다") {
            val first = Arb.alphanumericString(prefix = "First").next()
            val second = Arb.alphanumericString(prefix = "Second").next()
            val link = "https://${Arb.alphanumericString(prefix = "host").next()}.com"

            shareTextOf("$first\n$second", link) shouldBe "$first\n$second\n$link"
        }
    })
