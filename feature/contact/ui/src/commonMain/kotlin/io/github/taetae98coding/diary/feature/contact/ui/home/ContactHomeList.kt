package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Composable
internal fun ContactHomeList(
    onEvent: (ContactHomeScaffoldEvent) -> Unit,
    onContactListEvent: (ContactListEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contactPagingItems: LazyPagingItems<ContactListItem> = remember { flowOf(PagingData.empty<ContactListItem>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    isAddButtonVisibleProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.NAME },
    filterUiStateProvider: () -> ContactHomeFilterUiState = { ContactHomeFilterUiState() },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        filterProvider = { filterUiStateProvider().filter },
        itemListProvider = { contactPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { contactPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = contactPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryRefreshableEmptyBox(
                onRefresh = { onEvent(ContactHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = isRefreshingProvider,
            ) {
                ContactHomeEmpty(
                    modifier = Modifier.fillMaxSize(),
                    isFilterAppliedProvider = { filterUiStateProvider().isApplied },
                )
            }
        } else {
            DiaryRefreshableStaggeredGrid(
                onRefresh = { onEvent(ContactHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                bottomPadding = if (isAddButtonVisibleProvider()) DiaryTheme.dimens.floatingActionButtonClearance else DiaryTheme.dimens.screenVerticalPadding,
            ) {
                items(
                    count = contactPagingItems.itemCount,
                    key = contactPagingItems.itemKey { item -> item.contact.id },
                ) { index ->
                    val contact = contactPagingItems[index]

                    SwipeContactCard(
                        onEvent = onContactListEvent,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        contact = contact,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun ContactHomeListPreview() {
    val contactList =
        remember {
            listOf(
                previewContact(
                    name = "김철수",
                    phoneNumberList = listOf("010-1234-5678"),
                    birthday = ContactBirthday(date = LocalDate(1990, 3, 4), calendar = ContactBirthdayCalendar.SOLAR),
                    isFavorite = true,
                ),
                previewContact(name = "이영희", phoneNumberList = emptyList()),
            )
        }
    val contactPagingData = remember(contactList) { flowOf(PagingData.from(contactList)) }

    DiaryTheme {
        ContactHomeList(
            onEvent = {},
            onContactListEvent = {},
            modifier = Modifier.fillMaxSize(),
            contactPagingItems = contactPagingData.collectAsLazyPagingItems(),
        )
    }
}
