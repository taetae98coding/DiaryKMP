package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort

@Composable
public fun DiaryChecklistItemSortBarHost(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sortProvider: () -> ChecklistItemSort = { ChecklistItemSort.ADDED },
    isSortVisibleProvider: () -> Boolean = { true },
) {
    if (!isSortVisibleProvider()) return

    DiaryListSortBar(
        onClick = onClick,
        sortProvider = sortProvider,
        icon = { sort, iconModifier -> ChecklistItemSortIcon(sort = sort, modifier = iconModifier) },
        label = ::checklistItemSortLabel,
        modifier = modifier,
    )
}

private class ChecklistItemSortPreviewParameter : PreviewParameterProvider<ChecklistItemSort> {
    override val values: Sequence<ChecklistItemSort> = checklistItemSortList.asSequence()
}

@ComponentPreview
@Composable
private fun DiaryChecklistItemSortBarHostPreview(
    @PreviewParameter(ChecklistItemSortPreviewParameter::class) sort: ChecklistItemSort,
) {
    DiaryTheme {
        Surface {
            DiaryChecklistItemSortBarHost(
                onClick = {},
                sortProvider = { sort },
            )
        }
    }
}
