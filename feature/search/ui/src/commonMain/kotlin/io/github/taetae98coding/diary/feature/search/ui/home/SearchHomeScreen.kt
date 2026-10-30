package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoContent
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceContent
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultScrollEffect
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
    val memoListState = rememberSearchHomeResultListState(viewModel = memoViewModel, pagingItems = memoPagingItems)
    val tagListState = rememberSearchHomeResultListState(viewModel = tagViewModel, pagingItems = tagPagingItems)
    val placeListState = rememberSearchHomeResultListState(viewModel = placeViewModel, pagingItems = placePagingItems)
    val webListState = rememberSearchHomeResultListState(viewModel = webViewModel, pagingItems = webPagingItems)

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
        SearchHomeTypeContent(
            type = type,
            memo = SearchHomeTypeResult(viewModel = memoViewModel, pagingItems = memoPagingItems, listState = memoListState, navigateToDetail = navigateToMemoDetail),
            tag = SearchHomeTypeResult(viewModel = tagViewModel, pagingItems = tagPagingItems, listState = tagListState, navigateToDetail = navigateToTagDetail),
            place = SearchHomeTypeResult(viewModel = placeViewModel, pagingItems = placePagingItems, listState = placeListState, navigateToDetail = navigateToPlaceDetail),
            web = SearchHomeTypeResult(viewModel = webViewModel, pagingItems = webPagingItems, listState = webListState, navigateToDetail = navigateToWebDetail),
            snackbarHostState = snackbarHostState,
        )
    }
}

private class SearchHomeTypeResult<T : Any, VM : SearchHomeResultViewModel<T>>(
    val viewModel: VM,
    val pagingItems: LazyPagingItems<T>,
    val listState: LazyListState,
    val navigateToDetail: (Uuid) -> Unit,
)

@Composable
private fun SearchHomeTypeContent(
    type: SearchHomeType,
    memo: SearchHomeTypeResult<Memo, SearchHomeMemoViewModel>,
    tag: SearchHomeTypeResult<Tag, SearchHomeTagViewModel>,
    place: SearchHomeTypeResult<Place, SearchHomePlaceViewModel>,
    web: SearchHomeTypeResult<Web, SearchHomeWebViewModel>,
    snackbarHostState: SnackbarHostState,
) {
    when (type) {
        SearchHomeType.MEMO ->
            SearchHomeMemoContent(
                viewModel = memo.viewModel,
                pagingItems = memo.pagingItems,
                listState = memo.listState,
                snackbarHostState = snackbarHostState,
                navigateToDetail = memo.navigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )

        SearchHomeType.TAG ->
            SearchHomeTagContent(
                viewModel = tag.viewModel,
                pagingItems = tag.pagingItems,
                listState = tag.listState,
                snackbarHostState = snackbarHostState,
                navigateToDetail = tag.navigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )

        SearchHomeType.PLACE ->
            SearchHomePlaceContent(
                viewModel = place.viewModel,
                pagingItems = place.pagingItems,
                listState = place.listState,
                snackbarHostState = snackbarHostState,
                navigateToDetail = place.navigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )

        SearchHomeType.WEB ->
            SearchHomeWebContent(
                viewModel = web.viewModel,
                pagingItems = web.pagingItems,
                listState = web.listState,
                snackbarHostState = snackbarHostState,
                navigateToDetail = web.navigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )
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

// 결과 영역은 보고 있는 유형만 구성하므로, 유형마다 보던 자리와 질의·정렬 변경에 따른 스크롤을 화면에서 둔다.
@Composable
private fun <T : Any> rememberSearchHomeResultListState(
    viewModel: SearchHomeResultViewModel<T>,
    pagingItems: LazyPagingItems<T>,
): LazyListState {
    val listState = rememberLazyListState()

    SearchHomeResultScrollEffect(viewModel = viewModel, pagingItems = pagingItems, listState = listState)
    return listState
}
