package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.form.TagForm
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagAddFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInput
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerDialogHost
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerEvent
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_add_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagAddScaffold(
    onEvent: (TagAddScaffoldEvent) -> Unit,
    onLinkPickerEvent: (TagLinkPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: TagFormState = rememberTagAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> TagAddUiState = { TagAddUiState() },
    linkUiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
    componentVisibleProvider: () -> TagAddScaffoldComponentVisible = { TagAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(TagAddScaffoldEvent.ClickAdd) },
        topBar = {
            TopBar(
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(TagAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.tag_add_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        TagForm(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            TagLinkInput(
                onTagClick = { id -> onEvent(TagAddScaffoldEvent.ClickLink(id = id)) },
                onLinkClick = { onEvent(TagAddScaffoldEvent.ClickLinkAdd) },
                modifier = Modifier.fillMaxWidth(),
                uiStateProvider = linkUiStateProvider,
            )
        }
    }

    TagLinkPickerDialogHost(
        dialogState = state.linkPickerDialogState,
        onEvent = onLinkPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = linkUiStateProvider,
    )
}

@Composable
private fun TopBar(
    onEvent: (TagAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentVisibleProvider: () -> TagAddScaffoldComponentVisible = { TagAddScaffoldComponentVisible() },
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.tag_add_title)) },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(TagAddScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.tag_add_navigate_up_button_content_description),
                )
            }
        },
    )
}

@ScreenPreview
@Composable
private fun TagAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        TagAddScaffold(
            onEvent = {},
            onLinkPickerEvent = {},
            uiStateProvider = { TagAddUiState(isInProgress = isInProgress) },
        )
    }
}
