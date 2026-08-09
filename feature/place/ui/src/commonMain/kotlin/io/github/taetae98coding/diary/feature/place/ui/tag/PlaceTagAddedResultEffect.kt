package io.github.taetae98coding.diary.feature.place.ui.tag

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import kotlin.uuid.Uuid

@Composable
internal fun PlaceTagAddedResultEffect(
    requestKey: Uuid,
    onTagAdded: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    ResultEffect<TagAddedResult>(
        resultKey = tagAddedResultKey(requestKey = requestKey),
        resultEventBus = resultEventBus,
    ) { result ->
        onTagAdded(result.id)
    }
}
