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
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class RoutineHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getRoutineFilterUseCase: GetRoutineFilterUseCase,
    getRoutineViewScopeUseCase: GetRoutineViewScopeUseCase,
    private val selectRoutineFilterTagUseCase: SelectRoutineFilterTagUseCase,
    private val unselectRoutineFilterTagUseCase: UnselectRoutineFilterTagUseCase,
    private val unselectAllRoutineFilterTagUseCase: UnselectAllRoutineFilterTagUseCase,
    private val setRoutineViewScopeUseCase: SetRoutineViewScopeUseCase,
) : ViewModel() {
    val tagPagingData: Flow<PagingData<Tag>> =
        pageTagUseCase(parameter = "")
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<RoutineHomeFilterUiState> =
        combine(
            getRoutineFilterUseCase(),
            getRoutineViewScopeUseCase(),
        ) { tagListResult, viewScopeResult ->
            RoutineHomeFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                viewScope = viewScopeResult.getOrDefault(ROUTINE_DEFAULT_VIEW_SCOPE),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = RoutineHomeFilterUiState(),
        )

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private var isUnselectAllTagInProgress = false
    private var isSetViewScopeInProgress = false

    fun selectTag(id: Uuid) {
        if (!inProgressSelectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                selectRoutineFilterTagUseCase(parameter = id)
            } finally {
                inProgressSelectTagSet.remove(id)
            }
        }
    }

    fun unselectTag(id: Uuid) {
        if (!inProgressUnselectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                unselectRoutineFilterTagUseCase(parameter = id)
            } finally {
                inProgressUnselectTagSet.remove(id)
            }
        }
    }

    fun unselectAllTag() {
        if (isUnselectAllTagInProgress) return
        isUnselectAllTagInProgress = true

        viewModelScope.launch {
            try {
                unselectAllRoutineFilterTagUseCase()
            } finally {
                isUnselectAllTagInProgress = false
            }
        }
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
