package io.github.taetae98coding.diary.domain.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow

public interface SearchTagRepository {
    public fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Tag>>
}
