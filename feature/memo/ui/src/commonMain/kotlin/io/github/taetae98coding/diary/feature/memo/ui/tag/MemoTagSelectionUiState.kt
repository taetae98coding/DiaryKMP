package io.github.taetae98coding.diary.feature.memo.ui.tag

import kotlin.uuid.Uuid

internal data class MemoTagSelectionUiState(
    val tagIdSet: Set<Uuid> = emptySet(),
    val primaryTagId: Uuid? = null,
)
