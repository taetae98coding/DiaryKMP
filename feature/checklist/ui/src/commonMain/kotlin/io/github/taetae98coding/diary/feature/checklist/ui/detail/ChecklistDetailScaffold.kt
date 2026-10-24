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
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryChecklistItemSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_loading_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklist
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistDetailScaffold(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
    uiStateProvider: () -> ChecklistDetailUiState = { ChecklistDetailUiState.Loading },
    itemUiStateProvider: () -> ChecklistDetailItemUiState = { ChecklistDetailItemUiState() },
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
        DiaryCrossfade(
            targetState = uiStateProvider() is ChecklistDetailUiState.Content,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) { isContent ->
            if (isContent) {
                ChecklistDetailContent(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    itemUiStateProvider = itemUiStateProvider,
                )
            } else {
                DiaryLoadingBox(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = stringResource(Res.string.checklist_detail_loading_content_description),
                )
            }
        }
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
            state = rememberChecklistDetailFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
