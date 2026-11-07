package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicForm
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormEvent
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_add_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_title
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicAddScaffold(
    onEvent: (MusicAddScaffoldEvent) -> Unit,
    onFormEvent: (MusicFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MusicFormState = rememberMusicAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MusicAddUiState = { MusicAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
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
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
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
            onEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            isFetchInProgressProvider = { uiStateProvider().isLinkFetchInProgress },
            tagUiStateProvider = tagUiStateProvider,
        )
    }

    EntityTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun MusicAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        MusicAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { MusicAddUiState(isInProgress = isInProgress) },
        )
    }
}
