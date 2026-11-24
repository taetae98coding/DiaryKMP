@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.routine.RoutinePagingState
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@AssistedInject
internal class TagDetailRoutineViewModel(
    @Assisted tagId: Uuid,
    pageTagRoutineUseCase: PageTagRoutineUseCase,
    finishRoutineUseCase: FinishRoutineUseCase,
    restartRoutineUseCase: RestartRoutineUseCase,
    deleteRoutineUseCase: DeleteRoutineUseCase,
    restoreRoutineUseCase: RestoreRoutineUseCase,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(tagId: Uuid): TagDetailRoutineViewModel
    }

    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    private val routinePagingState =
        RoutinePagingState(
            pageRoutine = { today ->
                scopeUiState.flatMapLatest { (scopeValue) ->
                    pageTagRoutineUseCase(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = scopeValue, today = today))
                }
            },
        )

    val routinePagingData: Flow<PagingData<RoutineSummary>> =
        routinePagingState.routinePagingData.cachedIn(viewModelScope)

    private val routineActions =
        ListItemActions<RoutineListEffect>(
            finishUseCase = finishRoutineUseCase,
            restartUseCase = restartRoutineUseCase,
            deleteUseCase = deleteRoutineUseCase,
            restoreUseCase = restoreRoutineUseCase,
            finishedEffect = RoutineListEffect::Finished,
            deletedEffect = RoutineListEffect::Deleted,
        )

    val effect: Flow<RoutineListEffect> = routineActions.effect

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

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
}
