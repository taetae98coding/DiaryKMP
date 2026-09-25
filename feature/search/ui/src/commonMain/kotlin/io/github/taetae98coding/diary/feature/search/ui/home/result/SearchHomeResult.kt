package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_empty_description
import io.github.taetae98coding.diary.feature.search.ui.search_home_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchHomeResult(
    onEvent: (SearchHomeResultEvent) -> Unit,
    emptyIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    query: String = "",
    pagingItems: LazyPagingItems<*> = remember { flowOf(PagingData.empty<Any>()) }.collectAsLazyPagingItems(),
    sortProvider: () -> ListSort = { ListSort.TITLE },
    list: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DiaryListSortBarHost(
            onClick = { onEvent(SearchHomeResultEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { pagingItems.itemCount > 0 },
        )

        DiaryCrossfade(
            targetState = query.isNotBlank() && pagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.search_home_empty_title),
                    description = stringResource(Res.string.search_home_empty_description),
                    icon = emptyIcon,
                )
            } else {
                list()
            }
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(SearchHomeResultEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun SearchHomeResultPreview() {
    DiaryTheme {
        SearchHomeResult(
            onEvent = {},
            emptyIcon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            query = "여행",
            list = {},
        )
    }
}
