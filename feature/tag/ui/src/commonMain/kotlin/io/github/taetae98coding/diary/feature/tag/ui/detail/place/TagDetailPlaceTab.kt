package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun TagDetailPlaceTab(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: TagDetailPlaceState = rememberTagDetailPlaceState(),
    sortSheetState: DialogState = rememberDialogState(),
    uiStateProvider: () -> TagDetailPlaceUiState = { TagDetailPlaceUiState.Loading },
    placeListUiStateProvider: () -> TagDetailPlaceListUiState = { TagDetailPlaceListUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    val uiState = uiStateProvider()
    val mapState = rememberTagDetailPlaceMapState(uiState = uiState)

    TagDetailPlaceMoveMapEffect(
        onEvent = onEvent,
        mapState = mapState,
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(TagDetailPlaceContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = {
                when (state.viewMode) {
                    TagDetailPlaceViewMode.LIST -> placePagingItems.itemCount > 0
                    TagDetailPlaceViewMode.MAP -> placeListUiStateProvider().placeList.isNotEmpty()
                }
            },
            trailing = {
                TagDetailPlaceViewModeButton(
                    onClick = state::toggleViewMode,
                    viewModeProvider = { state.viewMode },
                )
            },
        )

        DiaryCrossfade(
            targetState = state.viewMode,
            modifier = Modifier.fillMaxSize(),
        ) { viewMode ->
            when (viewMode) {
                TagDetailPlaceViewMode.LIST ->
                    TagDetailPlacePagingList(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        placePagingItems = placePagingItems,
                        isRefreshingProvider = isRefreshingProvider,
                        sortProvider = sortProvider,
                    )

                TagDetailPlaceViewMode.MAP ->
                    when (uiState) {
                        is TagDetailPlaceUiState.Loading -> Unit

                        is TagDetailPlaceUiState.Loaded ->
                            TagDetailPlaceMapContent(
                                onEvent = onEvent,
                                modifier = Modifier.fillMaxSize(),
                                mapState = mapState,
                                placeListUiStateProvider = placeListUiStateProvider,
                                isRefreshingProvider = isRefreshingProvider,
                                sortProvider = sortProvider,
                            )
                    }
            }
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagDetailPlaceContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun TagDetailPlaceTabPreview(
    @PreviewParameter(TagDetailPlaceViewModePreviewParameter::class) viewMode: TagDetailPlaceViewMode,
) {
    DiaryTheme {
        Surface {
            TagDetailPlaceTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                state = TagDetailPlaceState(initialViewMode = viewMode),
            )
        }
    }
}
