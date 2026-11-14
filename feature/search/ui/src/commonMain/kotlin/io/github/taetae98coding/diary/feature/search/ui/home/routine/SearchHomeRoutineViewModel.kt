@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchRoutineUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeRoutineViewModel(
    searchRoutineUseCase: SearchRoutineUseCase,
    private val finishRoutineUseCase: FinishRoutineUseCase,
    private val restartRoutineUseCase: RestartRoutineUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    private val restoreRoutineUseCase: RestoreRoutineUseCase,
) : ViewModel() {
    private val today = MutableStateFlow<LocalDate?>(null)

    private val resultState =
        SearchHomeResultState<RoutineSummary>(
            search = { query, _ ->
                today.flatMapLatest { today ->
                    if (today == null) {
                        // 불러오기 상태를 싣지 않은 빈 목록은 오늘이 정해지기 전까지 결과를 불러오는 중으로 둔다.
                        flowOf(Result.success(PagingData.empty()))
                    } else {
                        searchRoutineUseCase(parameter = SearchRoutineUseCase.Parameter(query = query, today = today))
                    }
                }
            },
        )

    val uiState: StateFlow<SearchHomeResultUiState> =
        resultState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(),
        )
    val pagingData: Flow<PagingData<RoutineSummary>> = resultState.pagingData.cachedIn(viewModelScope)

    private val _effect = Channel<RoutineListEffect>(Channel.BUFFERED)
    val effect: Flow<RoutineListEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        resultState.updateQuery(query = query)
    }

    fun updateToday(today: LocalDate) {
        this.today.value = today
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                finishRoutineUseCase(parameter = id)
                    .onSuccess { _effect.send(RoutineListEffect.Finished(id = id)) }
            }
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                restartRoutineUseCase(parameter = id)
                    .onSuccess { _effect.send(RoutineListEffect.Restarted(id = id)) }
            }
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                deleteRoutineUseCase(parameter = id)
                    .onSuccess { _effect.send(RoutineListEffect.Deleted(id = id)) }
            }
        }
    }

    fun undo(effect: RoutineListEffect) {
        viewModelScope.launch {
            resultState.runOnce(id = effect.targetId()) {
                when (effect) {
                    is RoutineListEffect.Finished -> restartRoutineUseCase(parameter = effect.id)
                    is RoutineListEffect.Restarted -> finishRoutineUseCase(parameter = effect.id)
                    is RoutineListEffect.Deleted -> restoreRoutineUseCase(parameter = effect.id)
                }
            }
        }
    }

    private fun RoutineListEffect.targetId(): Uuid =
        when (this) {
            is RoutineListEffect.Finished -> id
            is RoutineListEffect.Restarted -> id
            is RoutineListEffect.Deleted -> id
        }
}
