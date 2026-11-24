package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
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
import kotlin.uuid.Uuid

@AssistedInject
internal class ChecklistDetailViewModel(
    @Assisted private val id: Uuid,
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    findChecklistUseCase: FindChecklistUseCase,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(id: Uuid): ChecklistDetailViewModel
    }

    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<ChecklistDetailUiState> =
        combine(
            findChecklistUseCase(parameter = id),
            isUpdateInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { checklist ->
                ChecklistDetailUiState.Content(
                    id = checklist.id,
                    detail = checklist.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: ChecklistDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = ChecklistDetailUiState.Loading,
        )

    private val _effect = Channel<ChecklistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: ChecklistDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(ChecklistDetailEffect.UpdateSucceeded) }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteChecklistUseCase(parameter = id)
                    .onSuccess { _effect.send(ChecklistDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
