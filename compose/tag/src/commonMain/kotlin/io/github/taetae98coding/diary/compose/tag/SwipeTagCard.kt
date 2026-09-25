package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToFinishAndDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeTagCard(
    onEvent: (TagListEvent) -> Unit,
    modifier: Modifier = Modifier,
    tag: Tag? = null,
    finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
) {
    val finishContentDescription =
        when (finishAction) {
            SwipeFinishAction.FINISH -> stringResource(Res.string.tag_list_swipe_finish_content_description)
            SwipeFinishAction.RESTART -> stringResource(Res.string.tag_list_swipe_restart_content_description)
        }

    SwipeToFinishAndDeleteBox(
        // key가 바뀌어야 스와이프 상태가 새로 만들어지므로 완료 여부를 함께 넣는다.
        key = tag?.let { value -> value.id to value.isFinished },
        onFinish = { tag?.let { value -> onEvent(value.swipeFinishEvent(finishAction = finishAction)) } },
        onDelete = { tag?.let { value -> onEvent(TagListEvent.SwipeDelete(id = value.id)) } },
        finishContentDescription = finishContentDescription,
        deleteContentDescription = stringResource(Res.string.tag_list_swipe_delete_content_description),
        modifier = modifier,
        gesturesEnabled = tag != null,
        finishAction = finishAction,
    ) {
        TagCard(
            onClick = { tag?.let { value -> onEvent(TagListEvent.ClickTag(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            tag = tag,
        )
    }
}

private fun Tag.swipeFinishEvent(finishAction: SwipeFinishAction): TagListEvent =
    when (finishAction) {
        SwipeFinishAction.FINISH -> TagListEvent.SwipeFinish(id = id)
        SwipeFinishAction.RESTART -> TagListEvent.SwipeRestart(id = id)
    }

@ComponentPreview
@Composable
private fun SwipeTagCardPreview() {
    val tag = remember { previewTag(emoji = "🏃", title = "태그 제목", color = 0xFF3A7BD5) }

    DiaryTheme {
        SwipeTagCard(
            onEvent = {},
            tag = tag,
        )
    }
}
