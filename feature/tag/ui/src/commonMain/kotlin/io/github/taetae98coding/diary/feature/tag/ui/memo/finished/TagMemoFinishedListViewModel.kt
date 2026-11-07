package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.MemoListState
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
internal class TagMemoFinishedListViewModel(
    @InjectedParam tagId: Uuid,
    pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    findTagUseCase: FindTagUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    private val memoListState =
        MemoListState(
            pageMemo = { sort -> pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = sort)) },
        )

    private val memoActions =
        ListItemActions(
            finishUseCase = finishMemoUseCase,
            restartUseCase = restartMemoUseCase,
            deleteUseCase = deleteMemoUseCase,
            restoreUseCase = restoreMemoUseCase,
            restartedEffect = TagMemoFinishedListEffect::Restarted,
            deletedEffect = TagMemoFinishedListEffect::Deleted,
        )

    val uiState: StateFlow<TagMemoFinishedListUiState> =
        findTagUseCase(parameter = tagId)
            .map { result ->
                result
                    .getOrNull()
                    ?.let { tag -> TagMemoFinishedListUiState(title = tag.detail.emojiWithTitle) }
                    ?: TagMemoFinishedListUiState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagMemoFinishedListUiState(),
            )

    val sortUiState: StateFlow<ListSortUiState> = memoListState.sortUiState
    val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)
    val effect: Flow<TagMemoFinishedListEffect> = memoActions.effect

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
