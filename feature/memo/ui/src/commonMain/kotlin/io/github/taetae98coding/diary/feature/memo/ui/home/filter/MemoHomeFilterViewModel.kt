package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoDateExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPlaceExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoTagExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.TagFilterState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class MemoHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getMemoFilterUseCase: GetMemoFilterUseCase,
    getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase,
    selectMemoFilterTagUseCase: SelectMemoFilterTagUseCase,
    unselectMemoFilterTagUseCase: UnselectMemoFilterTagUseCase,
    unselectAllMemoFilterTagUseCase: UnselectAllMemoFilterTagUseCase,
    private val setMemoDateExistenceFilterUseCase: SetMemoDateExistenceFilterUseCase,
    private val setMemoTagExistenceFilterUseCase: SetMemoTagExistenceFilterUseCase,
    private val setMemoPlaceExistenceFilterUseCase: SetMemoPlaceExistenceFilterUseCase,
) : ViewModel() {
    private val tagFilterState =
        TagFilterState(
            pageTagUseCase = pageTagUseCase,
            getFilterUseCase = getMemoFilterUseCase,
            selectTagUseCase = selectMemoFilterTagUseCase,
            unselectTagUseCase = unselectMemoFilterTagUseCase,
            unselectAllTagUseCase = unselectAllMemoFilterTagUseCase,
        )

    val tagPagingData: Flow<PagingData<Tag>> = tagFilterState.tagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<MemoHomeFilterUiState> =
        combine(
            tagFilterState.selectedTagIdSet,
            getMemoExistenceFilterUseCase(),
        ) { selectedTagIdSet, existenceResult ->
            MemoHomeFilterUiState(
                selectedTagIdSet = selectedTagIdSet,
                existence = existenceResult.getOrDefault(MemoExistenceFilter()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = MemoHomeFilterUiState(),
        )

    private val inProgressSetDateExistenceSet = mutableSetOf<MemoFilterExistence>()
    private val inProgressSetTagExistenceSet = mutableSetOf<MemoFilterExistence>()
    private val inProgressSetPlaceExistenceSet = mutableSetOf<MemoFilterExistence>()

    fun selectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.select(id = id) }
    }

    fun unselectTag(id: Uuid) {
        viewModelScope.launch { tagFilterState.unselect(id = id) }
    }

    fun unselectAllTag() {
        viewModelScope.launch { tagFilterState.unselectAll() }
    }

    fun setDateExistence(existence: MemoFilterExistence) {
        if (!inProgressSetDateExistenceSet.add(existence)) return

        viewModelScope.launch {
            try {
                setMemoDateExistenceFilterUseCase(parameter = existence)
            } finally {
                inProgressSetDateExistenceSet.remove(existence)
            }
        }
    }

    fun setTagExistence(existence: MemoFilterExistence) {
        if (!inProgressSetTagExistenceSet.add(existence)) return

        viewModelScope.launch {
            try {
                setMemoTagExistenceFilterUseCase(parameter = existence)
            } finally {
                inProgressSetTagExistenceSet.remove(existence)
            }
        }
    }

    fun setPlaceExistence(existence: MemoFilterExistence) {
        if (!inProgressSetPlaceExistenceSet.add(existence)) return

        viewModelScope.launch {
            try {
                setMemoPlaceExistenceFilterUseCase(parameter = existence)
            } finally {
                inProgressSetPlaceExistenceSet.remove(existence)
            }
        }
    }
}
