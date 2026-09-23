package io.github.taetae98coding.diary.domain.playlist.link

import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException

private const val YOUTUBE_HOST = "youtube.com"
private const val YOUTU_BE_HOST = "youtu.be"

// 주소 라이브러리를 domain으로 들이지 않으려고 방식, 호스트, 나머지로만 끊어 읽는다.
private val SCHEME_AND_HOST_REGEX = Regex("""^(https?)://([^/?#\s]+)([/?#].*)?$""", RegexOption.IGNORE_CASE)
private const val AUTHORITY_GROUP = 2
private const val PATH_AND_QUERY_GROUP = 3

internal fun String.toYoutubeVideoLinkOrThrow(): String {
    val link = trim()

    if (link.isEmpty()) throw MusicLinkBlankException()
    if (!link.isYoutubeVideoLink()) throw MusicLinkNotYoutubeException()

    return link
}

// 곡의 링크는 비워 둘 수 있으므로 값이 있을 때만 YouTube 링크인지 판정한다.
internal fun String.toOptionalYoutubeVideoLinkOrThrow(): String {
    val link = trim()

    if (link.isEmpty()) return link

    return link.toYoutubeVideoLinkOrThrow()
}

internal fun String.toYoutubeLinkPartsOrNull(): YoutubeLinkParts? {
    val match = SCHEME_AND_HOST_REGEX.matchEntire(trim()) ?: return null
    val host =
        match.groupValues[AUTHORITY_GROUP]
            .substringAfterLast('@')
            .substringBefore(':')
            .lowercase()

    return if (host.isYoutubeHost()) {
        match.groupValues[PATH_AND_QUERY_GROUP].toYoutubeLinkParts(host = host)
    } else {
        null
    }
}

private fun String.toYoutubeLinkParts(host: String): YoutubeLinkParts {
    val path = substringBefore('?').substringBefore('#')
    val query = substringAfter('?', missingDelimiterValue = "").substringBefore('#')

    return YoutubeLinkParts(
        host = host,
        pathSegmentList = path.split('/').filter { segment -> segment.isNotEmpty() },
        queryMap =
            query
                .split('&')
                .filter { parameter -> parameter.isNotEmpty() }
                .associate { parameter -> parameter.substringBefore('=') to parameter.substringAfter('=', missingDelimiterValue = "") },
    )
}

internal data class YoutubeLinkParts(
    val host: String,
    val pathSegmentList: List<String>,
    val queryMap: Map<String, String>,
) {
    val isShortHost: Boolean
        get() = host == YOUTU_BE_HOST
}

private fun String.isYoutubeVideoLink(): Boolean = toYoutubeLinkPartsOrNull() != null

private fun String.isYoutubeHost(): Boolean = this == YOUTUBE_HOST || this == YOUTU_BE_HOST || endsWith(".$YOUTUBE_HOST")
