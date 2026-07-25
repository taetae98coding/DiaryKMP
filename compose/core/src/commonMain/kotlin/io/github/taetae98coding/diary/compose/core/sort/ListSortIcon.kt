package io.github.taetae98coding.diary.compose.core.sort

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.HistoryIcon
import io.github.taetae98coding.diary.compose.core.icon.SortByAlphaIcon
import io.github.taetae98coding.diary.compose.core.icon.SortIcon
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort

// 이름이 현재 정렬을 알리므로 아이콘에는 접근성 이름을 두지 않는다.
@Composable
public fun ListSortIcon(
    sort: ListSort,
    modifier: Modifier = Modifier,
) {
    when (sort) {
        ListSort.DEFAULT -> SortIcon(modifier = modifier)
        ListSort.TITLE -> SortByAlphaIcon(modifier = modifier)
        ListSort.NAME -> SortByAlphaIcon(modifier = modifier)
        ListSort.RECENTLY_UPDATED -> HistoryIcon(modifier = modifier)
    }
}

@IconPreview
@Composable
private fun ListSortIconPreview() {
    DiaryTheme {
        ListSortIcon(sort = ListSort.TITLE)
    }
}
