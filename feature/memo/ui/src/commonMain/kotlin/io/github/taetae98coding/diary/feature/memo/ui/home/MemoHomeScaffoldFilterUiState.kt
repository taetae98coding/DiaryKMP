package io.github.taetae98coding.diary.feature.memo.ui.home

import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import kotlin.uuid.Uuid

internal data class MemoHomeScaffoldFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val existence: MemoExistenceFilter = MemoExistenceFilter(),
) {
    val isApplied: Boolean = selectedTagIdSet.isNotEmpty() || existence != MemoExistenceFilter()
}
