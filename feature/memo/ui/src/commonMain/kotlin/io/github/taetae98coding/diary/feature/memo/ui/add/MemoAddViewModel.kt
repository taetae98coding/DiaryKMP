package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionUiState
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
internal class MemoAddViewModel(
    private val addMemoUseCase: AddMemoUseCase,
) : ViewModel() {
    val uiState: StateFlow<MemoAddUiState>
        field = MutableStateFlow(MemoAddUiState())

    private val _effect = Channel<MemoAddEffect>(Channel.BUFFERED)
    val effect: Flow<MemoAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: MemoDetail,
        tagSelection: PrimaryTagSelectionUiState,
        webIdSet: Set<Uuid>,
        contactIdSet: Set<Uuid>,
        placeIdSet: Set<Uuid>,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addMemoUseCase(
                    parameter =
                        AddMemoUseCase.Parameter(
                            detail = detail,
                            primaryTagId = tagSelection.primaryTagId,
                            tagIdSet = tagSelection.tagIdSet,
                            webIdSet = webIdSet,
                            contactIdSet = contactIdSet,
                            placeIdSet = placeIdSet,
                        ),
                ).onSuccess { _effect.send(MemoAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is MemoTitleBlankException -> _effect.send(MemoAddEffect.TitleBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
