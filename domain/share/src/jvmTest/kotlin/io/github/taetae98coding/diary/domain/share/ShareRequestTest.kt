package io.github.taetae98coding.diary.domain.share

import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ShareRequestTest :
    FunSpec({
        test("글은 원문과 공유 제목을 그대로 넘긴다") {
            val text = "  ${Arb.alphanumericString(prefix = "Text").next()}\n"
            val subject = "  ${Arb.alphanumericString(prefix = "Subject").next()}  "
            val request = ShareRequest(text = text, subject = subject, file = ShareFile.None)

            request.read(readFileText = { error("파일을 읽지 않는다") }) shouldBe ShareReadResult.Read(content = ShareContent.Text(text = text, subject = subject))
        }

        test("TC-d0eb4e3b-73f9-47ad-976d-9c33ac0b12b1 빈 글은 열지 않는다") {
            ShareRequest(text = "", subject = Arb.alphanumericString(prefix = "Subject").next(), file = ShareFile.None).read(readFileText = { "" }) shouldBe
                ShareReadResult.Unreadable
        }

        test("TC-b280acf1-29e3-44b8-8b9f-8960f3fa37c0 연락처 파일") {
            listOf("text/x-vcard", "text/vcard", "text/directory; profile=vCard", "TEXT/VCARD").forEach { mimeType ->
                val vCard = Arb.alphanumericString(prefix = "VCard").next()
                val request = ShareRequest(text = "", subject = "", file = ShareFile.Attached(mimeType = mimeType))

                request.read(readFileText = { vCard }) shouldBe ShareReadResult.Read(content = ShareContent.Contact(vCard = vCard))
            }
        }

        test("TC-b280acf1-29e3-44b8-8b9f-8960f3fa37c0 일정 파일") {
            val iCalendar = Arb.alphanumericString(prefix = "ICalendar").next()
            val request = ShareRequest(text = "", subject = "", file = ShareFile.Attached(mimeType = "text/calendar"))

            request.read(readFileText = { iCalendar }) shouldBe ShareReadResult.Read(content = ShareContent.Event(iCalendar = iCalendar))
        }

        test("TC-b280acf1-29e3-44b8-8b9f-8960f3fa37c0 그 밖의 파일") {
            listOf("application/pdf", "image/png").forEach { mimeType ->
                ShareRequest(text = "", subject = "", file = ShareFile.Attached(mimeType = mimeType))
                    .read(readFileText = { Arb.alphanumericString(prefix = "File").next() }) shouldBe ShareReadResult.Unreadable
            }
        }

        test("TC-088449f3-2488-4202-ae77-3a277c59b6d8 파일과 글이 함께 오면 파일을 쓴다") {
            val vCard = Arb.alphanumericString(prefix = "VCard").next()
            val request =
                ShareRequest(text = "https://example.com", subject = Arb.alphanumericString(prefix = "Subject").next(), file = ShareFile.Attached(mimeType = "text/x-vcard"))

            request.read(readFileText = { vCard }) shouldBe ShareReadResult.Read(content = ShareContent.Contact(vCard = vCard))
        }

        test("TC-ec691cea-8626-42fe-b650-bff377ae5cea 받지 않는 파일은 글이 함께 와도 받지 않고 닫는다") {
            listOf("application/pdf", "image/png", "text/plain").forEach { mimeType ->
                ShareRequest(text = "https://example.com", subject = "", file = ShareFile.Attached(mimeType = mimeType))
                    .read(readFileText = { Arb.alphanumericString(prefix = "File").next() }) shouldBe ShareReadResult.Unreadable
            }
        }

        test("TC-d0eb4e3b-73f9-47ad-976d-9c33ac0b12b1 비었거나 읽지 못한 파일은 열지 않는다") {
            listOf("text/x-vcard", "text/calendar").forEach { mimeType ->
                ShareRequest(text = "https://example.com", subject = "", file = ShareFile.Attached(mimeType = mimeType)).read(readFileText = { "" }) shouldBe
                    ShareReadResult.Unreadable
            }
        }

        test("TC-c0589900-2307-4f7d-bf06-7a5646ca17dc 한도를 넘는 공유는 너무 길다고 알린다") {
            val subject = "s"
            val limit = "a".repeat(MAX_SHARE_TEXT_BYTES - subject.length)

            ShareRequest(text = limit, subject = subject, file = ShareFile.None).read(readFileText = { "" }) shouldBe
                ShareReadResult.Read(content = ShareContent.Text(text = limit, subject = subject))
            ShareRequest(text = limit + "a", subject = subject, file = ShareFile.None).read(readFileText = { "" }) shouldBe ShareReadResult.TooLong
            ShareRequest(text = "가".repeat(MAX_SHARE_TEXT_BYTES / UTF8_BYTES_OF_HANGUL + 1), subject = "", file = ShareFile.None).read(readFileText = { "" }) shouldBe ShareReadResult.TooLong
            ShareRequest(text = "", subject = "", file = ShareFile.Attached(mimeType = "text/x-vcard")).read(readFileText = { "a".repeat(MAX_SHARE_TEXT_BYTES) }) shouldBe
                ShareReadResult.Read(content = ShareContent.Contact(vCard = "a".repeat(MAX_SHARE_TEXT_BYTES)))
            ShareRequest(text = "", subject = "", file = ShareFile.Attached(mimeType = "text/x-vcard")).read(readFileText = { "a".repeat(MAX_SHARE_TEXT_BYTES + 1) }) shouldBe
                ShareReadResult.TooLong
        }
    })

private const val UTF8_BYTES_OF_HANGUL = 3
