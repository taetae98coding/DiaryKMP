package io.github.taetae98coding.diary.compose.checklist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeChecklistCard(
    onEvent: (ChecklistListEvent) -> Unit,
    modifier: Modifier = Modifier,
    summary: ChecklistSummary? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.checklist_list_delete_content_description),
        onDelete = { summary?.let { value -> onEvent(ChecklistListEvent.SwipeDelete(id = value.checklist.id)) } },
        modifier = modifier,
        key = summary?.checklist?.id,
        gesturesEnabled = summary != null,
    ) {
        ChecklistCard(
            onClick = { summary?.let { value -> onEvent(ChecklistListEvent.ClickChecklist(id = value.checklist.id)) } },
            modifier = Modifier.fillMaxWidth(),
            summary = summary,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeChecklistCardPreview() {
    DiaryTheme {
        SwipeChecklistCard(
            onEvent = {},
            summary = previewChecklistSummary(title = "여행 준비"),
        )
    }
}
