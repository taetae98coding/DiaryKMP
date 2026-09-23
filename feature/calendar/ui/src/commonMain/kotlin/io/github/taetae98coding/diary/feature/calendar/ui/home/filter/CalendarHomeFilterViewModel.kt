package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class CalendarHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getCalendarFilterUseCase: GetCalendarFilterUseCase,
    private val selectCalendarFilterTagUseCase: SelectCalendarFilterTagUseCase,
    private val unselectCalendarFilterTagUseCase: UnselectCalendarFilterTagUseCase,
    private val unselectAllCalendarFilterTagUseCase: UnselectAllCalendarFilterTagUseCase,
) : ViewModel() {
    val tagPagingData: Flow<PagingData<Tag>> =
        pageTagUseCase(parameter = "")
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<CalendarHomeFilterUiState> =
        getCalendarFilterUseCase(parameter = Unit)
            .map { result ->
                CalendarHomeFilterUiState(
                    selectedTagIdSet =
                        result
                            .getOrDefault(emptyList())
                            .map { tag -> tag.id }
                            .toSet(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeFilterUiState(),
            )

    fun selectTag(id: Uuid) {
        viewModelScope.launch {
            selectCalendarFilterTagUseCase(parameter = id)
        }
    }

    fun unselectTag(id: Uuid) {
        viewModelScope.launch {
            unselectCalendarFilterTagUseCase(parameter = id)
        }
    }

    fun unselectAllTag() {
        viewModelScope.launch {
            unselectAllCalendarFilterTagUseCase(parameter = Unit)
        }
    }
}
