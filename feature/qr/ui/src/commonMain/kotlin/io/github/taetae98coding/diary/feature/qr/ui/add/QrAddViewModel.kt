package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class QrAddViewModel(
    private val addQrUseCase: AddQrUseCase,
) : ViewModel() {
    val uiState: StateFlow<QrAddUiState>
        field = MutableStateFlow(QrAddUiState())

    private val _effect = Channel<QrAddEffect>(Channel.BUFFERED)
    val effect: Flow<QrAddEffect> = _effect.receiveAsFlow()

    fun add(detail: QrDetail) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addQrUseCase(parameter = detail)
                    .onSuccess { _effect.send(QrAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is QrTitleBlankException -> _effect.send(QrAddEffect.TitleBlank)
                            is QrValueEmptyException -> _effect.send(QrAddEffect.ValueEmpty)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
