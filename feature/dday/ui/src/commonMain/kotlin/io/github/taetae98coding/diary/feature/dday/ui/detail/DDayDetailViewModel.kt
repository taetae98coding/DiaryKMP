package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
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
internal class DDayDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateDDayUseCase: UpdateDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    findDDayUseCase: FindDDayUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<DDayDetailUiState> =
        combine(
            findDDayUseCase(parameter = id),
            isUpdateInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { dday ->
                DDayDetailUiState.Content(
                    id = dday.id,
                    detail = dday.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: DDayDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = DDayDetailUiState.Loading,
        )

    private val _effect = Channel<DDayDetailEffect>(Channel.BUFFERED)
    val effect: Flow<DDayDetailEffect> = _effect.receiveAsFlow()

    fun update(input: DDayInput) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input))
                    .onSuccess { _effect.send(DDayDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        if (throwable is DDayRepeatBlankException) {
                            _effect.send(DDayDetailEffect.RepeatBlank)
                        }
                    }
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
                deleteDDayUseCase(parameter = id)
                    .onSuccess { _effect.send(DDayDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
