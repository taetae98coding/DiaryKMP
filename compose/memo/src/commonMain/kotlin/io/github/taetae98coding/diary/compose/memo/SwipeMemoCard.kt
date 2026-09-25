package io.github.taetae98coding.diary.compose.memo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToFinishAndDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.core.model.memo.Memo
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeMemoCard(
    onEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    memo: Memo? = null,
    finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
) {
    val finishContentDescription =
        when (finishAction) {
            SwipeFinishAction.FINISH -> stringResource(Res.string.memo_list_swipe_finish_content_description)
            SwipeFinishAction.RESTART -> stringResource(Res.string.memo_list_swipe_restart_content_description)
        }

    SwipeToFinishAndDeleteBox(
        // key가 바뀌어야 스와이프 상태가 새로 만들어지므로 완료 여부를 함께 넣는다.
        key = memo?.let { value -> value.id to value.isFinished },
        onFinish = { memo?.let { value -> onEvent(value.swipeFinishEvent(finishAction = finishAction)) } },
        onDelete = { memo?.let { value -> onEvent(MemoListEvent.SwipeDelete(id = value.id)) } },
        finishContentDescription = finishContentDescription,
        deleteContentDescription = stringResource(Res.string.memo_list_swipe_delete_content_description),
        modifier = modifier,
        gesturesEnabled = memo != null,
        finishAction = finishAction,
    ) {
        MemoCard(
            memo = memo,
            onClick = { memo?.let { value -> onEvent(MemoListEvent.ClickMemo(id = value.id)) } },
        )
    }
}

private fun Memo.swipeFinishEvent(finishAction: SwipeFinishAction): MemoListEvent =
    when (finishAction) {
        SwipeFinishAction.FINISH -> MemoListEvent.SwipeFinish(id = id)
        SwipeFinishAction.RESTART -> MemoListEvent.SwipeRestart(id = id)
    }

@ComponentPreview
@Composable
private fun SwipeMemoCardPreview() {
    val memo = remember { previewMemo(title = "메모 제목", color = 0xFF3A7BD5) }

    DiaryTheme {
        SwipeMemoCard(
            onEvent = {},
            memo = memo,
        )
    }
}
