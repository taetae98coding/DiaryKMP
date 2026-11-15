@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.usecase.AddQuickRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetUnfinishedRoutineExistsUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecordEffect
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineHomeViewModel(
    getRoutineFilterUseCase: GetRoutineFilterUseCase,
    getRoutineFilterTagIdUseCase: GetRoutineFilterTagIdUseCase,
    getRoutineViewScopeUseCase: GetRoutineViewScopeUseCase,
    getUnfinishedRoutineExistsUseCase: GetUnfinishedRoutineExistsUseCase,
    pageViewScopeRoutineUseCase: PageViewScopeRoutineUseCase,
    finishRoutineUseCase: FinishRoutineUseCase,
    restartRoutineUseCase: RestartRoutineUseCase,
    deleteRoutineUseCase: DeleteRoutineUseCase,
    restoreRoutineUseCase: RestoreRoutineUseCase,
    private val addQuickRoutineRecordUseCase: AddQuickRoutineRecordUseCase,
    private val deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase,
) : ViewModel() {
    private val today = MutableStateFlow<LocalDate?>(null)

    val routinePagingData: Flow<PagingData<RoutineSummary>> =
        today
            .flatMapLatest { today ->
                if (today == null) {
                    // 불러오기 상태를 싣지 않은 빈 목록은 오늘이 정해지기 전까지 목록을 불러오는 중으로 둔다.
                    flowOf(PagingData.empty())
                } else {
                    pageViewScopeRoutineUseCase(parameter = today)
                        // 조건 없는 PagingData.empty()는 불러오기 상태를 싣지 않아 목록이 처음의 불러오는 중에 머물러 빈 상태로 바뀌지 않는다.
                        .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
                }
            }.cachedIn(viewModelScope)

    val filterUiState: StateFlow<RoutineHomeScaffoldFilterUiState> =
        combine(
            getRoutineFilterUseCase(),
            getRoutineFilterTagIdUseCase(),
            getRoutineViewScopeUseCase(),
            getUnfinishedRoutineExistsUseCase(),
        ) { tagListResult, storedTagIdSetResult, viewScopeResult, hasUnfinishedRoutineResult ->
            RoutineHomeScaffoldFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                storedTagIdSet = storedTagIdSetResult.getOrDefault(emptySet()),
                viewScope = viewScopeResult.getOrDefault(ROUTINE_DEFAULT_VIEW_SCOPE),
                hasUnfinishedRoutine = hasUnfinishedRoutineResult.getOrDefault(false),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = RoutineHomeScaffoldFilterUiState(isLoaded = false),
        )

    private val routineActions =
        ListItemActions(
            finishUseCase = finishRoutineUseCase,
            restartUseCase = restartRoutineUseCase,
            deleteUseCase = deleteRoutineUseCase,
            restoreUseCase = restoreRoutineUseCase,
            finishedEffect = RoutineListEffect::Finished,
            deletedEffect = RoutineListEffect::Deleted,
        )

    val effect: Flow<RoutineListEffect> = routineActions.effect

    private val _recordEffect = Channel<RoutineHomeRecordEffect>(Channel.BUFFERED)
    val recordEffect: Flow<RoutineHomeRecordEffect> = _recordEffect.receiveAsFlow()

    private val recordInProgressRoutineIdSet = mutableSetOf<Uuid>()
    private val undoInProgressRecordIdSet = mutableSetOf<Uuid>()

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

    fun undo(effect: RoutineListEffect) {
        when (effect) {
            is RoutineListEffect.Finished -> restart(id = effect.id)
            is RoutineListEffect.Restarted -> finish(id = effect.id)
            is RoutineListEffect.Deleted -> restore(id = effect.id)
        }
    }

    fun quickRecord(
        record: RoutineHomeRecord,
        today: LocalDate,
    ) {
        record(record = record, today = today, amount = null)
    }

    fun amountRecord(
        record: RoutineHomeRecord,
        today: LocalDate,
    ) {
        record(record = record, today = today, amount = record.amount)
    }

    fun undoRecord(recordId: Uuid) {
        if (!undoInProgressRecordIdSet.add(recordId)) return

        viewModelScope.launch {
            try {
                deleteRoutineRecordUseCase(parameter = recordId)
            } finally {
                undoInProgressRecordIdSet.remove(recordId)
            }
        }
    }

    private fun record(
        record: RoutineHomeRecord,
        today: LocalDate,
        amount: Int?,
    ) {
        if (!recordInProgressRoutineIdSet.add(record.routineId)) return

        viewModelScope.launch {
            try {
                addQuickRoutineRecordUseCase(parameter = AddQuickRoutineRecordUseCase.Parameter(routineId = record.routineId, today = today, amount = amount))
                    .onSuccess { recordId -> _recordEffect.send(RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)) }
            } finally {
                recordInProgressRoutineIdSet.remove(record.routineId)
            }
        }
    }
}

private val LOADED_EMPTY_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
