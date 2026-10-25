package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.CheckBoxOutlineBlankIcon
import io.github.taetae98coding.diary.compose.core.icon.FormatListNumberedIcon
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort

@Composable
internal fun ChecklistItemSortIcon(
    sort: ChecklistItemSort,
    modifier: Modifier = Modifier,
) {
    when (sort) {
        ChecklistItemSort.ADDED -> FormatListNumberedIcon(modifier = modifier)
        ChecklistItemSort.UNCHECKED_FIRST -> CheckBoxOutlineBlankIcon(modifier = modifier)
    }
}

@IconPreview
@Composable
private fun ChecklistItemSortIconPreview() {
    DiaryTheme {
        ChecklistItemSortIcon(sort = ChecklistItemSort.ADDED)
    }
}
