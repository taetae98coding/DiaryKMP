package io.github.taetae98coding.diary.compose.core.sort

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.list_sort_bottom_sheet_title
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import org.jetbrains.compose.resources.stringResource

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val CONTENT_BOTTOM_PADDING = 16.dp
private val TITLE_VERTICAL_PADDING = 12.dp
private val ROW_MIN_HEIGHT = 56.dp
private val ROW_SPACING = 16.dp

@Composable
public fun DiaryListSortBottomSheetContent(
    onSelect: (ListSort) -> Unit,
    modifier: Modifier = Modifier,
    sortList: List<ListSort> = listSortList,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.list_sort_bottom_sheet_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = TITLE_VERTICAL_PADDING),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(bottom = CONTENT_BOTTOM_PADDING),
        ) {
            sortList.forEach { item ->
                SortRow(
                    onClick = { onSelect(item) },
                    modifier = Modifier.fillMaxWidth(),
                    sort = item,
                    isSelectedProvider = { item == sortProvider() },
                )
            }
        }
    }
}

@Composable
private fun SortRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sort: ListSort = ListSort.DEFAULT,
    isSelectedProvider: () -> Boolean = { false },
) {
    val isSelected = isSelectedProvider()

    Row(
        modifier =
            modifier
                .heightIn(min = ROW_MIN_HEIGHT)
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(horizontal = CONTENT_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ListSortIcon(
            sort = sort,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ROW_SPACING))

        Text(
            text = stringResource(listSortLabel(sort = sort)),
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.bodyLarge,
        )

        RadioButton(
            selected = isSelected,
            onClick = null,
        )
    }
}

@ScreenPreview
@Composable
private fun DiaryListSortBottomSheetContentPreview(
    @PreviewParameter(ListSortPreviewParameter::class) sort: ListSort,
) {
    DiaryTheme {
        DiaryListSortBottomSheetContent(
            onSelect = {},
            sortList = memoListSortList,
            sortProvider = { sort },
        )
    }
}
