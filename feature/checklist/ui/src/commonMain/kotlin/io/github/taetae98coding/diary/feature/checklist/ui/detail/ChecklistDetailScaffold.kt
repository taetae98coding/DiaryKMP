package io.github.taetae98coding.diary.feature.checklist.ui.detail

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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryChecklistItemSortBottomSheetHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_loading_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklist
import io.github.taetae98coding.diary.feature.checklist.ui.tag.ChecklistTagInputEvent
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistDetailScaffold(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    onTagInputEvent: (ChecklistTagInputEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> ChecklistDetailUiState = { ChecklistDetailUiState.Loading },
    itemUiStateProvider: () -> ChecklistDetailItemUiState = { ChecklistDetailItemUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> ChecklistDetailScaffoldComponentVisible = { ChecklistDetailScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? ChecklistDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(ChecklistDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            ChecklistDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                itemUiStateProvider = itemUiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.checklist_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? ChecklistDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        ChecklistDetailScaffoldContent(
            onEvent = onEvent,
            onTagInputEvent = onTagInputEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
            itemUiStateProvider = itemUiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
        )
    }

    DiaryChecklistItemSortBottomSheetHost(
        onSelect = { sort -> onEvent(ChecklistDetailScaffoldEvent.SelectSort(sort = sort)) },
        state = state.sortSheetState,
        sortProvider = { itemUiStateProvider().sort },
    )
    ChecklistDetailResetDialogHost(
        onEvent = onEvent,
        state = state.resetDialogState,
    )
    EntityTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@Composable
private fun ChecklistDetailScaffoldContent(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    onTagInputEvent: (ChecklistTagInputEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
    uiStateProvider: () -> ChecklistDetailUiState = { ChecklistDetailUiState.Loading },
    itemUiStateProvider: () -> ChecklistDetailItemUiState = { ChecklistDetailItemUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is ChecklistDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            ChecklistDetailContent(
                onEvent = onEvent,
                onTagInputEvent = onTagInputEvent,
                modifier = Modifier.fillMaxSize(),
                state = state,
                itemUiStateProvider = itemUiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            DiaryLoadingBox(
                modifier = Modifier.fillMaxSize(),
                contentDescription = stringResource(Res.string.checklist_detail_loading_content_description),
            )
        }
    }
}

private class ChecklistDetailUiStatePreviewParameter : PreviewParameterProvider<ChecklistDetailUiState> {
    override val values: Sequence<ChecklistDetailUiState> =
        sequenceOf(
            ChecklistDetailUiState.Loading,
            ChecklistDetailUiState.Content(id = Uuid.NIL, detail = previewChecklist(title = "여행 준비").detail),
        )
}

@ScreenPreview
@Composable
private fun ChecklistDetailScaffoldPreview(
    @PreviewParameter(ChecklistDetailUiStatePreviewParameter::class) uiState: ChecklistDetailUiState,
) {
    val detail = (uiState as? ChecklistDetailUiState.Content)?.detail ?: ChecklistDetail.EMPTY

    DiaryTheme {
        ChecklistDetailScaffold(
            onEvent = {},
            onTagInputEvent = {},
            onTagPickerEvent = {},
            state = rememberChecklistDetailFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
