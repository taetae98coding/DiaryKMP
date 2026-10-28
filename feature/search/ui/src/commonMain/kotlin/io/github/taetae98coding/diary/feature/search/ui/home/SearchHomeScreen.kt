package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoContent
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceContent
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagContent
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebContent
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    initialType: SearchHomeType,
    memoViewModel: SearchHomeMemoViewModel,
    tagViewModel: SearchHomeTagViewModel,
    placeViewModel: SearchHomePlaceViewModel,
    webViewModel: SearchHomeWebViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberSearchHomeScaffoldState(initialType = initialType)
    val snackbarHostState = remember { SnackbarHostState() }

    val memoPagingItems = memoViewModel.pagingData.collectAsLazyPagingItems()
    val tagPagingItems = tagViewModel.pagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.pagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.pagingData.collectAsLazyPagingItems()

    SearchHomeScreenEffect(
        state = state,
        snackbarHostState = snackbarHostState,
        viewModelList = listOf(memoViewModel, tagViewModel, placeViewModel, webViewModel),
    )

    SearchHomeScaffold(
        onEvent = { event ->
            when (event) {
                is SearchHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
            }
        },
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
    ) { type ->
        when (type) {
            SearchHomeType.MEMO ->
                SearchHomeMemoContent(
                    viewModel = memoViewModel,
                    pagingItems = memoPagingItems,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToMemoDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.TAG ->
                SearchHomeTagContent(
                    viewModel = tagViewModel,
                    pagingItems = tagPagingItems,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToTagDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.PLACE ->
                SearchHomePlaceContent(
                    viewModel = placeViewModel,
                    pagingItems = placePagingItems,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToPlaceDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.WEB ->
                SearchHomeWebContent(
                    viewModel = webViewModel,
                    pagingItems = webPagingItems,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToWebDetail,
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}

@Composable
private fun SearchHomeScreenEffect(
    state: SearchHomeScaffoldState,
    snackbarHostState: SnackbarHostState,
    viewModelList: List<SearchHomeResultViewModel<*>>,
) {
    RequestFocusEffect(focusRequester = state.focusRequester)
    DismissUndoSnackbarEffect(keyProvider = { state.type }, hostState = snackbarHostState)
    SearchHomeQueryEffect(
        queryState = state.queryState,
        onQueryChange = { query -> viewModelList.forEach { viewModel -> viewModel.updateQuery(query) } },
    )
}
