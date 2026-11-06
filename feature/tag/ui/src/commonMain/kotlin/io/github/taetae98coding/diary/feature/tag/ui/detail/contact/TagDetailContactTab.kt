package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.contact.ContactListEvent
import io.github.taetae98coding.diary.compose.contact.SwipeContactCard
import io.github.taetae98coding.diary.compose.contact.previewContact
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.list.sort.nameListSortList
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_contact_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_contact_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailContactTab(
    onEvent: (TagDetailContactContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contactPagingItems: LazyPagingItems<ContactListItem> = remember { flowOf(PagingData.empty<ContactListItem>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.NAME },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        filterProvider = scopeProvider,
        itemListProvider = { contactPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { contactPagingItems.loadState.refresh is LoadState.Loading },
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(TagDetailContactContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { contactPagingItems.itemCount > 0 },
        )

        DiaryCrossfade(
            targetState = contactPagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
                Empty(
                    onEvent = onEvent,
                    isRefreshingProvider = isRefreshingProvider,
                )
            } else {
                ContactGrid(
                    onEvent = onEvent,
                    gridState = gridState,
                    contactPagingItems = contactPagingItems,
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagDetailContactContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = nameListSortList,
        sortProvider = sortProvider,
    )
}

@Composable
private fun ContactGrid(
    onEvent: (TagDetailContactContentEvent) -> Unit,
    gridState: LazyStaggeredGridState,
    contactPagingItems: LazyPagingItems<ContactListItem>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableStaggeredGrid(
        onRefresh = { onEvent(TagDetailContactContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = contactPagingItems.itemCount,
            key = contactPagingItems.itemKey { item -> item.contact.id },
        ) { index ->
            val contact = contactPagingItems[index]

            SwipeContactCard(
                onEvent = { event ->
                    when (event) {
                        is ContactListEvent.ClickContact -> onEvent(TagDetailContactContentEvent.ClickContact(id = event.id))
                        is ContactListEvent.SwipeDelete -> onEvent(TagDetailContactContentEvent.DeleteContact(id = event.id))
                    }
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                contact = contact,
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailContactContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = { onEvent(TagDetailContactContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_contact_empty_title),
            // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            description = stringResource(Res.string.tag_detail_contact_empty_description),
            icon = { ContactIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailContactTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewContact(name = "김철수", phoneNumberList = listOf("010-1234-5678"))))) }

    DiaryTheme {
        Surface {
            TagDetailContactTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                contactPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
