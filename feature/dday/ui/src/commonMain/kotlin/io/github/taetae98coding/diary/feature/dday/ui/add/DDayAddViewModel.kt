package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
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
internal class DDayAddViewModel(
    private val addDDayUseCase: AddDDayUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    val uiState: StateFlow<DDayAddUiState> =
        isInProgress
            .map { isInProgress -> DDayAddUiState(isInProgress = isInProgress) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = DDayAddUiState(),
            )

    private val _effect = Channel<DDayAddEffect>(Channel.BUFFERED)
    val effect: Flow<DDayAddEffect> = _effect.receiveAsFlow()

    fun add(
        input: DDayInput,
        tagIdSet: Set<Uuid>,
    ) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                addDDayUseCase(parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = tagIdSet))
                    .onSuccess { _effect.send(DDayAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is DDayTitleBlankException -> _effect.send(DDayAddEffect.TitleBlank)
                            is DDayRepeatBlankException -> _effect.send(DDayAddEffect.RepeatBlank)
                        }
                    }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
