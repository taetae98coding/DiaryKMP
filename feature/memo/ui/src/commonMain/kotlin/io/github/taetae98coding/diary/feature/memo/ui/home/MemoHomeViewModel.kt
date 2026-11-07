
package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
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
internal class MemoHomeViewModel(
    getMemoFilterUseCase: GetMemoFilterUseCase,
    getMemoFilterTagIdUseCase: GetMemoFilterTagIdUseCase,
    getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase,
    pageMemoHomeUseCase: PageMemoHomeUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    private val memoListState = MemoListState(pageMemo = { sort -> pageMemoHomeUseCase(parameter = sort) })

    private val memoActions =
        ListItemActions(
            finishUseCase = finishMemoUseCase,
            restartUseCase = restartMemoUseCase,
            deleteUseCase = deleteMemoUseCase,
            restoreUseCase = restoreMemoUseCase,
            finishedEffect = MemoListEffect::Finished,
            deletedEffect = MemoListEffect::Deleted,
        )

    val filterUiState: StateFlow<MemoHomeScaffoldFilterUiState> =
        combine(
            getMemoFilterUseCase(parameter = Unit),
            getMemoFilterTagIdUseCase(parameter = Unit),
            getMemoExistenceFilterUseCase(parameter = Unit),
        ) { tagListResult, storedTagIdSetResult, existenceResult ->
            MemoHomeScaffoldFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                storedTagIdSet = storedTagIdSetResult.getOrDefault(emptySet()),
                existence = existenceResult.getOrDefault(MemoExistenceFilter()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = MemoHomeScaffoldFilterUiState(isLoaded = false),
        )

    val sortUiState: StateFlow<ListSortUiState> = memoListState.sortUiState
    val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)
    val effect: Flow<MemoListEffect> = memoActions.effect

    fun select(sort: ListSort) {
        memoListState.select(sort = sort)
    }

    fun finish(id: Uuid) {
        viewModelScope.launch { memoActions.finish(id = id) }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch { memoActions.restart(id = id) }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { memoActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { memoActions.restore(id = id) }
    }
}
