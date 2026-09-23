package io.github.taetae98coding.diary.domain.playlist.link

private const val VIDEO_ID_QUERY_KEY = "v"
private const val WATCH_PATH = "watch"
private val VIDEO_ID_PATH_PREFIX_SET = setOf("shorts", "embed", "live", "v")
private val VIDEO_ID_REGEX = Regex("""^[A-Za-z0-9_-]{11}$""")

// 썸네일은 저장하지 않고 링크에서 영상 ID를 얻어 만든다. 주소 형식은 docs/spec/music-add.md의 `썸네일 이미지 조회`가 가리키는 문서를 따른다.
public fun String.toYoutubeVideoThumbnailOrNull(): String? {
    val videoId = toYoutubeVideoIdOrNull() ?: return null

    return "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
}

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
