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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    private val query =
        MutableSharedFlow<String>(
            replay = 1,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .distinctUntilChanged()
            .debounceReportedSearchQuery()
            .flatMapLatest { value ->
                pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = value))
            }.map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val selectableTagPagingData: Flow<PagingData<Tag>> =
        flowOf("")
            .flatMapLatest { value ->
                pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = value))
            }.map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<TagLinkInputUiState> =
        getLinkedTagUseCase(parameter = id)
            .map { result -> TagLinkInputUiState(linkedTagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagLinkInputUiState(),
            )

    private val inProgressLinkSet = mutableSetOf<Uuid>()
    private val inProgressUnlinkSet = mutableSetOf<Uuid>()

    fun updateQuery(query: String) {
        this.query.tryEmit(query)
    }

    fun link(tagId: Uuid) {
        if (!inProgressLinkSet.add(tagId)) return

        viewModelScope.launch {
            try {
                addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = tagId))
            } finally {
                inProgressLinkSet.remove(tagId)
            }
        }
    }

    fun unlink(tagId: Uuid) {
        if (!inProgressUnlinkSet.add(tagId)) return

        viewModelScope.launch {
            try {
                removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = tagId))
            } finally {
                inProgressUnlinkSet.remove(tagId)
            }
        }
    }
}
