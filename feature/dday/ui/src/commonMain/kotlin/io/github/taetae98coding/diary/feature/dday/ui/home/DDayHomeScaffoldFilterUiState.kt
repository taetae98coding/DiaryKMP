package io.github.taetae98coding.diary.feature.dday.ui.home

import kotlin.uuid.Uuid

internal data class DDayHomeScaffoldFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val storedTagIdSet: Set<Uuid> = emptySet(),
    val isLoaded: Boolean = true,
) {
    val isApplied: Boolean = selectedTagIdSet.isNotEmpty()

    // selectedTagIdSet은 선택할 수 없게 된 태그를 빼고 조회되어 태그 상태만 바뀌어도 달라지므로, 목록 위치 되돌림의 기준은 저장된 선택으로 둔다.
    val listQueryFilter: Set<Uuid>? = if (isLoaded) storedTagIdSet else null
}
