package io.github.taetae98coding.diary.feature.core.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

public interface MemoListController<Effect : Any> {
    public val sortUiState: StateFlow<ListSortUiState>
    public val memoPagingData: Flow<PagingData<MemoListItem>>
    public val effect: Flow<Effect>

    public fun select(sort: ListSort)

    public fun finish(id: Uuid)

    public fun restart(id: Uuid)

    public fun delete(id: Uuid)

    public fun restore(id: Uuid)
}
