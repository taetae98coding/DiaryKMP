package io.github.taetae98coding.diary.feature.search.ui.home.web

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
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.web.WebListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeWebContent(
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = SearchHomeType.WEB, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val viewModel = koinViewModel<SearchHomeWebViewModel>()
        val webPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
        val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val sort by viewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        WebListUndoSnackbarEffect(
            onRestore = viewModel::restore,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        DiarySearchQueryEffect(
            queryState = queryState,
            onQueryChange = viewModel::updateQuery,
        )
        SearchHomeQueryScrollEffect(
            listState = listState,
            queryProvider = { appliedQuery },
        )
        ListQueryScrollEffect(
            listState = listState,
            sortProvider = { sort },
            itemListProvider = { webPagingItems.itemSnapshotList.items },
        )
        SearchHomeWebList(
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
            webPagingItems = webPagingItems,
            query = appliedQuery,
            sortProvider = { sort },
        )
    }
}
