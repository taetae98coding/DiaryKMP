package io.github.taetae98coding.diary.domain.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.Web
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoWebRepository {
    public fun getWebList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Web>>

    public fun pageSelectableWeb(
        account: Account,
        query: String,
    ): Flow<PagingData<Web>>

    public suspend fun findWebIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid>

    public suspend fun upsert(
        account: Account,
        memoId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
