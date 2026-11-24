package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class MemoFinishedListViewModel(
    pageFinishedMemoUseCase: PageFinishedMemoUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    private val memoListState = MemoListState(pageMemo = { sort -> pageFinishedMemoUseCase(parameter = sort) })

    private val memoActions =
        ListItemActions(
            finishUseCase = finishMemoUseCase,
            restartUseCase = restartMemoUseCase,
            deleteUseCase = deleteMemoUseCase,
            restoreUseCase = restoreMemoUseCase,
            restartedEffect = MemoFinishedListEffect::Restarted,
            deletedEffect = MemoFinishedListEffect::Deleted,
        )

    val sortUiState: StateFlow<ListSortUiState> = memoListState.sortUiState
    val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)
    val effect: Flow<MemoFinishedListEffect> = memoActions.effect

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
