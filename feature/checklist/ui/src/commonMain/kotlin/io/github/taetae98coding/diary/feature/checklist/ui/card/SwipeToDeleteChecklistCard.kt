package io.github.taetae98coding.diary.feature.checklist.ui.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_delete_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklistSummary
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwipeToDeleteChecklistCard(
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    summary: ChecklistSummary? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.checklist_home_delete_content_description),
        onDelete = onDelete,
        modifier = modifier,
        key = summary?.checklist?.id,
        gesturesEnabled = summary != null,
    ) {
        ChecklistCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            summary = summary,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeleteChecklistCardPreview() {
    DiaryTheme {
        SwipeToDeleteChecklistCard(
            onClick = {},
            onDelete = {},
            summary = previewChecklistSummary(title = "여행 준비"),
        )
    }
}
