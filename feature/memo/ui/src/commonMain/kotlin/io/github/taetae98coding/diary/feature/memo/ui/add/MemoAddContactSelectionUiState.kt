package io.github.taetae98coding.diary.feature.memo.ui.add

import kotlin.uuid.Uuid

internal data class MemoAddContactSelectionUiState(
    val contactIdSet: Set<Uuid> = emptySet(),
)
