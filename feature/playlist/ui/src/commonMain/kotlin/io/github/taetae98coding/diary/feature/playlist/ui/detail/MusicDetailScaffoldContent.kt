package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicForm
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormEvent
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState

@Composable
internal fun MusicDetailScaffoldContent(
    onFetchLinkClick: () -> Unit,
    onFormEvent: (MusicFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MusicFormState = rememberMusicDetailFormState(),
    uiStateProvider: () -> MusicDetailUiState = { MusicDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is MusicDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            MusicForm(
                onFetchLinkClick = onFetchLinkClick,
                onEvent = onFormEvent,
                modifier = Modifier.fillMaxSize(),
                state = state,
                isFetchInProgressProvider = { (uiStateProvider() as? MusicDetailUiState.Content)?.isLinkFetchInProgress == true },
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun MusicDetailScaffoldContentPreview() {
    DiaryTheme {
        MusicDetailScaffoldContent(
            onFetchLinkClick = {},
            onFormEvent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
