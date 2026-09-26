package io.github.taetae98coding.diary.feature.search.ui.home.place

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.place.PlaceListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryEffect
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomePlaceContent(
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = SearchHomeType.PLACE, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val viewModel = koinViewModel<SearchHomePlaceViewModel>()

        // 결과를 받기 전에 지금 질의를 먼저 알려, 다시 나타난 유형이 떠나기 전의 결과를 건너뛰게 한다.
        SearchHomeQueryEffect(
            queryState = queryState,
            onQueryShow = viewModel::showQuery,
            onQueryChange = viewModel::updateQuery,
        )

        val placePagingItems = viewModel.pagingData.collectAsLazyPagingItems()
        val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val sort by viewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        PlaceListUndoSnackbarEffect(
            onRestore = viewModel::restore,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeQueryScrollEffect(
            listState = listState,
            queryProvider = { appliedQuery },
        )
        ListQueryScrollEffect(
            listState = listState,
            sortProvider = { sort },
            itemListProvider = { placePagingItems.itemSnapshotList.items },
        )
        SearchHomePlaceList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> sortSheetState.show()
                    is SearchHomeResultEvent.SelectSort -> viewModel.select(sort = event.sort)
                }
            },
            onItemEvent = { event ->
                when (event) {
                    is SearchHomeResultItemEvent.Click -> navigateToDetail(event.id)
                    is SearchHomeResultItemEvent.SwipeDelete -> viewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = listState,
            sortSheetState = sortSheetState,
            placePagingItems = placePagingItems,
            query = appliedQuery,
            sortProvider = { sort },
        )
    }
}
