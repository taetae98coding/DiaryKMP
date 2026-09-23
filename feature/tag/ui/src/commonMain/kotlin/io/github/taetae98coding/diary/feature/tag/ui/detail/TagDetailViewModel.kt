package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
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
internal class TagDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val findTagUseCase: FindTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isFinishInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<TagDetailUiState> =
        combine(
            findTagUseCase(parameter = id),
            isUpdateInProgress,
            isFinishInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isFinishInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { tag ->
                TagDetailUiState.Content(
                    id = tag.id,
                    detail = tag.detail,
                    isFinished = tag.isFinished,
                    isInProgress = isUpdateInProgress,
                    isFinishInProgress = isFinishInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: TagDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = TagDetailUiState.Loading,
        )

    private val _effect = Channel<TagDetailEffect>(Channel.BUFFERED)
    val effect: Flow<TagDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: TagDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateTagUseCase(parameter = UpdateTagUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(TagDetailEffect.UpdateSucceeded) }
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
                finishTagUseCase(parameter = id)
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
                restartTagUseCase(parameter = id)
            } finally {
                isFinishInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteTagUseCase(parameter = id)
                    .onSuccess { _effect.send(TagDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
