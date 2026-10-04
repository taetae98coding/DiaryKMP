
package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.memo.MemoListViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MemoFinishedListViewModel(
    private val pageFinishedMemoUseCase: PageFinishedMemoUseCase,
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : MemoListViewModel<MemoFinishedListEffect>(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
        restartedEffect = MemoFinishedListEffect::Restarted,
        deletedEffect = MemoFinishedListEffect::Deleted,
    ) {
    override fun pageMemo(sort: ListSort): Flow<Result<PagingData<Memo>>> = pageFinishedMemoUseCase(parameter = sort)
}
