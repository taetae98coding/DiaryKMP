@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ChecklistDetailItemViewModel(
    @InjectedParam private val checklistId: Uuid,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val checkChecklistItemUseCase: CheckChecklistItemUseCase,
    private val uncheckChecklistItemUseCase: UncheckChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
    private val resetChecklistUseCase: ResetChecklistUseCase,
    getChecklistItemUseCase: GetChecklistItemUseCase,
) : ViewModel() {
    private val sort = MutableStateFlow(ChecklistItemSort.ADDED)
    private val isAddInProgress = MutableStateFlow(false)
    private val isResetInProgress = MutableStateFlow(false)
    private val checkInProgressIds = mutableSetOf<Uuid>()
    private val deleteInProgressIds = mutableSetOf<Uuid>()

    private val itemList: Flow<List<ChecklistItem>> =
        sort.flatMapLatest { sort ->
            getChecklistItemUseCase(parameter = GetChecklistItemUseCase.Parameter(checklistId = checklistId, sort = sort))
                .map { result -> result.getOrElse { emptyList() } }
        }

    val uiState: StateFlow<ChecklistDetailItemUiState> =
        combine(
            itemList,
            sort,
            isAddInProgress,
            isResetInProgress,
        ) { itemList, sort, isAddInProgress, isResetInProgress ->
            ChecklistDetailItemUiState(
                itemList = itemList,
                isLoaded = true,
                sort = sort,
                isAddInProgress = isAddInProgress,
                isResetInProgress = isResetInProgress,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = ChecklistDetailItemUiState(),
        )

    private val _effect = Channel<ChecklistDetailItemEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistDetailItemEffect> = _effect.receiveAsFlow()

    fun selectSort(sort: ChecklistItemSort) {
        this.sort.value = sort
    }

    fun add(content: String) {
        if (isAddInProgress.value) return

        viewModelScope.launch {
            isAddInProgress.value = true
            try {
                addChecklistItemUseCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = checklistId, content = content))
                    .onSuccess { _effect.send(ChecklistDetailItemEffect.AddSucceeded) }
            } finally {
                isAddInProgress.value = false
            }
        }
    }

    fun toggle(
        id: Uuid,
        isChecked: Boolean,
    ) {
        if (!checkInProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                if (isChecked) {
                    uncheckChecklistItemUseCase(parameter = id)
                } else {
                    checkChecklistItemUseCase(parameter = id)
                }
            } finally {
                checkInProgressIds.remove(id)
            }
        }
    }

    fun delete(itemId: Uuid) {
        if (!deleteInProgressIds.add(itemId)) return

        viewModelScope.launch {
            try {
                deleteChecklistItemUseCase(parameter = itemId)
            } finally {
                deleteInProgressIds.remove(itemId)
            }
        }
    }

    fun reset() {
        if (isResetInProgress.value) return

        viewModelScope.launch {
            isResetInProgress.value = true
            try {
                resetChecklistUseCase(parameter = checklistId)
            } finally {
                isResetInProgress.value = false
            }
        }
    }
}
