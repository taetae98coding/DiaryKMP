@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.memo

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.toMemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActionViewModel
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

public abstract class MemoListViewModel<Effect : Any>(
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
    finishedEffect: ((Uuid) -> Effect)? = null,
    restartedEffect: ((Uuid) -> Effect)? = null,
    deletedEffect: ((Uuid) -> Effect)? = null,
) : ListItemActionViewModel<Effect>(
        finishUseCase = finishMemoUseCase,
        restartUseCase = restartMemoUseCase,
        deleteUseCase = deleteMemoUseCase,
        restoreUseCase = restoreMemoUseCase,
        finishedEffect = finishedEffect,
        restartedEffect = restartedEffect,
        deletedEffect = deletedEffect,
    ) {
    public val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState())

    public val memoPagingData: Flow<PagingData<MemoListItem>> =
        sortUiState
            .flatMapLatest { (sort) ->
                pageMemo(sort = sort)
                    .map { result -> result.getOrElse { PagingData.empty() } }
                    .map { pagingData -> pagingData.toMemoListItem(sort = sort) }
            }.cachedIn(viewModelScope)

    protected abstract fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>>

    public fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }
}
