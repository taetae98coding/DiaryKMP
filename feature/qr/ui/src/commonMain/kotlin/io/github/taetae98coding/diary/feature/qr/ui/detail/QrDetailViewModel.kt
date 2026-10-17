package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
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
internal class QrDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateQrUseCase: UpdateQrUseCase,
    private val deleteQrUseCase: DeleteQrUseCase,
    findQrUseCase: FindQrUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<QrDetailUiState> =
        combine(
            findQrUseCase(parameter = id),
            isUpdateInProgress,
            isDeleteInProgress,
            getDefaultMapProviderUseCase(parameter = Unit),
        ) { result, isUpdateInProgress, isDeleteInProgress, providerResult ->
            result.getOrNull()?.let { qr ->
                QrDetailUiState.Content(
                    id = qr.id,
                    detail = qr.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                    defaultProvider = providerResult.getOrNull(),
                )
            } ?: QrDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = QrDetailUiState.Loading,
        )

    private val _effect = Channel<QrDetailEffect>(Channel.BUFFERED)
    val effect: Flow<QrDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: QrDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(QrDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is QrValueEmptyException -> _effect.send(QrDetailEffect.ValueEmpty)
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
                deleteQrUseCase(parameter = id)
                    .onSuccess { _effect.send(QrDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
