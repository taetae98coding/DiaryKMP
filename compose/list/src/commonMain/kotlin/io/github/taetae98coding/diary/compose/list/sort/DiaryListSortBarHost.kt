package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort

@Composable
public fun DiaryListSortBarHost(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    isSortVisibleProvider: () -> Boolean = { true },
    trailing: @Composable (() -> Unit)? = null,
) {
    if (isSortVisibleProvider()) {
        DiaryListSortBar(
            onClick = onClick,
            modifier = modifier,
            sortProvider = sortProvider,
            trailing = trailing,
        )
    } else if (trailing != null) {
        DiaryListSortBarTrailing(
            modifier = modifier,
            trailing = trailing,
        )
    }
}

@Composable
private fun DiaryListSortBarTrailing(
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.padding(horizontal = DiaryTheme.dimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        trailing()
    }
}

@ComponentPreview
@Composable
private fun DiaryListSortBarHostPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isSortVisible: Boolean,
) {
    DiaryTheme {
        Surface {
            DiaryListSortBarHost(
                onClick = {},
                isSortVisibleProvider = { isSortVisible },
                trailing = { TextButton(onClick = {}) { Text(text = "Trailing") } },
            )
        }
    }
}
