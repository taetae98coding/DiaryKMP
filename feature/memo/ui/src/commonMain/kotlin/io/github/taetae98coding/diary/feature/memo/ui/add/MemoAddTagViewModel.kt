@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagSelection
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceReportedSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    val selection: StateFlow<MemoTagSelection>
        field =
        MutableStateFlow(
            MemoTagSelection(
                tagIdSet = setOfNotNull(initialPrimaryTagId),
                primaryTagId = initialPrimaryTagId,
            ),
        )

    // 화면이 검색어를 알려 주기 전에는 조회하지 않는다. 기준은 debounceReportedSearchQuery를 따른다.
    private val query = MutableStateFlow<String?>(null)

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceReportedSearchQuery()
            .flatMapLatest { value -> pageTagUseCase(parameter = value) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    private val selectedTagList: Flow<List<Tag>> =
        selection
            .flatMapLatest { value -> getSelectedTagUseCase(parameter = value.tagIdSet) }
            .map { result -> result.getOrNull().orEmpty() }

    val uiState: StateFlow<MemoTagInputUiState> =
        combine(selectedTagList, selection) { selectedTagList, selection ->
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
        this.query.value = query
    }

    fun selectTag(id: Uuid) {
        selection.update { value -> value.copy(tagIdSet = value.tagIdSet + id) }
    }

    fun unselectTag(id: Uuid) {
        selection.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet - id,
                primaryTagId = value.primaryTagId?.takeIf { primaryTagId -> primaryTagId != id },
            )
        }
    }

    fun selectPrimaryTag(id: Uuid) {
        selection.update { value ->
            value.copy(
                tagIdSet = value.tagIdSet + id,
                primaryTagId = id,
            )
        }
    }

    fun unselectPrimaryTag() {
        selection.update { value -> value.copy(primaryTagId = null) }
    }

    private fun MemoTagSelection.selectableIn(tagList: List<Tag>): MemoTagSelection {
        val selectableIdSet = tagList.mapTo(mutableSetOf()) { tag -> tag.id }

        return MemoTagSelection(
            tagIdSet = tagIdSet.intersect(selectableIdSet),
            primaryTagId = primaryTagId?.takeIf { tagId -> tagId in selectableIdSet },
        )
    }
}
