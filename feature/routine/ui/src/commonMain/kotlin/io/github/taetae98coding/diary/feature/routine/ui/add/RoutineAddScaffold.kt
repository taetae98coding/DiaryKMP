package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineForm
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormEvent
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineAddScaffold(
    onEvent: (RoutineAddScaffoldEvent) -> Unit,
    onFormEvent: (RoutineFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
    scrollState: ScrollState = rememberScrollState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> RoutineAddUiState = { RoutineAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> RoutineAddScaffoldComponentVisible = { RoutineAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(RoutineAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.routine_add_title),
                onNavigateUp = { onEvent(RoutineAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.routine_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(RoutineAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.routine_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        RoutineForm(
            onEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .systemScroll(scrollState) { scrollState.animateScrollTo(0) }
                    .verticalScroll(scrollState)
                    .padding(
                        start = DiaryTheme.dimens.screenHorizontalPadding,
                        top = DiaryTheme.dimens.screenVerticalPadding,
                        end = DiaryTheme.dimens.screenHorizontalPadding,
                        bottom = DiaryTheme.dimens.floatingActionButtonClearance,
                    ),
            state = state,
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
private fun RoutineAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        RoutineAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { RoutineAddUiState(isInProgress = isInProgress) },
        )
    }
}
