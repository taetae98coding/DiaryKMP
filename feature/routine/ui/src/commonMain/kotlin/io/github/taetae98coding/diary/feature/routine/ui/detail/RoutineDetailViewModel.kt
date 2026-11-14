package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val finishRoutineUseCase: FinishRoutineUseCase,
    private val restartRoutineUseCase: RestartRoutineUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    findRoutineUseCase: FindRoutineUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isFinishInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<RoutineDetailUiState> =
        combine(
            findRoutineUseCase(parameter = id),
            isUpdateInProgress,
            isFinishInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isFinishInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { routine ->
                RoutineDetailUiState.Content(
                    id = routine.id,
                    detail = routine.detail,
                    isFinished = routine.isFinished,
                    isUpdateInProgress = isUpdateInProgress,
                    isFinishInProgress = isFinishInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: RoutineDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = RoutineDetailUiState.Loading,
        )

    private val _effect = Channel<RoutineDetailEffect>(Channel.BUFFERED)
    val effect: Flow<RoutineDetailEffect> = _effect.receiveAsFlow()

    fun update(input: RoutineInput) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input))
                    .onSuccess { _effect.send(RoutineDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is RoutineGoalAmountInvalidException -> _effect.send(RoutineDetailEffect.GoalAmountInvalid)
                            is RoutineAmountPerRecordInvalidException -> _effect.send(RoutineDetailEffect.AmountPerRecordInvalid)
                        }
                    }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun finish() {
        if (isFinishInProgress.value) return

        viewModelScope.launch {
            isFinishInProgress.value = true
            try {
                finishRoutineUseCase(parameter = id)
            } finally {
                isFinishInProgress.value = false
            }
        }
    }

    fun restart() {
        if (isFinishInProgress.value) return

        viewModelScope.launch {
            isFinishInProgress.value = true
            try {
                restartRoutineUseCase(parameter = id)
            } finally {
                isFinishInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteRoutineUseCase(parameter = id)
                    .onSuccess { _effect.send(RoutineDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
