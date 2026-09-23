package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MusicForm(
    onFetchLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: MusicFormState = rememberMusicAddFormState(),
    isFetchInProgressProvider: () -> Boolean = { false },
) {
    DiaryInputColumn(modifier = modifier) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        MusicArtistInput(
            state = state.artistState,
            modifier = Modifier.fillMaxWidth(),
        )
        MusicLinkInput(
            onFetchClick = onFetchLinkClick,
            modifier = Modifier.fillMaxWidth(),
            state = state.linkState,
            isFetchInProgressProvider = isFetchInProgressProvider,
            thumbnailProvider = { state.thumbnail },
        )
    }
}

@ScreenPreview
@Composable
private fun MusicFormPreview() {
    DiaryTheme {
        Surface {
            MusicForm(onFetchLinkClick = {})
        }
    }
}
