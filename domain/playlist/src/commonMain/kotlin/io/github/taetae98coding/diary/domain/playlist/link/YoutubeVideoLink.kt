package io.github.taetae98coding.diary.domain.playlist.link

import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException

private const val YOUTUBE_HOST = "youtube.com"
private const val YOUTU_BE_HOST = "youtu.be"

// 주소 라이브러리를 domain으로 들이지 않으려고 방식과 호스트만 끊어 읽는다. 경로와 질의는 판정 대상이 아니다.
private val SCHEME_AND_HOST_REGEX = Regex("""^(https?)://([^/?#\s]+)([/?#].*)?$""", RegexOption.IGNORE_CASE)

internal fun String.toYoutubeVideoLinkOrThrow(): String {
    val link = trim()

    if (link.isEmpty()) throw MusicLinkBlankException()
    if (!link.isYoutubeVideoLink()) throw MusicLinkNotYoutubeException()

    return link
}

private fun String.isYoutubeVideoLink(): Boolean {
    val authority = SCHEME_AND_HOST_REGEX.matchEntire(this)?.groupValues?.get(2) ?: return false
    val host =
        authority
            .substringAfterLast('@')
            .substringBefore(':')
            .lowercase()

    return host == YOUTUBE_HOST || host == YOUTU_BE_HOST || host.endsWith(".$YOUTUBE_HOST")
}
