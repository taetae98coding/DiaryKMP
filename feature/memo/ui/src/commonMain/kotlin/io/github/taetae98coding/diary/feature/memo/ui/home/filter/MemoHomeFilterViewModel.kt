package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoDateExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPlaceExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoTagExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectMemoFilterTagUseCase
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
internal class MemoHomeFilterViewModel(
    pageTagUseCase: PageTagUseCase,
    getMemoFilterUseCase: GetMemoFilterUseCase,
    getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase,
    private val selectMemoFilterTagUseCase: SelectMemoFilterTagUseCase,
    private val unselectMemoFilterTagUseCase: UnselectMemoFilterTagUseCase,
    private val unselectAllMemoFilterTagUseCase: UnselectAllMemoFilterTagUseCase,
    private val setMemoDateExistenceFilterUseCase: SetMemoDateExistenceFilterUseCase,
    private val setMemoTagExistenceFilterUseCase: SetMemoTagExistenceFilterUseCase,
    private val setMemoPlaceExistenceFilterUseCase: SetMemoPlaceExistenceFilterUseCase,
) : ViewModel() {
    val tagPagingData: Flow<PagingData<Tag>> =
        pageTagUseCase(parameter = "")
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<MemoHomeFilterUiState> =
        combine(
            getMemoFilterUseCase(parameter = Unit),
            getMemoExistenceFilterUseCase(parameter = Unit),
        ) { tagListResult, existenceResult ->
            MemoHomeFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                existence = existenceResult.getOrDefault(MemoExistenceFilter()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = MemoHomeFilterUiState(),
        )

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private var isUnselectAllTagInProgress = false
    private val inProgressSetDateExistenceSet = mutableSetOf<MemoFilterExistence>()
    private val inProgressSetTagExistenceSet = mutableSetOf<MemoFilterExistence>()
    private val inProgressSetPlaceExistenceSet = mutableSetOf<MemoFilterExistence>()

    fun selectTag(id: Uuid) {
        if (!inProgressSelectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                selectMemoFilterTagUseCase(parameter = id)
            } finally {
                inProgressSelectTagSet.remove(id)
            }
        }
    }

    fun unselectTag(id: Uuid) {
        if (!inProgressUnselectTagSet.add(id)) return

        viewModelScope.launch {
            try {
                unselectMemoFilterTagUseCase(parameter = id)
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
                unselectAllMemoFilterTagUseCase(parameter = Unit)
            } finally {
                isUnselectAllTagInProgress = false
            }
        }
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
