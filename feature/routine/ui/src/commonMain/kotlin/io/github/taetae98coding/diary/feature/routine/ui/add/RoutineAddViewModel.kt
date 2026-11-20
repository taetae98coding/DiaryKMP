package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineAddViewModel(
    private val addRoutineUseCase: AddRoutineUseCase,
) : ViewModel() {
    val uiState: StateFlow<RoutineAddUiState>
        field = MutableStateFlow(RoutineAddUiState())

    private val _effect = Channel<RoutineAddEffect>(Channel.BUFFERED)
    val effect: Flow<RoutineAddEffect> = _effect.receiveAsFlow()

    fun add(
        input: RoutineInput,
        tagIdSet: Set<Uuid>,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { it.copy(isInProgress = true) }
            try {
                addRoutineUseCase(parameter = AddRoutineUseCase.Parameter(input = input, tagIdSet = tagIdSet))
                    .onSuccess { _effect.send(RoutineAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is RoutineTitleBlankException -> _effect.send(RoutineAddEffect.TitleBlank)
                            is RoutineGoalAmountInvalidException -> _effect.send(RoutineAddEffect.GoalAmountInvalid)
                            is RoutineAmountPerRecordInvalidException -> _effect.send(RoutineAddEffect.AmountPerRecordInvalid)
                        }
                    }
            } finally {
                uiState.update { it.copy(isInProgress = false) }
            }
        }
    }
}
