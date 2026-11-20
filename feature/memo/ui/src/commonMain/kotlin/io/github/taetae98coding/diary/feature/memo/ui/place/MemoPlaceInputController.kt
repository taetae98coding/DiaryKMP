package io.github.taetae98coding.diary.feature.memo.ui.place

import kotlin.uuid.Uuid

internal interface MemoPlaceInputController {
    fun updateQuery(query: String)

    fun selectPlace(placeId: Uuid)

    fun unselectPlace(placeId: Uuid)
}
