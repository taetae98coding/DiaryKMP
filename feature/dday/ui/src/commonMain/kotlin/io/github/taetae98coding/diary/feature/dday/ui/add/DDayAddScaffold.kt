package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
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
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_add_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_add_title
import io.github.taetae98coding.diary.feature.dday.ui.dday_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayForm
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormEvent
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayPresetRow
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayAddFormState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayAddScaffold(
    onEvent: (DDayAddScaffoldEvent) -> Unit,
    onFormEvent: (DDayFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> DDayAddUiState = { DDayAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> DDayAddScaffoldComponentVisible = { DDayAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(DDayAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.dday_add_title),
                onNavigateUp = { onEvent(DDayAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.dday_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(DDayAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.dday_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .systemScroll(state.scrollState) { state.scrollState.animateScrollTo(0) }
                    .verticalScroll(state.scrollState)
                    .padding(
                        start = DiaryTheme.dimens.screenHorizontalPadding,
                        top = DiaryTheme.dimens.screenVerticalPadding,
                        end = DiaryTheme.dimens.screenHorizontalPadding,
                        bottom = DiaryTheme.dimens.floatingActionButtonClearance,
                    ),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        ) {
            DDayPresetRow(
                modifier = Modifier.fillMaxWidth(),
                state = state,
            )
            DDayForm(
                onEvent = onFormEvent,
                modifier = Modifier.fillMaxWidth(),
                state = state,
                tagUiStateProvider = tagUiStateProvider,
            )
        }
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
private fun DDayAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        DDayAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { DDayAddUiState(isInProgress = isInProgress) },
        )
    }
}
