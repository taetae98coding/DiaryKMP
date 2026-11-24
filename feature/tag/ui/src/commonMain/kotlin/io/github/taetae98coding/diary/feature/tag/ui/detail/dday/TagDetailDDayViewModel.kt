@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

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
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
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
internal class TagDetailDDayViewModel(
    @Assisted tagId: Uuid,
    pageTagDDayUseCase: PageTagDDayUseCase,
    deleteDDayUseCase: DeleteDDayUseCase,
    restoreDDayUseCase: RestoreDDayUseCase,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(tagId: Uuid): TagDetailDDayViewModel
    }

    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    val ddayPagingData: Flow<PagingData<DDay>> =
        scopeUiState
            .flatMapLatest { (scopeValue) ->
                pageTagDDayUseCase(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = scopeValue))
                    .map { result -> result.getOrElse { PagingData.empty() } }
            }.cachedIn(viewModelScope)

    private val ddayActions =
        ListItemDeleteActions<DDayListEffect>(
            deleteUseCase = deleteDDayUseCase,
            restoreUseCase = restoreDDayUseCase,
            deletedEffect = DDayListEffect::Deleted,
        )

    val effect: Flow<DDayListEffect> = ddayActions.effect

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { ddayActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { ddayActions.restore(id = id) }
    }
}
