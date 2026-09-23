package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.OpenInNewButton
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
    TopAppBar(
        // 제목과 액션은 서로 다른 자리이므로 각 슬롯에서 상태를 읽어, 한쪽이 바뀔 때 다른 쪽까지 다시 그리지 않는다.
        title = {
            val uiState = uiStateProvider()

            if (uiState is MusicDetailUiState.Content) {
                Text(
                    text = uiState.detail.title,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.playlist_navigate_up_button_content_description),
                )
            }
        },
        actions = {
            val uiState = uiStateProvider()

            if (uiState is MusicDetailUiState.Content) {
                if (uiState.detail.link.isNotBlank()) {
                    OpenInNewButton(
                        onClick = { onEvent(MusicDetailScaffoldEvent.ClickOpenInNew) },
                        contentDescription = stringResource(Res.string.music_detail_open_in_new_button_content_description),
                    )
                }
                DeleteButton(
                    onClick = { onEvent(MusicDetailScaffoldEvent.ClickDelete) },
                    contentDescription = stringResource(Res.string.music_detail_delete_button_content_description),
                    isInProgressProvider = { uiState.isDeleteInProgress },
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
