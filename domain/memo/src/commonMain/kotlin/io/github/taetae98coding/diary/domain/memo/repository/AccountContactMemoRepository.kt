package io.github.taetae98coding.diary.domain.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountContactMemoRepository {
    public fun page(
        account: Account,
        contactId: Uuid,
        sort: ListSort,
    ): Flow<PagingData<Memo>>
}
