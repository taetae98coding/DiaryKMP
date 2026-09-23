package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicForm
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_add_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_title
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicAddScaffold(
    onEvent: (MusicAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MusicFormState = rememberMusicAddFormState(),
    uiStateProvider: () -> MusicAddUiState = { MusicAddUiState() },
    componentVisibleProvider: () -> MusicAddScaffoldComponentVisible = { MusicAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(MusicAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.music_add_title),
                onNavigateUp = { onEvent(MusicAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.playlist_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(MusicAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.music_add_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        MusicForm(
            onFetchLinkClick = { onEvent(MusicAddScaffoldEvent.ClickFetchLink) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            isFetchInProgressProvider = { uiStateProvider().isLinkFetchInProgress },
        )
    }
}

@ScreenPreview
@Composable
private fun MusicAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        MusicAddScaffold(
            onEvent = {},
            uiStateProvider = { MusicAddUiState(isInProgress = isInProgress) },
        )
    }
}
