package io.github.taetae98coding.diary.compose.routine

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToFinishAndDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeRoutineCard(
    onEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineListState = rememberRoutineListState(),
    summary: RoutineSummary? = null,
    quickRecord: (@Composable (RoutineSummary) -> Unit)? = null,
) {
    val routine = summary?.routine
    val finishAction = if (routine?.isFinished == true) SwipeFinishAction.RESTART else SwipeFinishAction.FINISH
    val finishContentDescription =
        when (finishAction) {
            SwipeFinishAction.FINISH -> stringResource(Res.string.routine_list_finish_content_description)
            SwipeFinishAction.RESTART -> stringResource(Res.string.routine_list_restart_content_description)
        }

    SwipeToFinishAndDeleteBox(
        // key가 바뀌어야 스와이프 상태가 새로 만들어지므로 완료 여부를 함께 넣는다.
        key = routine?.let { value -> value.id to value.isFinished },
        onFinish = { routine?.let { value -> onEvent(value.swipeFinishEvent()) } },
        onDelete = { routine?.let { value -> onEvent(RoutineListEvent.SwipeDelete(id = value.id)) } },
        finishContentDescription = finishContentDescription,
        deleteContentDescription = stringResource(Res.string.routine_list_delete_content_description),
        modifier = modifier,
        gesturesEnabled = summary != null,
        finishAction = finishAction,
    ) {
        RoutineCard(
            onClick = { routine?.let { value -> onEvent(RoutineListEvent.ClickRoutine(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            state = state,
            summary = summary,
            quickRecord = quickRecord,
        )
    }
}

private fun Routine.swipeFinishEvent(): RoutineListEvent = if (isFinished) RoutineListEvent.SwipeRestart(id = id) else RoutineListEvent.SwipeFinish(id = id)

@ComponentPreview
@Composable
private fun SwipeRoutineCardPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFinished: Boolean,
) {
    DiaryTheme {
        SwipeRoutineCard(
            onEvent = {},
            state = rememberRoutineListState(initialToday = previewRoutineToday()),
            summary = previewRoutineSummary(title = "물 마시기", isFinished = isFinished),
        )
    }
}
