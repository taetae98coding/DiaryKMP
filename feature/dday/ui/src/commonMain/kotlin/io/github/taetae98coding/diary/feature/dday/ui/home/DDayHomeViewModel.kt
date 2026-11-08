package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class DDayHomeViewModel(
    getDDayFilterUseCase: GetDDayFilterUseCase,
    getDDayFilterTagIdUseCase: GetDDayFilterTagIdUseCase,
    pageDDayUseCase: PageDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    private val restoreDDayUseCase: RestoreDDayUseCase,
) : ViewModel() {
    val ddayPagingData: Flow<PagingData<DDay>> =
        pageDDayUseCase(parameter = Unit)
            // 조건 없는 PagingData.empty()는 불러오기 상태를 싣지 않아 목록이 처음의 불러오는 중에 머물러 빈 상태로 바뀌지 않는다.
            .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
            .cachedIn(viewModelScope)

    val filterUiState: StateFlow<DDayHomeScaffoldFilterUiState> =
        combine(
            getDDayFilterUseCase(parameter = Unit),
            getDDayFilterTagIdUseCase(parameter = Unit),
        ) { tagListResult, storedTagIdSetResult ->
            DDayHomeScaffoldFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                storedTagIdSet = storedTagIdSetResult.getOrDefault(emptySet()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = DDayHomeScaffoldFilterUiState(isLoaded = false),
        )

    private val _effect = Channel<DDayListEffect>(Channel.BUFFERED)
    val effect: Flow<DDayListEffect> = _effect.receiveAsFlow()

    private val inProgressIdSet = mutableSetOf<Uuid>()

    fun delete(id: Uuid) {
        if (!inProgressIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteDDayUseCase(parameter = id)
                    .onSuccess { _effect.send(DDayListEffect.Deleted(id = id)) }
            } finally {
                inProgressIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreDDayUseCase(parameter = id)
            } finally {
                inProgressIdSet.remove(id)
            }
        }
    }
}

private val LOADED_EMPTY_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
