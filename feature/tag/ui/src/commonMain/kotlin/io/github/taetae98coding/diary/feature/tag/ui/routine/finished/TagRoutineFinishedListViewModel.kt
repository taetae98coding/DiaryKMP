@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagRoutineFinishedListViewModel(
    @InjectedParam tagId: Uuid,
    pageFinishedTagRoutineUseCase: PageFinishedTagRoutineUseCase,
    findTagUseCase: FindTagUseCase,
    finishRoutineUseCase: FinishRoutineUseCase,
    restartRoutineUseCase: RestartRoutineUseCase,
    deleteRoutineUseCase: DeleteRoutineUseCase,
    restoreRoutineUseCase: RestoreRoutineUseCase,
) : ViewModel() {
    private val routineActions =
        ListItemActions<RoutineListEffect>(
            finishUseCase = finishRoutineUseCase,
            restartUseCase = restartRoutineUseCase,
            deleteUseCase = deleteRoutineUseCase,
            restoreUseCase = restoreRoutineUseCase,
            restartedEffect = RoutineListEffect::Restarted,
            deletedEffect = RoutineListEffect::Deleted,
        )

    private val today = MutableStateFlow<LocalDate?>(null)

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
        today
            .flatMapLatest { today ->
                if (today == null) {
                    // 불러오기 상태를 싣지 않은 빈 목록은 오늘이 정해지기 전까지 목록을 불러오는 중으로 둔다.
                    flowOf(PagingData.empty())
                } else {
                    pageFinishedTagRoutineUseCase(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today))
                        // 조건 없는 PagingData.empty()는 불러오기 상태를 싣지 않아 목록이 처음의 불러오는 중에 머물러 빈 상태로 바뀌지 않는다.
                        .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
                }
            }.cachedIn(viewModelScope)

    val effect: Flow<RoutineListEffect> = routineActions.effect

    fun updateToday(today: LocalDate) {
        this.today.value = today
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

private val LOADED_EMPTY_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
