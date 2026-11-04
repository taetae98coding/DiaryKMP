package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class FileUploadMultipartTest :
    FunSpec({
        test("본문 전체 크기는 앞부분, 파일 내용, 뒷부분 크기의 합이다") {
            val contentLength =
                Arb
                    .int()
                    .next()
                    .toLong()
                    .and(0xFFFF)
            val multipart = multipart(contentLength = contentLength)

            multipart.totalLength shouldBe multipart.head.size + contentLength + multipart.tail.size
        }

        test("전체 보낸 양에서 앞부분을 빼고 파일 크기 안으로 맞춘 양을 파일에서 보낸 양으로 본다") {
            val multipart = multipart(contentLength = 1_000)
            val head = multipart.head.size.toLong()

            multipart.contentSentBytes(totalSentBytes = 0) shouldBe 0
            multipart.contentSentBytes(totalSentBytes = head - 1) shouldBe 0
            multipart.contentSentBytes(totalSentBytes = head + 400) shouldBe 400
            multipart.contentSentBytes(totalSentBytes = multipart.totalLength) shouldBe 1_000
        }

        test("올리기마다 다른 경계를 쓴다") {
            multipart(contentLength = 0).contentType shouldNotBe multipart(contentLength = 0).contentType
        }
    })

private fun multipart(contentLength: Long): FileUploadMultipart =
    FileUploadMultipart(
        name = "file-${Arb.int().next()}.txt",
        title = "title-${Arb.string().next()}",
        description = Arb.string().next(),
        mimeType = "text/plain",
        contentLength = contentLength,
    )
