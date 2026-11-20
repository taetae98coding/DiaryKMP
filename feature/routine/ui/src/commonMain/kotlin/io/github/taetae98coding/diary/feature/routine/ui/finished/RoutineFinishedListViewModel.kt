package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.routine.RoutinePagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineFinishedListViewModel(
    pageFinishedRoutineUseCase: PageFinishedRoutineUseCase,
    finishRoutineUseCase: FinishRoutineUseCase,
    restartRoutineUseCase: RestartRoutineUseCase,
    deleteRoutineUseCase: DeleteRoutineUseCase,
    restoreRoutineUseCase: RestoreRoutineUseCase,
) : ViewModel() {
    private val routinePagingState = RoutinePagingState(pageRoutine = { today -> pageFinishedRoutineUseCase(parameter = today) })

    private val routineActions =
        ListItemActions(
            finishUseCase = finishRoutineUseCase,
            restartUseCase = restartRoutineUseCase,
            deleteUseCase = deleteRoutineUseCase,
            restoreUseCase = restoreRoutineUseCase,
            restartedEffect = RoutineListEffect::Restarted,
            deletedEffect = RoutineListEffect::Deleted,
        )

    val routinePagingData: Flow<PagingData<RoutineSummary>> =
        routinePagingState.routinePagingData.cachedIn(viewModelScope)

    val effect: Flow<RoutineListEffect> = routineActions.effect

    fun updateToday(today: LocalDate) {
        routinePagingState.updateToday(today = today)
    }

    fun finish(id: Uuid) {
        viewModelScope.launch { routineActions.finish(id = id) }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch { routineActions.restart(id = id) }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { routineActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { routineActions.restore(id = id) }
    }

    fun undo(effect: RoutineListEffect) {
        when (effect) {
            is RoutineListEffect.Finished -> restart(id = effect.id)
            is RoutineListEffect.Restarted -> finish(id = effect.id)
            is RoutineListEffect.Deleted -> restore(id = effect.id)
        }
    }
}
