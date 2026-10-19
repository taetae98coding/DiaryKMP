@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.list.sort

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
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.Res
import io.github.taetae98coding.diary.compose.list.list_sort_bottom_sheet_title
import io.github.taetae98coding.diary.core.model.list.ListSort
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryListSortBottomSheetContent(
    onSelect: (ListSort) -> Unit,
    modifier: Modifier = Modifier,
    sortList: List<ListSort> = listSortList,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    DiaryListSortBottomSheetContent(
        onSelect = onSelect,
        sortList = sortList,
        sortProvider = sortProvider,
        icon = { sort, iconModifier -> ListSortIcon(sort = sort, modifier = iconModifier) },
        label = ::listSortLabel,
        modifier = modifier,
    )
}

@Composable
internal fun <T> DiaryListSortBottomSheetContent(
    onSelect: (T) -> Unit,
    sortList: List<T>,
    sortProvider: () -> T,
    icon: @Composable (sort: T, modifier: Modifier) -> Unit,
    label: (T) -> StringResource,
    modifier: Modifier = Modifier,
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
                    sort = item,
                    icon = icon,
                    label = label,
                    modifier = Modifier.fillMaxWidth(),
                    isSelectedProvider = { item == sortProvider() },
                )
            }
        }
    }
}

@Composable
private fun <T> SortRow(
    onClick: () -> Unit,
    sort: T,
    icon: @Composable (sort: T, modifier: Modifier) -> Unit,
    label: (T) -> StringResource,
    modifier: Modifier = Modifier,
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
        icon(sort, Modifier.size(ButtonDefaults.IconSize))
        Spacer(modifier = Modifier.width(DiaryListSortBottomSheetContentDefaults.IconLabelSpacing))

        Text(
            text = stringResource(label(sort)),
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
