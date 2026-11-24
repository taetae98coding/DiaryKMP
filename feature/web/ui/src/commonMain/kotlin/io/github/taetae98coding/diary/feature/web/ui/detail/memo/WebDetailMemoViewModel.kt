package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListController
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
internal class WebDetailMemoViewModel(
    @Assisted webId: Uuid,
    pageWebMemoUseCase: PageWebMemoUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel(),
    MemoListController<MemoListEffect> {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(webId: Uuid): WebDetailMemoViewModel
    }

    private val memoListState =
        MemoListState(
            pageMemo = { sort -> pageWebMemoUseCase(parameter = PageWebMemoUseCase.Parameter(webId = webId, sort = sort)) },
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
