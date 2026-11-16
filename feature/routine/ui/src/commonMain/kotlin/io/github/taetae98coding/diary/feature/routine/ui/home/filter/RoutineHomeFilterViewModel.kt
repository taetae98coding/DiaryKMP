package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.SelectRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.SetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UnselectAllRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UnselectRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.TagFilterState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getRoutineFilterUseCase: GetRoutineFilterUseCase,
    getRoutineViewScopeUseCase: GetRoutineViewScopeUseCase,
    selectRoutineFilterTagUseCase: SelectRoutineFilterTagUseCase,
    unselectRoutineFilterTagUseCase: UnselectRoutineFilterTagUseCase,
    unselectAllRoutineFilterTagUseCase: UnselectAllRoutineFilterTagUseCase,
    private val setRoutineViewScopeUseCase: SetRoutineViewScopeUseCase,
) : ViewModel() {
    private val tagFilterState =
        TagFilterState(
            pageTagUseCase = pageTagUseCase,
            getFilterUseCase = getRoutineFilterUseCase,
            selectTagUseCase = selectRoutineFilterTagUseCase,
            unselectTagUseCase = unselectRoutineFilterTagUseCase,
            unselectAllTagUseCase = unselectAllRoutineFilterTagUseCase,
        )

    val tagPagingData: Flow<PagingData<Tag>> = tagFilterState.tagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<RoutineHomeFilterUiState> =
        combine(
            tagFilterState.selectedTagIdSet,
            getRoutineViewScopeUseCase(),
        ) { selectedTagIdSet, viewScopeResult ->
            RoutineHomeFilterUiState(
                selectedTagIdSet = selectedTagIdSet,
                viewScope = viewScopeResult.getOrDefault(ROUTINE_DEFAULT_VIEW_SCOPE),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = RoutineHomeFilterUiState(),
        )

    private var isSetViewScopeInProgress = false

    fun selectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.select(id = id) }
    }

    fun unselectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.unselect(id = id) }
    }

    fun unselectAllTag() {
        viewModelScope.launch { tagFilterState.unselectAll() }
    }

    fun setViewScope(viewScope: RoutineViewScope) {
        if (isSetViewScopeInProgress) return
        isSetViewScopeInProgress = true

        viewModelScope.launch {
            try {
                setRoutineViewScopeUseCase(parameter = viewScope)
            } finally {
                isSetViewScopeInProgress = false
            }
        }
    }
}
