package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState

@Composable
internal fun MusicThumbnail(
    modifier: Modifier = Modifier,
    thumbnailProvider: () -> String = { "" },
    downloadStateProvider: () -> MusicDownloadState? = { null },
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
            color = DiaryTheme.colorScheme.surfaceContainerHighest,
        ) {}

        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        downloadStateProvider()?.let { downloadState ->
            MusicDownloadBadge(
                state = downloadState,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(MusicThumbnailDefaults.BadgePadding),
            )

            if (downloadState is MusicDownloadState.Running) {
                val progressModifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(MusicThumbnailDefaults.ProgressIndicatorHeight)
                val progress = downloadState.progress

                if (progress == null) {
                    LinearProgressIndicator(modifier = progressModifier)
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = progressModifier,
                    )
                }
            }
        }
    }
}

private class MusicThumbnailPreviewParameter : PreviewParameterProvider<MusicDownloadState?> {
    override val values: Sequence<MusicDownloadState?> =
        sequenceOf(
            null,
            MusicDownloadState.Pending,
            MusicDownloadState.Running(progress = null),
            MusicDownloadState.Running(progress = 0.62F),
            MusicDownloadState.Done,
            MusicDownloadState.Failed,
        )
}

@ComponentPreview
@Composable
private fun MusicThumbnailPreview(
    @PreviewParameter(MusicThumbnailPreviewParameter::class) downloadState: MusicDownloadState?,
) {
    DiaryTheme {
        MusicThumbnail(
            thumbnailProvider = { "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg" },
            downloadStateProvider = { downloadState },
        )
    }
}
