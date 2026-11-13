package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
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
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_detail_loading_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayForm
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormEvent
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayDetailFormState
import io.github.taetae98coding.diary.feature.dday.ui.previewDDayDetail
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DDayDetailScaffold(
    onEvent: (DDayDetailScaffoldEvent) -> Unit,
    onFormEvent: (DDayFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayDetailFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> DDayDetailUiState = { DDayDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> DDayDetailScaffoldComponentVisible = { DDayDetailScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? DDayDetailUiState.Content
            content != null && state.isChangedFrom(detail = content.detail)
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(DDayDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            DDayDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(DDayDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.dday_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? DDayDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        DDayDetailScaffoldContent(
            onFormEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
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

@Composable
private fun DDayDetailScaffoldContent(
    onFormEvent: (DDayFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayDetailFormState(),
    uiStateProvider: () -> DDayDetailUiState = { DDayDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is DDayDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            DDayForm(
                onEvent = onFormEvent,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .systemScroll(state.scrollState) { state.scrollState.animateScrollTo(0) }
                        .verticalScroll(state.scrollState)
                        .padding(
                            start = DiaryTheme.dimens.screenHorizontalPadding,
                            top = DiaryTheme.dimens.screenVerticalPadding,
                            end = DiaryTheme.dimens.screenHorizontalPadding,
                            bottom = DiaryTheme.dimens.floatingActionButtonClearance,
                        ),
                state = state,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            DiaryLoadingBox(
                modifier = Modifier.fillMaxSize(),
                contentDescription = stringResource(Res.string.dday_detail_loading_content_description),
            )
        }
    }
}

private class DDayDetailUiStatePreviewParameter : PreviewParameterProvider<DDayDetailUiState> {
    override val values: Sequence<DDayDetailUiState> =
        sequenceOf(
            DDayDetailUiState.Loading,
            DDayDetailUiState.Content(id = Uuid.NIL, detail = previewDDayDetail(title = "만난 날")),
        )
}

@ScreenPreview
@Composable
private fun DDayDetailScaffoldPreview(
    @PreviewParameter(DDayDetailUiStatePreviewParameter::class) uiState: DDayDetailUiState,
) {
    val detail = (uiState as? DDayDetailUiState.Content)?.detail

    DiaryTheme {
        DDayDetailScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            state = rememberDDayDetailFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
