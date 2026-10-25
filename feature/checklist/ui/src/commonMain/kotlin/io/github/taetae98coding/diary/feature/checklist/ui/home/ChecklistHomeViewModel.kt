package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ChecklistHomeViewModel(
    pageChecklistUseCase: PageChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val restoreChecklistUseCase: RestoreChecklistUseCase,
) : ViewModel() {
    val checklistPagingData: Flow<PagingData<ChecklistSummary>> =
        pageChecklistUseCase(parameter = Unit)
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<ChecklistHomeEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistHomeEffect> = _effect.receiveAsFlow()

    private val inProgressIds = mutableSetOf<Uuid>()

    fun delete(id: Uuid) {
        if (!inProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                deleteChecklistUseCase(parameter = id)
                    .onSuccess { _effect.send(ChecklistHomeEffect.Deleted(id = id)) }
            } finally {
                inProgressIds.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                restoreChecklistUseCase(parameter = id)
            } finally {
                inProgressIds.remove(id)
            }
        }
    }
}
