package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail

@Stable
internal class MusicFormState(
    val titleState: DiaryTitleInputState,
    val artistState: MusicArtistInputState,
    val hostState: SnackbarHostState,
) {
    val detail: MusicDetail
        get() =
            MusicDetail(
                title = titleState.text.toString(),
                artist = artistState.text.toString(),
            )
}

@Composable
internal fun rememberMusicAddFormState(): MusicFormState {
    val titleState = rememberDiaryTitleInputState(initialText = MusicDetail.EMPTY.title)
    val artistState = rememberMusicArtistInputState(initialText = MusicDetail.EMPTY.artist)
    val hostState = remember { SnackbarHostState() }

    return remember(titleState, artistState, hostState) {
        MusicFormState(
            titleState = titleState,
            artistState = artistState,
            hostState = hostState,
        )
    }
}
