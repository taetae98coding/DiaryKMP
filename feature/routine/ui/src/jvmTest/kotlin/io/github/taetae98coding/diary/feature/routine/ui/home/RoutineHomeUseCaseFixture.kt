package io.github.taetae98coding.diary.feature.routine.ui.home

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetUnfinishedRoutineExistsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal fun routineViewScopeUseCase(viewScope: Flow<RoutineViewScope> = flowOf(ROUTINE_DEFAULT_VIEW_SCOPE)): GetRoutineViewScopeUseCase {
    val useCase = mockk<GetRoutineViewScopeUseCase>()
    every { useCase() } returns viewScope.map { value -> Result.success(value) }

    return useCase
}

internal fun unfinishedRoutineExistsUseCase(exists: Flow<Boolean> = flowOf(false)): GetUnfinishedRoutineExistsUseCase {
    val useCase = mockk<GetUnfinishedRoutineExistsUseCase>()
    every { useCase() } returns exists.map { value -> Result.success(value) }

    return useCase
}
