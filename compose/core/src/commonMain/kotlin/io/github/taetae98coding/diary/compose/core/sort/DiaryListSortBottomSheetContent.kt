@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.sort

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
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
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .selectableGroup()
                    .styleable(style = DiaryTheme.styles.bottomSheetContent),
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
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).styleable(style = DiaryTheme.styles.bottomSheetRow),
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
