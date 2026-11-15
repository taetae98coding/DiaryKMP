package io.github.taetae98coding.diary.feature.playlist.ui.detail

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.shareTextOf
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.link.isYoutubeVideoLink

internal sealed interface MusicShareContent {
    data class Text(
        val content: ShareContent.Text,
    ) : MusicShareContent

    data object LinkBlank : MusicShareContent

    data object LinkNotYoutube : MusicShareContent
}

internal fun musicShareContentOf(
    detail: MusicDetail,
    storedTitle: String,
): MusicShareContent {
    val link = detail.link.trim()

    return when {
        link.isEmpty() -> MusicShareContent.LinkBlank

        !link.isYoutubeVideoLink() -> MusicShareContent.LinkNotYoutube

        else -> {
            val title = detail.title.ifBlank { storedTitle }.trim()
            val artist = detail.artist.trim()
            val musicLine = if (artist.isEmpty()) title else "$title$MUSIC_LINE_SEPARATOR$artist"

            MusicShareContent.Text(content = ShareContent.Text(text = shareTextOf(musicLine, link), title = title))
        }
    }
}

private const val MUSIC_LINE_SEPARATOR = " - "
