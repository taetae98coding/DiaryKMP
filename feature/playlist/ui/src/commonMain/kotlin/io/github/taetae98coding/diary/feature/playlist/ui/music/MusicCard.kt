package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic

internal const val MUSIC_CARD_TEST_TAG: String = "MusicCard"

@Composable
internal fun MusicCard(
    modifier: Modifier = Modifier,
    music: Music? = null,
) {
    Card(modifier = modifier.testTag(MUSIC_CARD_TEST_TAG)) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
            null,
        )
}

@ComponentPreview
@Composable
private fun MusicCardPreview(
    @PreviewParameter(MusicCardPreviewParameter::class) music: Music?,
) {
    DiaryTheme {
        MusicCard(music = music)
    }
}
