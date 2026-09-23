@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
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
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoTagViewModel(
    @InjectedParam private val id: Uuid,
    pageMemoSelectableTagUseCase: PageMemoSelectableTagUseCase,
    getMemoTagUseCase: GetMemoTagUseCase,
    findMemoUseCase: FindMemoUseCase,
    private val addMemoTagUseCase: AddMemoTagUseCase,
    private val removeMemoTagUseCase: RemoveMemoTagUseCase,
    private val setMemoPrimaryTagUseCase: SetMemoPrimaryTagUseCase,
    private val unsetMemoPrimaryTagUseCase: UnsetMemoPrimaryTagUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value ->
                pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = value))
            }.mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<MemoTagInputUiState> =
        combine(
            getMemoTagUseCase(parameter = id),
            findMemoUseCase(parameter = id).map { result -> result.getOrNull()?.primaryTagId },
        ) { memoTagResult, primaryTagId ->
            val selectedTagList = memoTagResult.getOrNull().orEmpty()

            MemoTagInputUiState(
                selectedTagList = selectedTagList,
                primaryTagId = primaryTagId?.takeIf { tagId -> selectedTagList.any { tag -> tag.id == tagId } },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = MemoTagInputUiState(),
        )

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectTag(tagId: Uuid) {
        viewModelScope.launch {
            addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = id, tagId = tagId))
        }
    }

    fun unselectTag(tagId: Uuid) {
        viewModelScope.launch {
            removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = id, tagId = tagId))
        }
    }

    fun selectPrimaryTag(tagId: Uuid) {
        viewModelScope.launch {
            setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = id, tagId = tagId))
        }
    }

    fun unselectPrimaryTag() {
        viewModelScope.launch {
            unsetMemoPrimaryTagUseCase(parameter = id)
        }
    }
}
