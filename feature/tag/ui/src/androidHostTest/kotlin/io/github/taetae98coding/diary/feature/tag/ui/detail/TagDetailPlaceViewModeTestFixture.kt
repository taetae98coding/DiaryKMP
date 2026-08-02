package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import kotlinx.coroutines.flow.MutableStateFlow

internal fun List<TagDetailPlaceContentEvent>.withoutMoveMap(): List<TagDetailPlaceContentEvent> = filterNot { event -> event is TagDetailPlaceContentEvent.MoveMap }

internal const val DEFAULT_SHOW_MAP_DESCRIPTION = "Show map"
internal const val DEFAULT_SHOW_LIST_DESCRIPTION = "Show list"
internal const val KOREAN_SHOW_MAP_DESCRIPTION = "지도로 보기"
internal const val KOREAN_SHOW_LIST_DESCRIPTION = "목록으로 보기"

@Composable
internal fun ViewModeTestTagDetailPlaceTab(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    state: TagDetailPlaceState,
    placePagingDataFlow: MutableStateFlow<PagingData<Place>>,
    uiState: TagDetailPlaceUiState = TagDetailPlaceUiState.Loading,
    placeListUiState: TagDetailPlaceListUiState = TagDetailPlaceListUiState(),
) {
    DiaryTheme {
        TagDetailPlaceTab(
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize(),
            state = state,
            uiStateProvider = { uiState },
            placeListUiStateProvider = { placeListUiState },
            placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
        )
    }
}
