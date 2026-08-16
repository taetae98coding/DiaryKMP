package io.github.taetae98coding.diary.domain.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import kotlinx.coroutines.flow.Flow

public interface SearchMemoRepository {
    public fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Memo>>
}
