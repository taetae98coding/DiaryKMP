package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.exception.TagTitleBlankException
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagAddViewModel(
    private val addTagUseCase: AddTagUseCase,
) : ViewModel() {
    val uiState: StateFlow<TagAddUiState>
        field = MutableStateFlow(TagAddUiState())

    private val _effect = Channel<TagAddEffect>(Channel.BUFFERED)
    val effect: Flow<TagAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: TagDetail,
        linkedTagIdSet: Set<Uuid>,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { it.copy(isInProgress = true) }
            try {
                addTagUseCase(parameter = AddTagUseCase.Parameter(detail = detail, linkedTagIdSet = linkedTagIdSet))
                    .onSuccess { id -> _effect.send(TagAddEffect.AddSucceeded(id = id)) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is TagTitleBlankException -> _effect.send(TagAddEffect.TitleBlank)
                        }
                    }
            } finally {
                uiState.update { it.copy(isInProgress = false) }
            }
        }
    }
}
