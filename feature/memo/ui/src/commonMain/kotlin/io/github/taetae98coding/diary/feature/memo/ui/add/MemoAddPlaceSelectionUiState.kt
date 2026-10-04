package io.github.taetae98coding.diary.feature.memo.ui.add

import kotlin.uuid.Uuid

internal data class MemoAddPlaceSelectionUiState(
    val placeIdSet: Set<Uuid> = emptySet(),
)
