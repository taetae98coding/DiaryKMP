package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import kotlin.uuid.Uuid

internal data class RoutineHomeFilterUiState(
    val selectedTagIdSet: Set<Uuid> = emptySet(),
    val viewScope: RoutineViewScope = ROUTINE_DEFAULT_VIEW_SCOPE,
)
