package io.github.taetae98coding.diary.feature.memo.ui.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

internal sealed interface MemoPlacePickerEvent {
    data class ClickAdd(
        val coordinate: Coordinate?,
    ) : MemoPlacePickerEvent

    data class Select(
        val id: Uuid,
    ) : MemoPlacePickerEvent

    data class Unselect(
        val id: Uuid,
    ) : MemoPlacePickerEvent

    data class ChangeQuery(
        val query: String,
    ) : MemoPlacePickerEvent
}
