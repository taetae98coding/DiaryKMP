package io.github.taetae98coding.diary.compose.core.share

public enum class ShareFileType(
    public val mimeType: String,
    public val extension: String,
) {
    VCARD(mimeType = "text/vcard", extension = "vcf"),
    CALENDAR(mimeType = "text/calendar", extension = "ics"),
    PNG(mimeType = "image/png", extension = "png"),
}

internal fun ShareFileType.fileNameOf(name: String): String {
    val sanitized =
        name
            .trim()
            .map { char -> if (char in invalidFileNameCharSet || char.isISOControl()) INVALID_CHAR_REPLACEMENT else char }
            .joinToString(separator = "")

    return "${sanitized.ifEmpty { FALLBACK_FILE_NAME }}.$extension"
}

private val invalidFileNameCharSet: Set<Char> = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

private const val INVALID_CHAR_REPLACEMENT = '_'

// 화면은 항목 제목을 이름으로 넘기므로 비는 일이 없지만, 끝만 남은 파일 이름(.vcf)은 받는 앱이 숨김 파일로 다룬다.
private const val FALLBACK_FILE_NAME = "share"
