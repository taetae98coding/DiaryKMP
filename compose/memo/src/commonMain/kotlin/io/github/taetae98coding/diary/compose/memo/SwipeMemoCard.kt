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
internal fun SwipeMemoCard(
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
        key = memo?.id,
        onFinish = { memo?.let { value -> onEvent(MemoListEvent.SwipeFinish(id = value.id)) } },
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
