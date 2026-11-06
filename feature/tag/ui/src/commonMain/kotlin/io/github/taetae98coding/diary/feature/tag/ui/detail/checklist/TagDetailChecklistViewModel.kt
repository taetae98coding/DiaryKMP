@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailChecklistViewModel(
    @InjectedParam tagId: Uuid,
    pageChecklistUseCase: PageTagChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val restoreChecklistUseCase: RestoreChecklistUseCase,
) : ViewModel() {
    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val checklistPagingData: Flow<PagingData<ChecklistSummary>> =
        scopeUiState
            .flatMapLatest { (scopeValue) ->
                pageChecklistUseCase(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = scopeValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val _effect = Channel<ChecklistListEffect>(Channel.BUFFERED)
    val effect: Flow<ChecklistListEffect> = _effect.receiveAsFlow()

    private val inProgressDeleteIdSet = mutableSetOf<Uuid>()
    private val inProgressRestoreIdSet = mutableSetOf<Uuid>()

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        if (!inProgressDeleteIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteChecklistUseCase(parameter = id)
                    .onSuccess { _effect.send(ChecklistListEffect.Deleted(id = id)) }
            } finally {
                inProgressDeleteIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressRestoreIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreChecklistUseCase(parameter = id)
            } finally {
                inProgressRestoreIdSet.remove(id)
            }
        }
    }
}
