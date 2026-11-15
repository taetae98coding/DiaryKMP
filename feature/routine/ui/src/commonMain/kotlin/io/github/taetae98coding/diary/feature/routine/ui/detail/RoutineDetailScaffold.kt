package io.github.taetae98coding.diary.feature.routine.ui.detail

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
import io.github.taetae98coding.diary.compose.routine.previewRoutineDetail
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormEvent
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_loading_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_update_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun RoutineDetailScaffold(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    onFormEvent: (RoutineFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    recordPagingItems: LazyPagingItems<RoutineRecord> = remember { flowOf(PagingData.empty<RoutineRecord>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> RoutineDetailUiState = { RoutineDetailUiState.Loading },
    recordUiStateProvider: () -> RoutineDetailRecordUiState = { RoutineDetailRecordUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> RoutineDetailScaffoldComponentVisible = { RoutineDetailScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? RoutineDetailUiState.Content
            content != null && state.formState.isChangedFrom(detail = content.detail)
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(RoutineDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            RoutineDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.formState.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(RoutineDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.routine_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? RoutineDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        RoutineDetailScaffoldContent(
            onEvent = onEvent,
            onFormEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            recordPagingItems = recordPagingItems,
            uiStateProvider = uiStateProvider,
            recordUiStateProvider = recordUiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
        )
    }

    EntityTagPickerDialogHost(
        dialogState = state.formState.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@Composable
private fun RoutineDetailScaffoldContent(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    onFormEvent: (RoutineFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
    recordPagingItems: LazyPagingItems<RoutineRecord> = remember { flowOf(PagingData.empty<RoutineRecord>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> RoutineDetailUiState = { RoutineDetailUiState.Loading },
    recordUiStateProvider: () -> RoutineDetailRecordUiState = { RoutineDetailRecordUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    val detail = (uiStateProvider() as? RoutineDetailUiState.Content)?.detail

    DiaryCrossfade(
        targetState = detail != null,
        modifier = modifier,
    ) { isContent ->
        if (isContent && detail != null) {
            RoutineDetailContent(
                onEvent = onEvent,
                onFormEvent = onFormEvent,
                detail = detail,
                modifier = Modifier.fillMaxSize(),
                state = state,
                recordPagingItems = recordPagingItems,
                recordUiStateProvider = recordUiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else if (!isContent) {
            DiaryLoadingBox(
                modifier = Modifier.fillMaxSize(),
                contentDescription = stringResource(Res.string.routine_detail_loading_content_description),
            )
        }
    }
}

private class RoutineDetailUiStatePreviewParameter : PreviewParameterProvider<RoutineDetailUiState> {
    override val values: Sequence<RoutineDetailUiState> =
        sequenceOf(
            RoutineDetailUiState.Loading,
            RoutineDetailUiState.Content(id = Uuid.NIL, detail = previewRoutineDetail(title = "물 마시기"), isFinished = false),
        )
}

@ScreenPreview
@Composable
private fun RoutineDetailScaffoldPreview(
    @PreviewParameter(RoutineDetailUiStatePreviewParameter::class) uiState: RoutineDetailUiState,
) {
    val detail = (uiState as? RoutineDetailUiState.Content)?.detail

    DiaryTheme {
        RoutineDetailScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            state = rememberRoutineDetailScaffoldState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
