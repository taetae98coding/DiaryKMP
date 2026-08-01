package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.usecase.CopyMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
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
internal class MemoDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val findMemoUseCase: FindMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val copyMemoUseCase: CopyMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isFinishInProgress = MutableStateFlow(false)
    private val isCopyInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<MemoDetailUiState> =
        combine(
            findMemoUseCase(parameter = id),
            isUpdateInProgress,
            isFinishInProgress,
            isCopyInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isFinishInProgress, isCopyInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { memo ->
                MemoDetailUiState.Content(
                    id = memo.id,
                    detail = memo.detail,
                    isFinished = memo.isFinished,
                    isInProgress = isUpdateInProgress,
                    isFinishInProgress = isFinishInProgress,
                    isCopyInProgress = isCopyInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: MemoDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MemoDetailUiState.Loading,
        )

    private val _effect = Channel<MemoDetailEffect>(Channel.BUFFERED)
    val effect: Flow<MemoDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: MemoDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(MemoDetailEffect.UpdateSucceeded) }
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
                finishMemoUseCase(parameter = id)
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
                restartMemoUseCase(parameter = id)
            } finally {
                isFinishInProgress.value = false
            }
        }
    }

    fun copy() {
        if (isCopyInProgress.value) return

        viewModelScope.launch {
            isCopyInProgress.value = true
            try {
                copyMemoUseCase(parameter = id)
                    .onSuccess { copiedId -> _effect.send(MemoDetailEffect.CopySucceeded(id = copiedId)) }
            } finally {
                isCopyInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteMemoUseCase(parameter = id)
                    .onSuccess { _effect.send(MemoDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
