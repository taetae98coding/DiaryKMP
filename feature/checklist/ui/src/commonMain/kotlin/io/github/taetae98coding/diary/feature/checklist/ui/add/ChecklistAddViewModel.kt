package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class ChecklistAddViewModel(
    private val addChecklistUseCase: AddChecklistUseCase,
) : ViewModel() {
    val uiState: StateFlow<ChecklistAddUiState>
        field = MutableStateFlow(ChecklistAddUiState())

    private val _effect = Channel<ChecklistAddEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: ChecklistDetail,
        itemContentList: List<String>,
        tagIdSet: Set<Uuid>,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addChecklistUseCase(parameter = AddChecklistUseCase.Parameter(detail = detail, itemContentList = itemContentList, tagIdSet = tagIdSet))
                    .onSuccess { _effect.send(ChecklistAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        if (throwable is ChecklistTitleBlankException) {
                            _effect.send(ChecklistAddEffect.TitleBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
