package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val DEFAULT_ADD_DESCRIPTION = "Add place"
internal const val DEFAULT_LIST_VIEW_MODE_DESCRIPTION = "Show list"
internal const val DEFAULT_MAP_VIEW_MODE_DESCRIPTION = "Show map"
internal const val DEFAULT_SEARCH_DESCRIPTION = "Search"
internal const val DEFAULT_MAP_PROVIDER_DESCRIPTION = "Map provider"
internal const val KOREAN_LIST_VIEW_MODE_DESCRIPTION = "목록으로 보기"
internal const val KOREAN_MAP_VIEW_MODE_DESCRIPTION = "지도로 보기"

@Composable
internal fun ViewModeTestPlaceHomeScaffold(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    state: PlaceHomeScaffoldState,
    uiState: PlaceHomeUiState,
    placePagingDataFlow: MutableStateFlow<PagingData<Place>>,
    isRefreshing: Boolean = false,
) {
    DiaryTheme {
        PlaceHomeScaffold(
            onEvent = onEvent,
            state = state,
            uiStateProvider = { uiState },
            placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
            syncUiStateProvider = { SyncRefreshUiState(isRefreshing = isRefreshing) },
        )
    }
}

internal fun placePagingDataFlowOf(placeList: List<Place>): MutableStateFlow<PagingData<Place>> =
    MutableStateFlow(
        PagingData.from(
            data = placeList,
            sourceLoadStates =
                LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
        ),
    )

internal fun List<PlaceHomeScaffoldEvent>.withoutMoveMap(): List<PlaceHomeScaffoldEvent> = filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap }

internal fun viewModeTestPlace(): Place {
    val id = Arb.kotlinUuid().next()
    val detail =
        Arb
            .placeDetail()
            .next()
            .copy(
                title = "제목-$id-${Arb.string().next()}",
            )

    return Place(
        id = id,
        detail = detail,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
    )
}

internal fun loadingPlacePagingDataFlow(): MutableStateFlow<PagingData<Place>> =
    MutableStateFlow(
        PagingData.from(
            data = emptyList(),
            sourceLoadStates =
                LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = false),
                    append = LoadState.NotLoading(endOfPaginationReached = false),
                ),
        ),
    )
