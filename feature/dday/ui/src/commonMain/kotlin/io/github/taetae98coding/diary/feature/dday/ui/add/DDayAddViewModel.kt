package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class DDayAddViewModel(
    private val addDDayUseCase: AddDDayUseCase,
) : ViewModel() {
    val uiState: StateFlow<DDayAddUiState>
        field = MutableStateFlow(DDayAddUiState())

    private val _effect = Channel<DDayAddEffect>(Channel.BUFFERED)
    val effect: Flow<DDayAddEffect> = _effect.receiveAsFlow()

    fun add(
        input: DDayInput,
        tagSelection: PrimaryTagSelectionUiState,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addDDayUseCase(parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = tagSelection.tagIdSet, primaryTagId = tagSelection.primaryTagId))
                    .onSuccess { _effect.send(DDayAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is DDayTitleBlankException -> _effect.send(DDayAddEffect.TitleBlank)
                            is DDayRepeatBlankException -> _effect.send(DDayAddEffect.RepeatBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
