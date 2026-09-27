package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
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
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class QrAddViewModel(
    private val addQrUseCase: AddQrUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    val uiState: StateFlow<QrAddUiState> =
        combine(
            isInProgress,
            getDefaultMapProviderUseCase(parameter = Unit),
        ) { isInProgress, providerResult ->
            QrAddUiState(
                isInProgress = isInProgress,
                defaultProvider = providerResult.getOrNull(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = QrAddUiState(),
        )

    private val _effect = Channel<QrAddEffect>(Channel.BUFFERED)
    val effect: Flow<QrAddEffect> = _effect.receiveAsFlow()

    fun add(detail: QrDetail) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
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
                isInProgress.value = false
            }
        }
    }
}
