package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MusicDetailScaffold(
    onEvent: (MusicDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MusicFormState = rememberMusicDetailFormState(),
    uiStateProvider: () -> MusicDetailUiState = { MusicDetailUiState.Loading },
    componentVisibleProvider: () -> MusicDetailScaffoldComponentVisible = { MusicDetailScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? MusicDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(MusicDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            MusicDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            if (isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.music_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? MusicDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        MusicDetailScaffoldContent(
            onFetchLinkClick = { onEvent(MusicDetailScaffoldEvent.ClickFetchLink) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
        )
    }
}

private class MusicDetailUiStatePreviewParameter : PreviewParameterProvider<MusicDetailUiState> {
    override val values: Sequence<MusicDetailUiState> =
        sequenceOf(
            MusicDetailUiState.Loading,
            MusicDetailUiState.Content(id = Uuid.NIL, detail = previewMusic(title = "곡 제목", artist = "가수").detail),
        )
}

@ScreenPreview
@Composable
private fun MusicDetailScaffoldPreview(
    @PreviewParameter(MusicDetailUiStatePreviewParameter::class) uiState: MusicDetailUiState,
) {
    val detail = (uiState as? MusicDetailUiState.Content)?.detail ?: MusicDetail.EMPTY

    DiaryTheme {
        MusicDetailScaffold(
            onEvent = {},
            state = rememberMusicDetailFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
