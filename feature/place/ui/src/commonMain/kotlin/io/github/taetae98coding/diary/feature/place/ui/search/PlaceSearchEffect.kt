package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toCoordinateBounds
import io.github.taetae98coding.diary.compose.place.toMapProvider
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun PlaceSearchEffect(
    state: PlaceSearchDialogState,
    onSearch: (PlaceSearchRequest) -> Unit,
    onClear: () -> Unit,
) {
    val latestOnSearch by rememberUpdatedState(onSearch)
    val latestOnClear by rememberUpdatedState(onClear)

    LaunchedEffect(state) {
        snapshotFlow { state.mapState.bounds }
            .collect { bounds -> state.updateSearchBounds(bounds = bounds, center = state.mapState.coordinate) }
    }

    LaunchedEffect(state) {
        var previousProvider: DiaryMapProvider? = null

        snapshotFlow {
            val provider = state.mapState.provider

            PlaceSearchTrigger(
                query = state.query,
                provider = provider,
                searchBounds = state.searchBounds.takeIf { provider == DiaryMapProvider.GOOGLE },
            )
        }.collectLatest { trigger ->
            // 효과가 막 시작됐는데 검색어가 이미 있으면 화면이 복원되어 검색어가 다시 나타난 것이다.
            val isRestored = previousProvider == null
            val isProviderChanged = previousProvider != null && previousProvider != trigger.provider

            previousProvider = trigger.provider

            if (trigger.query.isBlank()) {
                latestOnClear()
                return@collectLatest
            }

            if (!isRestored && !isProviderChanged) delay(INPUT_IDLE_DELAY)

            latestOnSearch(
                PlaceSearchRequest(
                    query = trigger.query,
                    provider = trigger.provider.toMapProvider(),
                    // 목록의 장소로 옮긴 영역은 다시 검색을 일으키지 않을 뿐, 검색할 때는 사용자가 지금 보고 있는 영역을 기준으로 삼는다.
                    bounds =
                        state.mapState.bounds
                            .takeIf { trigger.provider == DiaryMapProvider.GOOGLE }
                            ?.toCoordinateBounds(),
                ),
            )
        }
    }
}

private data class PlaceSearchTrigger(
    val query: String,
    val provider: DiaryMapProvider,
    val searchBounds: DiaryMapBounds?,
)
