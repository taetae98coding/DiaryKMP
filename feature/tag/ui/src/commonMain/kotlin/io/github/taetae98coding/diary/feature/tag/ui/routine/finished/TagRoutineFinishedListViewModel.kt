package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

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
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.routine.RoutinePagingState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@AssistedInject
internal class TagRoutineFinishedListViewModel(
    @Assisted tagId: Uuid,
    pageFinishedTagRoutineUseCase: PageFinishedTagRoutineUseCase,
    findTagUseCase: FindTagUseCase,
    finishRoutineUseCase: FinishRoutineUseCase,
    restartRoutineUseCase: RestartRoutineUseCase,
    deleteRoutineUseCase: DeleteRoutineUseCase,
    restoreRoutineUseCase: RestoreRoutineUseCase,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(tagId: Uuid): TagRoutineFinishedListViewModel
    }

    private val routineActions =
        ListItemActions<RoutineListEffect>(
            finishUseCase = finishRoutineUseCase,
            restartUseCase = restartRoutineUseCase,
            deleteUseCase = deleteRoutineUseCase,
            restoreUseCase = restoreRoutineUseCase,
            restartedEffect = RoutineListEffect::Restarted,
            deletedEffect = RoutineListEffect::Deleted,
        )

    private val routinePagingState =
        RoutinePagingState(pageRoutine = { today -> pageFinishedTagRoutineUseCase(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)) })

    val uiState: StateFlow<TagRoutineFinishedListUiState> =
        findTagUseCase(parameter = tagId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { tag -> TagRoutineFinishedListUiState(title = tag.detail.emojiWithTitle) }
                    ?: TagRoutineFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagRoutineFinishedListUiState(),
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
}
