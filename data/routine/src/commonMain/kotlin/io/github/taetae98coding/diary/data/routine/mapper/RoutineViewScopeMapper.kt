package io.github.taetae98coding.diary.data.routine.mapper

import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope

internal fun RoutineViewScope.toLocal(): RoutineViewScopeLocalEntity =
    when (this) {
        RoutineViewScope.TODAY -> RoutineViewScopeLocalEntity.TODAY
        RoutineViewScope.ALL -> RoutineViewScopeLocalEntity.ALL
    }

internal fun RoutineViewScopeLocalEntity.toDomain(): RoutineViewScope =
    when (this) {
        RoutineViewScopeLocalEntity.TODAY -> RoutineViewScope.TODAY
        RoutineViewScopeLocalEntity.ALL -> RoutineViewScope.ALL
    }
