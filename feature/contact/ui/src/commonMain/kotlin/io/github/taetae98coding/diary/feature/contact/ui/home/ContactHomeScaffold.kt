package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.list.sort.nameListSortList
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_add_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_title
import io.github.taetae98coding.diary.feature.contact.ui.contact_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHomeScaffold(
    onEvent: (ContactHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> ContactHomeUiState = { ContactHomeUiState() },
    sortProvider: () -> ListSort = { ListSort.NAME },
    componentVisibleProvider: () -> ContactHomeScaffoldComponentVisible = { ContactHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.contact_home_title),
                onNavigateUp = { onEvent(ContactHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.contact_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(ContactHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.contact_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBarHost(
                onClick = { onEvent(ContactHomeScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { contactPagingItems.itemCount > 0 },
            )

            ContactHomeList(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                contactPagingItems = contactPagingItems,
                isRefreshingProvider = { uiStateProvider().isRefreshing },
                sortProvider = sortProvider,
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(ContactHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = nameListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun ContactHomeScaffoldPreview() {
    DiaryTheme {
        ContactHomeScaffold(onEvent = {})
    }
}
