@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    private val selectablePaging =
        MemoSelectablePaging(scope = viewModelScope) { query -> pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = query)) }

    val tagPagingData: Flow<PagingData<Tag>> = selectablePaging.pagingData

    val selectableTagPagingData: Flow<PagingData<Tag>> = selectablePaging.selectablePagingData

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

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private val inProgressSelectPrimaryTagSet = mutableSetOf<Uuid>()
    private var isUnselectPrimaryTagInProgress = false

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectTag(tagId: Uuid) {
        if (!inProgressSelectTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = id, tagId = tagId))
            } finally {
                inProgressSelectTagSet.remove(tagId)
            }
        }
    }

    fun unselectTag(tagId: Uuid) {
        if (!inProgressUnselectTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = id, tagId = tagId))
            } finally {
                inProgressUnselectTagSet.remove(tagId)
            }
        }
    }

    fun selectPrimaryTag(tagId: Uuid) {
        if (!inProgressSelectPrimaryTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = id, tagId = tagId))
            } finally {
                inProgressSelectPrimaryTagSet.remove(tagId)
            }
        }
    }

    fun unselectPrimaryTag() {
        if (isUnselectPrimaryTagInProgress) return
        isUnselectPrimaryTagInProgress = true

        viewModelScope.launch {
            try {
                unsetMemoPrimaryTagUseCase(parameter = id)
            } finally {
                isUnselectPrimaryTagInProgress = false
            }
        }
    }
}
