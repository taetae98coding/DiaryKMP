package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.contact.ui.previewContact
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal const val CONTACT_HOME_LIST_TEST_TAG: String = "ContactHomeList"

@Composable
internal fun ContactHomeList(
    onEvent: (ContactHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.NAME },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { contactPagingItems.itemSnapshotList.items },
    )

    DiaryCrossfade(
        targetState = contactPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(ContactHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                ContactHomeEmpty(
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                )
            }
        } else {
            DiaryRefreshableStaggeredGrid(
                onRefresh = { onEvent(ContactHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = CONTACT_HOME_LIST_TEST_TAG,
            ) {
                items(
                    count = contactPagingItems.itemCount,
                    key = contactPagingItems.itemKey { contact -> contact.id },
                ) { index ->
                    val contact = contactPagingItems[index]

                    ContactCard(
                        onClick = { contact?.let { value -> onEvent(ContactHomeScaffoldEvent.ClickContact(id = value.id)) } },
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
            modifier = Modifier.fillMaxSize(),
            contactPagingItems = contactPagingData.collectAsLazyPagingItems(),
        )
    }
}
