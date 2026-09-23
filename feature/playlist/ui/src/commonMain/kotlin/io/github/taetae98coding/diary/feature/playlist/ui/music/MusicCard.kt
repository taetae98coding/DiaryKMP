@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoThumbnailOrNull
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic

internal const val MUSIC_CARD_TEST_TAG: String = "MusicCard"

@Composable
internal fun MusicCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    music: Music? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(MUSIC_CARD_TEST_TAG),
        enabled = music != null,
    ) {
        MusicThumbnail(thumbnailProvider = {
            music
                ?.detail
                ?.link
                ?.toYoutubeVideoThumbnailOrNull()
                .orEmpty()
        })
        Column(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            Text(
                text = music?.detail?.title.orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )
            Text(
                text = music?.detail?.artist.orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.bodySmall,
            )
        }
    }
}

private class MusicCardPreviewParameter : PreviewParameterProvider<Music?> {
    override val values: Sequence<Music?> =
        sequenceOf(
            previewMusic(title = "곡 제목", artist = "가수"),
            previewMusic(title = "링크 없는 곡", artist = "가수", link = ""),
            previewMusic(title = "가수 없는 곡", artist = ""),
            null,
        )
}

@ComponentPreview
@Composable
private fun MusicCardPreview(
    @PreviewParameter(MusicCardPreviewParameter::class) music: Music?,
) {
    DiaryTheme {
        MusicCard(
            onClick = {},
            music = music,
        )
    }
}
