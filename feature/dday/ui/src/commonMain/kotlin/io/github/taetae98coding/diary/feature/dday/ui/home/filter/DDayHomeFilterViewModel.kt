package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.SelectDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectAllDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.TagFilterState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class DDayHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getDDayFilterUseCase: GetDDayFilterUseCase,
    selectDDayFilterTagUseCase: SelectDDayFilterTagUseCase,
    unselectDDayFilterTagUseCase: UnselectDDayFilterTagUseCase,
    unselectAllDDayFilterTagUseCase: UnselectAllDDayFilterTagUseCase,
) : ViewModel() {
    private val tagFilterState =
        TagFilterState(
            pageTagUseCase = pageTagUseCase,
            getFilterUseCase = getDDayFilterUseCase,
            selectTagUseCase = selectDDayFilterTagUseCase,
            unselectTagUseCase = unselectDDayFilterTagUseCase,
            unselectAllTagUseCase = unselectAllDDayFilterTagUseCase,
        )

    val tagPagingData: Flow<PagingData<Tag>> = tagFilterState.tagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<DDayHomeFilterUiState> =
        tagFilterState.selectedTagIdSet
            .map { selectedTagIdSet -> DDayHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = DDayHomeFilterUiState(),
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
