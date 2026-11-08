package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.SelectDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectAllDDayFilterTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnselectDDayFilterTagUseCase
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
internal class DDayHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getDDayFilterUseCase: GetDDayFilterUseCase,
    private val selectDDayFilterTagUseCase: SelectDDayFilterTagUseCase,
    private val unselectDDayFilterTagUseCase: UnselectDDayFilterTagUseCase,
    private val unselectAllDDayFilterTagUseCase: UnselectAllDDayFilterTagUseCase,
) : ViewModel() {
    val tagPagingData: Flow<PagingData<Tag>> =
        pageTagUseCase(parameter = "")
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<DDayHomeFilterUiState> =
        getDDayFilterUseCase(parameter = Unit)
            .map { result ->
                DDayHomeFilterUiState(
                    selectedTagIdSet =
                        result
                            .getOrDefault(emptyList())
                            .map { tag -> tag.id }
                            .toSet(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = DDayHomeFilterUiState(),
            )

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private var isUnselectAllTagInProgress = false

    fun selectTag(id: Uuid) {
        if (!inProgressSelectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                selectDDayFilterTagUseCase(parameter = id)
            } finally {
                inProgressSelectTagSet.remove(id)
            }
        }
    }

    fun unselectTag(id: Uuid) {
        if (!inProgressUnselectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                unselectDDayFilterTagUseCase(parameter = id)
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
                unselectAllDDayFilterTagUseCase(parameter = Unit)
            } finally {
                isUnselectAllTagInProgress = false
            }
        }
    }
}
