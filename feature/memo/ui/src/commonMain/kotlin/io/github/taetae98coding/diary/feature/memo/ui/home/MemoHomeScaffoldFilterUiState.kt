package io.github.taetae98coding.diary.feature.memo.ui.home

import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import kotlin.uuid.Uuid

internal data class MemoHomeScaffoldFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val storedTagIdSet: Set<Uuid> = emptySet(),
    val existence: MemoExistenceFilter = MemoExistenceFilter(),
    val isLoaded: Boolean = true,
) {
    val isApplied: Boolean = selectedTagIdSet.isNotEmpty() || existence != MemoExistenceFilter()

    // selectedTagIdSet은 선택할 수 없게 된 태그를 빼고 조회되어 태그 상태만 바뀌어도 달라지므로, 목록 위치 되돌림의 기준은 저장된 선택으로 둔다.
    val listQueryFilter: MemoHomeListQueryFilter? = if (isLoaded) MemoHomeListQueryFilter(storedTagIdSet = storedTagIdSet, existence = existence) else null
}

internal data class MemoHomeListQueryFilter(
    val storedTagIdSet: Set<Uuid>,
    val existence: MemoExistenceFilter,
)
