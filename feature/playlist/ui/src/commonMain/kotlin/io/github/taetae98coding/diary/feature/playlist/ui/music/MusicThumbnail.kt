package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal const val MUSIC_THUMBNAIL_ASPECT_RATIO: Float = 16F / 9F

@Composable
internal fun MusicThumbnail(
    modifier: Modifier = Modifier,
    thumbnailProvider: () -> String = { "" },
) {
    val painter = rememberAsyncImagePainter(model = thumbnailProvider().takeIf { thumbnail -> thumbnail.isNotBlank() })
    val state by painter.state.collectAsState()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(MUSIC_THUMBNAIL_ASPECT_RATIO),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DiaryTheme.colorScheme.surfaceContainerHighest,
        ) {}

        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                // YouTube 썸네일은 4:3이고 위아래에 검은 띠가 있을 수 있어 맞춰 넣지 않고 잘라 넣는다.
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private class MusicThumbnailPreviewParameter : PreviewParameterProvider<String> {
    override val values: Sequence<String> =
        sequenceOf(
            "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            "",
        )
}

@ComponentPreview
@Composable
private fun MusicThumbnailPreview(
    @PreviewParameter(MusicThumbnailPreviewParameter::class) thumbnail: String,
) {
    DiaryTheme {
        MusicThumbnail(thumbnailProvider = { thumbnail })
    }
}
