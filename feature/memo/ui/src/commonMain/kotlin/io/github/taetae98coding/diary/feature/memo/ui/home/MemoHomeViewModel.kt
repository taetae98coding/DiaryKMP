
package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.memo.MemoListViewModel
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MemoHomeViewModel(
    getMemoFilterUseCase: GetMemoFilterUseCase,
    getMemoFilterTagIdUseCase: GetMemoFilterTagIdUseCase,
    getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase,
    private val pageMemoHomeUseCase: PageMemoHomeUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : MemoListViewModel<MemoListEffect>(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
        finishedEffect = MemoListEffect::Finished,
        deletedEffect = MemoListEffect::Deleted,
    ) {
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

    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageMemoHomeUseCase(parameter = sort)
}
