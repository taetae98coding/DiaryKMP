package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_title
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistAddScaffold(
    onEvent: (ChecklistAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistAddFormState = rememberChecklistAddFormState(),
    uiStateProvider: () -> ChecklistAddUiState = { ChecklistAddUiState() },
    componentVisibleProvider: () -> ChecklistAddScaffoldComponentVisible = { ChecklistAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(ChecklistAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.checklist_add_title),
                onNavigateUp = { onEvent(ChecklistAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.checklist_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(ChecklistAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.checklist_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        ChecklistAddForm(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
        )
    }
}

@ScreenPreview
@Composable
private fun ChecklistAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        ChecklistAddScaffold(
            onEvent = {},
            uiStateProvider = { ChecklistAddUiState(isInProgress = isInProgress) },
        )
    }
}
