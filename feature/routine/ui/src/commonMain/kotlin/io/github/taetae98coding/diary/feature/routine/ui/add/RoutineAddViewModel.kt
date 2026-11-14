package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
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
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineAddViewModel(
    private val addRoutineUseCase: AddRoutineUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    val uiState: StateFlow<RoutineAddUiState> =
        isInProgress
            .map { isInProgress -> RoutineAddUiState(isInProgress = isInProgress) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = RoutineAddUiState(),
            )

    private val _effect = Channel<RoutineAddEffect>(Channel.BUFFERED)
    val effect: Flow<RoutineAddEffect> = _effect.receiveAsFlow()

    fun add(
        input: RoutineInput,
        tagIdSet: Set<Uuid>,
    ) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
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
                isInProgress.value = false
            }
        }
    }
}
