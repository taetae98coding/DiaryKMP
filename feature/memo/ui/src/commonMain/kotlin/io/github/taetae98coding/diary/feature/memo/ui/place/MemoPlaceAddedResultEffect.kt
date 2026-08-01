package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import kotlin.uuid.Uuid

@Composable
internal fun MemoPlaceAddedResultEffect(
    onPlaceAdded: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    ResultEffect<PlaceAddedResult>(resultEventBus = resultEventBus) { result ->
        onPlaceAdded(result.id)
    }
}
