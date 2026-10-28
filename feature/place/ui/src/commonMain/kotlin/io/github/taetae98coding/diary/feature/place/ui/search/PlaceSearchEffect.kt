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
import kotlinx.coroutines.flow.drop

@Composable
internal fun PlaceSearchEffect(
    state: PlaceSearchDialogState,
    onSearch: (PlaceSearchRequest) -> Unit,
    onClear: () -> Unit,
) {
    val latestOnSearch by rememberUpdatedState(onSearch)
    val latestOnClear by rememberUpdatedState(onClear)

    LaunchedEffect(state) {
        snapshotFlow { state.mapState.provider }
            .drop(1)
            .collect { latestOnClear() }
    }

    LaunchedEffect(state) {
        snapshotFlow {
            val provider = state.mapState.provider

            PlaceSearchTrigger(
                query = state.query,
                provider = provider,
                bounds = state.mapState.bounds.takeIf { provider == DiaryMapProvider.GOOGLE },
            )
        }.collectLatest { trigger ->
            if (trigger.query.isBlank()) {
                latestOnClear()
                return@collectLatest
            }

            delay(INPUT_IDLE_DELAY)

            latestOnSearch(
                PlaceSearchRequest(
                    query = trigger.query,
                    provider = trigger.provider.toMapProvider(),
                    bounds = trigger.bounds?.toCoordinateBounds(),
                ),
            )
        }
    }
}

private data class PlaceSearchTrigger(
    val query: String,
    val provider: DiaryMapProvider,
    val bounds: DiaryMapBounds?,
)
