package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_delete_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwipeToDeleteMusicCard(
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    music: Music? = null,
    downloadStateProvider: () -> MusicDownloadState? = { null },
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.playlist_home_delete_content_description),
        onDelete = onDelete,
        modifier = modifier,
        key = music?.id,
        gesturesEnabled = music != null,
    ) {
        MusicCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            music = music,
            downloadStateProvider = downloadStateProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeleteMusicCardPreview() {
    DiaryTheme {
        SwipeToDeleteMusicCard(
            onClick = {},
            onDelete = {},
            music = previewMusic(title = "곡 제목", artist = "가수"),
        )
    }
}
