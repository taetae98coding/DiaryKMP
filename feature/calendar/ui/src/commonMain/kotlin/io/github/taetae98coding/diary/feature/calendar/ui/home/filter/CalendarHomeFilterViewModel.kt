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
import io.github.taetae98coding.diary.feature.core.tag.TagFilterState
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
    selectCalendarFilterTagUseCase: SelectCalendarFilterTagUseCase,
    unselectCalendarFilterTagUseCase: UnselectCalendarFilterTagUseCase,
    unselectAllCalendarFilterTagUseCase: UnselectAllCalendarFilterTagUseCase,
) : ViewModel() {
    private val tagFilterState =
        TagFilterState(
            pageTagUseCase = pageTagUseCase,
            getFilterUseCase = getCalendarFilterUseCase,
            selectTagUseCase = selectCalendarFilterTagUseCase,
            unselectTagUseCase = unselectCalendarFilterTagUseCase,
            unselectAllTagUseCase = unselectAllCalendarFilterTagUseCase,
        )

    val tagPagingData: Flow<PagingData<Tag>> = tagFilterState.tagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<CalendarHomeFilterUiState> =
        tagFilterState.selectedTagIdSet
            .map { selectedTagIdSet -> CalendarHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeFilterUiState(),
            )

    fun selectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.select(id = id) }
    }

    fun unselectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.unselect(id = id) }
    }

    fun unselectAllTag() {
        viewModelScope.launch { tagFilterState.unselectAll() }
    }
}
