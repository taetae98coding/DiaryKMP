package io.github.taetae98coding.diary.feature.core.memo.finished

import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.core.memo.MemoListViewModel
import kotlinx.coroutines.flow.StateFlow

public abstract class EntityMemoFinishedListViewModel(
    finishMemoUseCase: FinishMemoUseCase,
    restartMemoUseCase: RestartMemoUseCase,
    deleteMemoUseCase: DeleteMemoUseCase,
    restoreMemoUseCase: RestoreMemoUseCase,
) : MemoListViewModel<EntityMemoFinishedListEffect>(
        finishMemoUseCase = finishMemoUseCase,
        restartMemoUseCase = restartMemoUseCase,
        deleteMemoUseCase = deleteMemoUseCase,
        restoreMemoUseCase = restoreMemoUseCase,
        restartedEffect = EntityMemoFinishedListEffect::Restarted,
        deletedEffect = EntityMemoFinishedListEffect::Deleted,
    ) {
    public abstract val uiState: StateFlow<EntityMemoFinishedListUiState>
}
