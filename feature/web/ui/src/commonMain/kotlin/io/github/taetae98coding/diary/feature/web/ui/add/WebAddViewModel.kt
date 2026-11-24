package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebTitleBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebUrlBlankException
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
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
internal class WebAddViewModel(
    private val addWebUseCase: AddWebUseCase,
) : ViewModel() {
    val uiState: StateFlow<WebAddUiState>
        field = MutableStateFlow(WebAddUiState())

    private val _effect = Channel<WebAddEffect>(Channel.BUFFERED)
    val effect: Flow<WebAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: WebDetail,
        tagIdSet: Set<Uuid>,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addWebUseCase(parameter = AddWebUseCase.Parameter(detail = detail, tagIdSet = tagIdSet))
                    .onSuccess { id -> _effect.send(WebAddEffect.AddSucceeded(id = id)) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is WebTitleBlankException -> _effect.send(WebAddEffect.TitleBlank)
                            is WebUrlBlankException -> _effect.send(WebAddEffect.UrlBlank)
                            is WebHeaderNameBlankException -> _effect.send(WebAddEffect.HeaderNameBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
