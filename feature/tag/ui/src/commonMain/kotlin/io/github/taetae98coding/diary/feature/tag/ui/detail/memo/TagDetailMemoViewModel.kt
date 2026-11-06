@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListController
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class TagDetailMemoViewModel(
    @InjectedParam tagId: Uuid,
    pageTagMemoUseCase: PageTagMemoUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel(),
    MemoListController<MemoListEffect> {
    val scopeUiState: StateFlow<TagDetailScopeUiState>
        field = MutableStateFlow(TagDetailScopeUiState())

    private val memoListState =
        MemoListState(
            pageMemo = { sort ->
                scopeUiState.flatMapLatest { (scopeValue) ->
                    pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = scopeValue, sort = sort))
                }
            },
        )

    private val memoActions =
        ListItemActions(
            finishUseCase = finishMemoUseCase,
            restartUseCase = restartMemoUseCase,
            deleteUseCase = deleteMemoUseCase,
            restoreUseCase = restoreMemoUseCase,
            finishedEffect = MemoListEffect::Finished,
            deletedEffect = MemoListEffect::Deleted,
        )

    override val sortUiState: StateFlow<ListSortUiState> = memoListState.sortUiState
    override val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)
    override val effect: Flow<MemoListEffect> = memoActions.effect

    fun select(scope: TagScope) {
        scopeUiState.value = TagDetailScopeUiState(scope = scope)
    }

    override fun select(sort: ListSort) {
        memoListState.select(sort = sort)
    }

    override fun finish(id: Uuid) {
        viewModelScope.launch { memoActions.finish(id = id) }
    }

    override fun restart(id: Uuid) {
        viewModelScope.launch { memoActions.restart(id = id) }
    }

    override fun delete(id: Uuid) {
        viewModelScope.launch { memoActions.delete(id = id) }
    }

    override fun restore(id: Uuid) {
        viewModelScope.launch { memoActions.restore(id = id) }
    }
}
