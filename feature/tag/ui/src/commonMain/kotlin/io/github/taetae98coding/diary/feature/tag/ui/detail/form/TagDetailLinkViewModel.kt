@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetLinkedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagLinkSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceReportedSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailLinkViewModel(
    @InjectedParam private val id: Uuid,
    pageTagLinkSelectableTagUseCase: PageTagLinkSelectableTagUseCase,
    getLinkedTagUseCase: GetLinkedTagUseCase,
    private val addTagLinkUseCase: AddTagLinkUseCase,
    private val removeTagLinkUseCase: RemoveTagLinkUseCase,
) : ViewModel() {
    // 화면이 검색어를 알려 주기 전에는 조회하지 않는다. 기준은 debounceReportedSearchQuery를 따른다.
    private val query = MutableStateFlow<String?>(null)

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceReportedSearchQuery()
            .flatMapLatest { value ->
                pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = value))
            }.mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<TagLinkInputUiState> =
        getLinkedTagUseCase(parameter = id)
            .map { result -> TagLinkInputUiState(linkedTagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagLinkInputUiState(),
            )

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun link(tagId: Uuid) {
        viewModelScope.launch {
            addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = tagId))
        }
    }

    fun unlink(tagId: Uuid) {
        viewModelScope.launch {
            removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = tagId))
        }
    }
}
