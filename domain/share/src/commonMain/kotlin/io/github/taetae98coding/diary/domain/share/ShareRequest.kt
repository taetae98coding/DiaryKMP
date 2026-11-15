package io.github.taetae98coding.diary.domain.share

public const val MAX_SHARE_TEXT_BYTES: Int = 64 * 1024

private val contactMimeTypeSet: Set<String> = setOf("text/x-vcard", "text/vcard", "text/directory")
private const val EVENT_MIME_TYPE: String = "text/calendar"

public sealed interface ShareFile {
    public data object None : ShareFile

    public data class Attached(
        val mimeType: String,
    ) : ShareFile
}

public data class ShareRequest(
    val text: String,
    val subject: String,
    val file: ShareFile,
)

public sealed interface ShareReadResult {
    public data class Read(
        val content: ShareContent,
    ) : ShareReadResult

    public data object TooLong : ShareReadResult

    public data object Unreadable : ShareReadResult
}

// readFileText는 한도를 넘는지 알 수 있도록 한도보다 큰 파일이면 한도를 넘는 길이까지 읽어 돌려준다.
public suspend fun ShareRequest.read(readFileText: suspend () -> String): ShareReadResult =
    when (file) {
        ShareFile.None -> readWithin(text = text, subject = subject) { ShareContent.Text(text = text, subject = subject) }
        is ShareFile.Attached -> file.read(readFileText = readFileText)
    }

private suspend fun ShareFile.Attached.read(readFileText: suspend () -> String): ShareReadResult {
    val normalized = mimeType.substringBefore(';').trim().lowercase()

    return when {
        normalized in contactMimeTypeSet -> readFileText().let { text -> readWithin(text = text) { ShareContent.Contact(vCard = text) } }
        normalized == EVENT_MIME_TYPE -> readFileText().let { text -> readWithin(text = text) { ShareContent.Event(iCalendar = text) } }
        else -> ShareReadResult.Unreadable
    }
}

private inline fun readWithin(
    text: String,
    subject: String = "",
    content: () -> ShareContent,
): ShareReadResult =
    when {
        text.isEmpty() -> ShareReadResult.Unreadable
        text.utf8Size() + subject.utf8Size() > MAX_SHARE_TEXT_BYTES -> ShareReadResult.TooLong
        else -> ShareReadResult.Read(content = content())
    }

private fun String.utf8Size(): Int = encodeToByteArray().size
