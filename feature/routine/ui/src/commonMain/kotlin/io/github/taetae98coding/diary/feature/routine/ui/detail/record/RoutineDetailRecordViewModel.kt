package io.github.taetae98coding.diary.feature.routine.ui.detail.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineDetailRecordViewModel(
    @InjectedParam private val routineId: Uuid,
    private val addRoutineRecordUseCase: AddRoutineRecordUseCase,
    private val deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase,
    pageRoutineRecordUseCase: PageRoutineRecordUseCase,
) : ViewModel() {
    private val isAddInProgress = MutableStateFlow(false)
    private val deleteInProgressIdSet = mutableSetOf<Uuid>()

    val recordPagingData: Flow<PagingData<RoutineRecord>> =
        pageRoutineRecordUseCase(parameter = routineId)
            .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<RoutineDetailRecordUiState> =
        isAddInProgress
            .map { isAddInProgress -> RoutineDetailRecordUiState(isAddInProgress = isAddInProgress) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = RoutineDetailRecordUiState(),
            )

    private val _effect = Channel<RoutineDetailRecordEffect>(Channel.BUFFERED)
    val effect: Flow<RoutineDetailRecordEffect> = _effect.receiveAsFlow()

    fun add(
        date: LocalDate,
        amount: Int,
    ) {
        if (isAddInProgress.value) return

        viewModelScope.launch {
            isAddInProgress.value = true
            try {
                addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = routineId, date = date, amount = amount))
                    .onSuccess { _effect.send(RoutineDetailRecordEffect.AddSucceeded) }
            } finally {
                isAddInProgress.value = false
            }
        }
    }

    fun delete(recordId: Uuid) {
        if (!deleteInProgressIdSet.add(recordId)) return

        viewModelScope.launch {
            try {
                deleteRoutineRecordUseCase(parameter = recordId)
            } finally {
                deleteInProgressIdSet.remove(recordId)
            }
        }
    }
}

// 조회에 실패하면 불러오기가 끝난 빈 목록으로 두어, 불러오는 중이 아니라 기록이 없는 것으로 표시하게 한다.
private val LOADED_EMPTY_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
