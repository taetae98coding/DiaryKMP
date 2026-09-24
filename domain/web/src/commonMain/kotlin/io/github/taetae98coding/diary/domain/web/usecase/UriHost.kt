package io.github.taetae98coding.diary.domain.web.usecase

private const val SCHEME_SEPARATOR = "://"
private const val AUTHORITY_TERMINATOR_SET = "/?#"

// 공용 Kotlin에는 URL 파서가 없어 RFC 3986의 authority 자리에서 사용자 정보와 포트를 뗀 호스트만 읽는다.
internal fun String.uriHostOrNull(): String? {
    val schemeEnd = indexOf(SCHEME_SEPARATOR)
    if (schemeEnd <= 0) return null

    val authorityStart = schemeEnd + SCHEME_SEPARATOR.length
    val authorityEnd =
        indices
            .drop(authorityStart)
            .firstOrNull { index -> this[index] in AUTHORITY_TERMINATOR_SET }
            ?: length
    val authority = substring(authorityStart, authorityEnd).substringAfterLast('@')
    val host =
        if (authority.startsWith('[')) {
            authority.substringBefore(']').removePrefix("[")
        } else {
            authority.substringBefore(':')
        }

    return host.lowercase().ifBlank { null }
}
