package io.github.taetae98coding.diary.domain.playlist.link

private const val VIDEO_ID_QUERY_KEY = "v"
private const val WATCH_PATH = "watch"
private val VIDEO_ID_PATH_PREFIX_SET = setOf("shorts", "embed", "live", "v")
private val VIDEO_ID_REGEX = Regex("""^[A-Za-z0-9_-]{11}$""")

public fun String.toYoutubeVideoThumbnailOrNull(): String? {
    val videoId = toYoutubeVideoIdOrNull() ?: return null

    return "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
}

public fun String.isYoutubeVideoId(): Boolean = VIDEO_ID_REGEX.matches(this)

public fun String.toYoutubeVideoLink(): String = "https://www.youtube.com/watch?v=$this"

internal fun String.toYoutubeVideoIdOrNull(): String? {
    val parts = toYoutubeLinkPartsOrNull() ?: return null
    val segmentList = parts.pathSegmentList
    val candidate =
        when {
            parts.isShortHost -> segmentList.firstOrNull()
            segmentList.singleOrNull() == WATCH_PATH -> parts.queryMap[VIDEO_ID_QUERY_KEY]
            segmentList.size == 2 && segmentList[0] in VIDEO_ID_PATH_PREFIX_SET -> segmentList[1]
            else -> null
        }

    return candidate?.takeIf { value -> VIDEO_ID_REGEX.matches(value) }
}
