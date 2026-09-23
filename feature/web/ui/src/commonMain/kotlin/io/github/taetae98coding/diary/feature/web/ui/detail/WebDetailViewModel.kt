package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
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
internal class WebDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateWebUseCase: UpdateWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    findWebUseCase: FindWebUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<WebDetailUiState> =
        combine(
            findWebUseCase(parameter = id),
            isUpdateInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { web ->
                WebDetailUiState.Content(
                    id = web.id,
                    detail = web.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: WebDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = WebDetailUiState.Loading,
        )

    private val _effect = Channel<WebDetailEffect>(Channel.BUFFERED)
    val effect: Flow<WebDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: WebDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(WebDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is WebHeaderNameBlankException -> _effect.send(WebDetailEffect.HeaderNameBlank)
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
                deleteWebUseCase(parameter = id)
                    .onSuccess { _effect.send(WebDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
