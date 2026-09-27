package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.ktor.http.ContentType
import kotlin.uuid.Uuid

private const val CRLF = "\r\n"
private const val NAME_FIELD = "name"
private const val TITLE_FIELD = "title"
private const val DESCRIPTION_FIELD = "description"
private const val FILE_FIELD = "file"

// 제목과 설명은 길이 제한이 없어 헤더에 싣지 못하므로 파일 내용과 함께 multipart 본문으로 보낸다.
// iOS의 백그라운드 전송은 기기의 파일에서만 보낼 수 있어, 본문을 앞부분·파일 내용·뒷부분으로 나눠 두고 전송 수단마다 이어 붙인다.
internal class FileUploadMultipart(
    name: String,
    title: String,
    description: String,
    mimeType: String,
    val contentLength: Long,
    boundary: String = "diary-${Uuid.random().toHexString()}",
) {
    val contentType: ContentType = ContentType.MultiPart.FormData.withParameter("boundary", boundary)

    val head: ByteArray =
        buildString {
            appendTextField(boundary = boundary, field = NAME_FIELD, value = name)
            appendTextField(boundary = boundary, field = TITLE_FIELD, value = title)
            appendTextField(boundary = boundary, field = DESCRIPTION_FIELD, value = description)
            append("--$boundary$CRLF")
            append("""Content-Disposition: form-data; name="$FILE_FIELD"; filename="$FILE_FIELD"""")
            append(CRLF)
            append("Content-Type: $mimeType$CRLF")
            append(CRLF)
        }.encodeToByteArray()

    val tail: ByteArray = "$CRLF--$boundary--$CRLF".encodeToByteArray()

    val totalLength: Long
        get() = head.size + contentLength + tail.size

    // 전송 수단은 본문 전체의 보낸 양을 알려 주므로, 앞부분을 빼고 파일 내용에서 보낸 양으로 바꾼다.
    fun contentSentBytes(totalSentBytes: Long): Long = (totalSentBytes - head.size).coerceIn(0, contentLength)
}

private fun StringBuilder.appendTextField(
    boundary: String,
    field: String,
    value: String,
) {
    append("--$boundary$CRLF")
    append("""Content-Disposition: form-data; name="$field"""")
    append(CRLF)
    append(CRLF)
    append(value)
    append(CRLF)
}
