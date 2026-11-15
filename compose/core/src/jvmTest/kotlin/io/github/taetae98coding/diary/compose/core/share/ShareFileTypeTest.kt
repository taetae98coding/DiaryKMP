package io.github.taetae98coding.diary.compose.core.share

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.randomByteArray
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next

class ShareFileTypeTest :
    FunSpec({
        test("TC-ad1c1280-cf45-4567-b36b-d22b6959abed 정한 이름 뒤에 형식별 끝을 붙인다") {
            listOf(
                ShareFileType.VCARD to "vcf",
                ShareFileType.CALENDAR to "ics",
                ShareFileType.PNG to "png",
            ).forEach { (type, extension) ->
                val name = Arb.alphanumericString(prefix = "Name").next()

                type.fileNameOf(name = name) shouldBe "$name.$extension"
            }
        }

        test("TC-0a047fb6-8b50-4f29-9c41-88124ccb7d86 쓸 수 없는 글자는 바꾸고 앞뒤 공백은 지운다") {
            val first = Arb.alphanumericString(prefix = "First").next()
            val second = Arb.alphanumericString(prefix = "Second").next()

            ShareFileType.VCARD.fileNameOf(name = "A/B\\C:D*E?F\"G<H>I|J") shouldBe "A_B_C_D_E_F_G_H_I_J.vcf"
            ShareFileType.CALENDAR.fileNameOf(name = " $first\n$second") shouldBe "${first}_$second.ics"
        }

        test("형식별 파일 종류") {
            ShareFileType.VCARD.mimeType shouldBe "text/vcard"
            ShareFileType.CALENDAR.mimeType shouldBe "text/calendar"
            ShareFileType.PNG.mimeType shouldBe "image/png"
        }

        test("TC-49912e7f-75f8-4960-b2f1-eb7406d63568 정리한 이름이 비면 share를 쓴다") {
            listOf("", " \n ").forEach { name ->
                val type = Arb.enum<ShareFileType>().next()

                type.fileNameOf(name = name) shouldBe "share.${type.extension}"
            }
        }

        test("파일 내용은 바이트 내용으로 비교한다") {
            val bytes = Arb.randomByteArray().next()
            val name = Arb.alphanumericString(prefix = "Name").next()
            val type = Arb.enum<ShareFileType>().next()

            ShareContent.File(bytes = bytes, name = name, type = type) shouldBe ShareContent.File(bytes = bytes.copyOf(), name = name, type = type)
            ShareContent.File(bytes = bytes + byteArrayOf(1), name = name, type = type) shouldNotBe ShareContent.File(bytes = bytes, name = name, type = type)
        }
    })
