package io.github.taetae98coding.diary.compose.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeMusicCard(
    onEvent: (MusicListEvent) -> Unit,
    modifier: Modifier = Modifier,
    music: Music? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.music_list_delete_content_description),
        onDelete = { music?.let { value -> onEvent(MusicListEvent.SwipeDelete(id = value.id)) } },
        modifier = modifier,
        key = music?.id,
        gesturesEnabled = music != null,
    ) {
        MusicCard(
            onClick = { music?.let { value -> onEvent(MusicListEvent.ClickMusic(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            music = music,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeMusicCardPreview() {
    DiaryTheme {
        SwipeMusicCard(
            onEvent = {},
            music = previewMusic(title = "곡 제목", artist = "가수"),
        )
    }
}
