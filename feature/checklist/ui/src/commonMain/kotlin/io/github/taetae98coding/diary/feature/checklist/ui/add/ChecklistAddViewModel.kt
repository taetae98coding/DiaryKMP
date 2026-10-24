package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
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

@KoinViewModel
internal class ChecklistAddViewModel(
    private val addChecklistUseCase: AddChecklistUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    val uiState: StateFlow<ChecklistAddUiState> =
        isInProgress
            .map { isInProgress -> ChecklistAddUiState(isInProgress = isInProgress) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = ChecklistAddUiState(),
            )

    private val _effect = Channel<ChecklistAddEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: ChecklistDetail,
        itemContentList: List<String>,
    ) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                addChecklistUseCase(parameter = AddChecklistUseCase.Parameter(detail = detail, itemContentList = itemContentList))
                    .onSuccess { _effect.send(ChecklistAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        if (throwable is ChecklistTitleBlankException) {
                            _effect.send(ChecklistAddEffect.TitleBlank)
                        }
                    }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
