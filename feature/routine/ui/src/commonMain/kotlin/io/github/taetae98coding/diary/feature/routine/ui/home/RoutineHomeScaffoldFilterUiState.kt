package io.github.taetae98coding.diary.feature.routine.ui.home

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import kotlin.uuid.Uuid

internal data class RoutineHomeScaffoldFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val storedTagIdSet: Set<Uuid> = emptySet(),
    val viewScope: RoutineViewScope = ROUTINE_DEFAULT_VIEW_SCOPE,
    val hasUnfinishedRoutine: Boolean = false,
    val isLoaded: Boolean = true,
) {
    val isApplied: Boolean = selectedTagIdSet.isNotEmpty()

    val emptyReason: RoutineHomeEmptyReason =
        when {
            isApplied -> RoutineHomeEmptyReason.NO_FILTERED_ROUTINE
            hasUnfinishedRoutine && viewScope == RoutineViewScope.TODAY -> RoutineHomeEmptyReason.NO_TODAY_ROUTINE
            else -> RoutineHomeEmptyReason.NO_ROUTINE
        }

    // selectedTagIdSet은 선택할 수 없게 된 태그를 빼고 조회되어 태그 상태만 바뀌어도 달라지므로, 목록 위치 되돌림의 기준은 저장된 선택으로 둔다.
    val listQueryFilter: RoutineHomeListQueryFilter? =
        if (isLoaded) {
            RoutineHomeListQueryFilter(
                storedTagIdSet = storedTagIdSet,
                viewScope = viewScope,
            )
        } else {
            null
        }
}

internal data class RoutineHomeListQueryFilter(
    val storedTagIdSet: Set<Uuid>,
    val viewScope: RoutineViewScope,
)

internal enum class RoutineHomeEmptyReason {
    NO_ROUTINE,
    NO_TODAY_ROUTINE,
    NO_FILTERED_ROUTINE,
}
