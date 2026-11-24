@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
internal class TagDetailChecklistViewModel(
    @Assisted tagId: Uuid,
    pageChecklistUseCase: PageTagChecklistUseCase,
    deleteChecklistUseCase: DeleteChecklistUseCase,
    restoreChecklistUseCase: RestoreChecklistUseCase,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(tagId: Uuid): TagDetailChecklistViewModel
    }

    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val checklistPagingData: Flow<PagingData<ChecklistSummary>> =
        scopeUiState
            .flatMapLatest { (scopeValue) ->
                pageChecklistUseCase(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = scopeValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val checklistActions =
        ListItemDeleteActions<ChecklistListEffect>(
            deleteUseCase = deleteChecklistUseCase,
            restoreUseCase = restoreChecklistUseCase,
            deletedEffect = ChecklistListEffect::Deleted,
        )

    val effect: Flow<ChecklistListEffect> = checklistActions.effect

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { checklistActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { checklistActions.restore(id = id) }
    }
}
