@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagSelectionUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoAddTagViewModel(
    @InjectedParam initialPrimaryTagId: Uuid?,
    pageTagUseCase: PageTagUseCase,
    getSelectedTagUseCase: GetSelectedTagUseCase,
) : ViewModel() {
    val selectionUiState: StateFlow<MemoTagSelectionUiState>
        field =
        MutableStateFlow(
            MemoTagSelectionUiState(
                tagIdSet = setOfNotNull(initialPrimaryTagId),
                primaryTagId = initialPrimaryTagId,
            ),
        )

    private val selectablePaging =
        MemoSelectablePaging(scope = viewModelScope) { query -> pageTagUseCase(parameter = query) }

    val tagPagingData: Flow<PagingData<Tag>> = selectablePaging.pagingData

    val selectableTagPagingData: Flow<PagingData<Tag>> = selectablePaging.selectablePagingData

    private val selectedTagList: Flow<List<Tag>> =
        selectionUiState
            .flatMapLatest { value -> getSelectedTagUseCase(parameter = value.tagIdSet) }
            .map { result -> result.getOrNull().orEmpty() }

    val uiState: StateFlow<MemoTagInputUiState> =
        combine(selectedTagList, selectionUiState) { selectedTagList, selection ->
            MemoTagInputUiState(
                selectedTagList = selectedTagList,
                primaryTagId = selection.selectableIn(tagList = selectedTagList).primaryTagId,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = MemoTagInputUiState(),
        )

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectTag(id: Uuid) {
        selectionUiState.update { value -> value.copy(tagIdSet = value.tagIdSet + id) }
    }

    fun unselectTag(id: Uuid) {
        selectionUiState.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet - id,
                primaryTagId = value.primaryTagId?.takeIf { primaryTagId -> primaryTagId != id },
            )
        }
    }

    fun selectPrimaryTag(id: Uuid) {
        selectionUiState.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet + id,
                primaryTagId = id,
            )
        }
    }

    fun unselectPrimaryTag() {
        selectionUiState.update { value -> value.copy(primaryTagId = null) }
    }

    private fun MemoTagSelectionUiState.selectableIn(tagList: List<Tag>): MemoTagSelectionUiState {
        val selectableIdSet = tagList.mapTo(mutableSetOf()) { tag -> tag.id }

        return MemoTagSelectionUiState(
            tagIdSet = tagIdSet.intersect(selectableIdSet),
            primaryTagId = primaryTagId?.takeIf { tagId -> tagId in selectableIdSet },
        )
    }
}
