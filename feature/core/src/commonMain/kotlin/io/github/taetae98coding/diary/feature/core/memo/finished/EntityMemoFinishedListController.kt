package io.github.taetae98coding.diary.feature.core.memo.finished

import io.github.taetae98coding.diary.feature.core.memo.MemoListController
import kotlinx.coroutines.flow.StateFlow

public interface EntityMemoFinishedListController : MemoListController<EntityMemoFinishedListEffect> {
    public val uiState: StateFlow<EntityMemoFinishedListUiState>
}
