package io.github.taetae98coding.diary.feature.search.ui.home.place

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import io.github.taetae98coding.diary.compose.place.PlaceListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomePlaceContent(
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent<Place, SearchHomePlaceViewModel>(
        type = SearchHomeType.PLACE,
        queryState = queryState,
        viewModelStoreProvider = viewModelStoreProvider,
        viewModel = { koinViewModel<SearchHomePlaceViewModel>() },
    ) { viewModel, state ->
        PlaceListUndoSnackbarEffect(
            onRestore = viewModel::restore,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomePlaceList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> state.sortSheetState.show()
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
            listState = state.listState,
            sortSheetState = state.sortSheetState,
            placePagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
            sortProvider = { state.uiState.sort },
        )
    }
}
