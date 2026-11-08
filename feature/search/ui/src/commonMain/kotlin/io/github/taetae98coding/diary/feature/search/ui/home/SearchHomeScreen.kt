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
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.UpdateDDayListTodayEffect
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.dday.SearchHomeDDayContent
import io.github.taetae98coding.diary.feature.search.ui.home.dday.SearchHomeDDayViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoContent
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceContent
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagContent
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebContent
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToDDayDetail: (Uuid) -> Unit,
    initialType: SearchHomeType,
    memoViewModel: SearchHomeMemoViewModel,
    tagViewModel: SearchHomeTagViewModel,
    placeViewModel: SearchHomePlaceViewModel,
    webViewModel: SearchHomeWebViewModel,
    ddayViewModel: SearchHomeDDayViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberSearchHomeScaffoldState(initialType = initialType)
    val snackbarHostState = remember { SnackbarHostState() }
    val ddayListState = rememberDDayListState()

    val memoPagingItems = memoViewModel.pagingData.collectAsLazyPagingItems()
    val tagPagingItems = tagViewModel.pagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.pagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.pagingData.collectAsLazyPagingItems()
    val ddayPagingItems = ddayViewModel.pagingData.collectAsLazyPagingItems()
    val memoListState = rememberSearchHomeResultListState(uiState = memoViewModel.uiState, pagingItems = memoPagingItems)
    val tagListState = rememberSearchHomeResultListState(uiState = tagViewModel.uiState, pagingItems = tagPagingItems)
    val placeListState = rememberSearchHomeResultListState(uiState = placeViewModel.uiState, pagingItems = placePagingItems)
    val webListState = rememberSearchHomeResultListState(uiState = webViewModel.uiState, pagingItems = webPagingItems)
    val ddayResultListState = rememberSearchHomeResultListState(uiState = ddayViewModel.uiState, pagingItems = ddayPagingItems)

    SearchHomeScreenEffect(
        state = state,
        snackbarHostState = snackbarHostState,
        ddayListState = ddayListState,
        onQueryChange = { query ->
            memoViewModel.updateQuery(query)
            tagViewModel.updateQuery(query)
            placeViewModel.updateQuery(query)
            webViewModel.updateQuery(query)
            ddayViewModel.updateQuery(query)
        },
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
            dday = SearchHomeTypeResult(viewModel = ddayViewModel, pagingItems = ddayPagingItems, listState = ddayResultListState, navigateToDetail = navigateToDDayDetail),
            ddayListState = ddayListState,
            snackbarHostState = snackbarHostState,
        )
    }
}

private class SearchHomeTypeResult<T : Any, VM : Any>(
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
    dday: SearchHomeTypeResult<DDay, SearchHomeDDayViewModel>,
    ddayListState: DDayListState,
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

        SearchHomeType.DDAY ->
            SearchHomeDDayContent(
                viewModel = dday.viewModel,
                pagingItems = dday.pagingItems,
                listState = dday.listState,
                ddayListState = ddayListState,
                snackbarHostState = snackbarHostState,
                navigateToDetail = dday.navigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )
    }
}

@Composable
private fun SearchHomeScreenEffect(
    state: SearchHomeScaffoldState,
    snackbarHostState: SnackbarHostState,
    ddayListState: DDayListState,
    onQueryChange: (String) -> Unit,
) {
    RequestFocusEffect(focusRequester = state.focusRequester)
    UpdateDDayListTodayEffect(state = ddayListState)
    DismissUndoSnackbarEffect(keyProvider = { state.type }, hostState = snackbarHostState)
    SearchHomeQueryEffect(
        queryState = state.queryState,
        onQueryChange = onQueryChange,
    )
}

// 결과 영역은 보고 있는 유형만 구성하므로, 유형마다 보던 자리와 질의·정렬 변경에 따른 스크롤을 화면에서 둔다.
@Composable
private fun <T : Any> rememberSearchHomeResultListState(
    uiState: StateFlow<SearchHomeResultUiState>,
    pagingItems: LazyPagingItems<T>,
): LazyListState {
    val listState = rememberLazyListState()

    SearchHomeResultScrollEffect(uiState = uiState, pagingItems = pagingItems, listState = listState)
    return listState
}
