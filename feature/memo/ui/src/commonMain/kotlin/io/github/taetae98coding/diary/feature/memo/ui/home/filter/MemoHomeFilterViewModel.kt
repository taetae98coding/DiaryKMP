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
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MemoHomeFilterUiState(),
        )

    fun selectTag(id: Uuid) {
        viewModelScope.launch {
            selectMemoFilterTagUseCase(parameter = id)
        }
    }

    fun unselectTag(id: Uuid) {
        viewModelScope.launch {
            unselectMemoFilterTagUseCase(parameter = id)
        }
    }

    fun unselectAllTag() {
        viewModelScope.launch {
            unselectAllMemoFilterTagUseCase(parameter = Unit)
        }
    }

    fun setDateExistence(existence: MemoFilterExistence) {
        viewModelScope.launch {
            setMemoDateExistenceFilterUseCase(parameter = existence)
        }
    }

    fun setTagExistence(existence: MemoFilterExistence) {
        viewModelScope.launch {
            setMemoTagExistenceFilterUseCase(parameter = existence)
        }
    }

    fun setPlaceExistence(existence: MemoFilterExistence) {
        viewModelScope.launch {
            setMemoPlaceExistenceFilterUseCase(parameter = existence)
        }
    }
}
