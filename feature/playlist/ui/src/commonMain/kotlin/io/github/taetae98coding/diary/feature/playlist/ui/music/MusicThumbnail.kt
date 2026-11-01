package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MusicThumbnail(
    modifier: Modifier = Modifier,
    thumbnailProvider: () -> String = { "" },
) {
    val painter = rememberAsyncImagePainter(model = thumbnailProvider().takeIf { thumbnail -> thumbnail.isNotBlank() })
    val state by painter.state.collectAsStateWithLifecycle()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(MusicThumbnailDefaults.ASPECT_RATIO),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DiaryTheme.colorScheme.surfaceDim,
        ) {}

        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MusicThumbnailPreview() {
    DiaryTheme {
        MusicThumbnail(
            thumbnailProvider = { "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg" },
        )
    }
}
