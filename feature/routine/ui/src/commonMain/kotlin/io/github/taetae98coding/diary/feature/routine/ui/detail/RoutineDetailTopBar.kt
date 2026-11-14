package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarActionScope
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.previewRoutineDetail
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_finish_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_restart_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun RoutineDetailTopBar(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> RoutineDetailUiState = { RoutineDetailUiState.Loading },
    componentVisibleProvider: () -> RoutineDetailScaffoldComponentVisible = { RoutineDetailScaffoldComponentVisible() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.routine_navigate_up_button_content_description)
    val deleteContentDescription = stringResource(Res.string.routine_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is RoutineDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(RoutineDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is RoutineDetailUiState.Content) {
                RoutineDetailFinishAction(content = uiState, onEvent = onEvent)
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(RoutineDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@Composable
private fun DiaryTopBarActionScope.RoutineDetailFinishAction(
    content: RoutineDetailUiState.Content,
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
) {
    ToggleAction(
        icon = DiaryTopBarIcon.FINISH,
        contentDescription =
            if (content.isFinished) {
                stringResource(Res.string.routine_detail_restart_button_content_description)
            } else {
                stringResource(Res.string.routine_detail_finish_button_content_description)
            },
        isChecked = content.isFinished,
        onClick = {
            if (content.isFinished) {
                onEvent(RoutineDetailScaffoldEvent.ClickRestart)
            } else {
                onEvent(RoutineDetailScaffoldEvent.ClickFinish)
            }
        },
        isInProgress = content.isFinishInProgress,
    )
}

@ComponentPreview
@Composable
private fun RoutineDetailTopBarPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFinished: Boolean,
) {
    DiaryTheme {
        RoutineDetailTopBar(
            onEvent = {},
            uiStateProvider = { RoutineDetailUiState.Content(id = Uuid.NIL, detail = previewRoutineDetail(title = "물 마시기"), isFinished = isFinished) },
        )
    }
}
