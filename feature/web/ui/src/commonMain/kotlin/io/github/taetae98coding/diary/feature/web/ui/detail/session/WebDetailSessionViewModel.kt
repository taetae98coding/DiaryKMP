@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.ImportChromeSessionUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class WebDetailSessionViewModel(
    @InjectedParam private val id: Uuid,
    findWebUseCase: FindWebUseCase,
    private val importChromeSessionUseCase: ImportChromeSessionUseCase,
) : ViewModel() {
    private val _effect = Channel<WebDetailSessionEffect>(Channel.BUFFERED)
    val effect: Flow<WebDetailSessionEffect> = _effect.receiveAsFlow()

    val uiState: StateFlow<WebDetailSessionUiState> =
        findWebUseCase(parameter = id)
            .mapNotNull { result -> result.getOrNull()?.detail?.url }
            .distinctUntilChanged()
            .transformLatest { url ->
                emit(WebDetailSessionUiState.Preparing)

                importChromeSessionUseCase(parameter = url)
                    .onFailure { _effect.send(WebDetailSessionEffect.ImportFailed) }

                emit(WebDetailSessionUiState.Prepared(url = url))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = WebDetailSessionUiState.Preparing,
            )
}
