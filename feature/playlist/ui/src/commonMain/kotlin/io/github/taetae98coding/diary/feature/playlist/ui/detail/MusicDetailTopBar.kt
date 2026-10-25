package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_open_in_new_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MusicDetailTopBar(
    onEvent: (MusicDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MusicDetailUiState = { MusicDetailUiState.Loading },
    componentVisibleProvider: () -> MusicDetailScaffoldComponentVisible = { MusicDetailScaffoldComponentVisible() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.playlist_navigate_up_button_content_description)
    val openInNewContentDescription = stringResource(Res.string.music_detail_open_in_new_button_content_description)
    val deleteContentDescription = stringResource(Res.string.music_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is MusicDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is MusicDetailUiState.Content) {
                IconAction(
                    icon = DiaryTopBarIcon.OPEN_IN_NEW,
                    contentDescription = openInNewContentDescription,
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickOpenInNew) },
                    isVisible = uiState.detail.link.isNotBlank(),
                )
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun MusicDetailTopBarPreview() {
    DiaryTheme {
        MusicDetailTopBar(
            onEvent = {},
            uiStateProvider = { MusicDetailUiState.Content(id = Uuid.NIL, detail = previewMusic(title = "곡 제목", artist = "가수").detail) },
        )
    }
}
