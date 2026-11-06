package io.github.taetae98coding.diary.feature.place.ui.memo.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListController
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListEffect
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaceMemoFinishedListViewModel(
    @InjectedParam placeId: Uuid,
    pageFinishedPlaceMemoUseCase: PageFinishedPlaceMemoUseCase,
    findPlaceUseCase: FindPlaceUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel(),
    EntityMemoFinishedListController {
    private val memoListState =
        MemoListState(
            pageMemo = { sort -> pageFinishedPlaceMemoUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = sort)) },
        )

    private val memoActions =
        ListItemActions(
            finishUseCase = finishMemoUseCase,
            restartUseCase = restartMemoUseCase,
            deleteUseCase = deleteMemoUseCase,
            restoreUseCase = restoreMemoUseCase,
            restartedEffect = EntityMemoFinishedListEffect::Restarted,
            deletedEffect = EntityMemoFinishedListEffect::Deleted,
        )

    override val uiState: StateFlow<EntityMemoFinishedListUiState> =
        findPlaceUseCase(parameter = placeId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { place -> EntityMemoFinishedListUiState(title = place.detail.title) }
                    ?: EntityMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityMemoFinishedListUiState(),
            )

    override val sortUiState: StateFlow<ListSortUiState> = memoListState.sortUiState
    override val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)
    override val effect: Flow<EntityMemoFinishedListEffect> = memoActions.effect

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
