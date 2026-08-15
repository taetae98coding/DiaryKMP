package io.github.taetae98coding.diary.domain.web.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountWebRepository {
    public fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Web>>

    public fun get(
        account: Account,
        webIdSet: Set<Uuid>,
    ): Flow<List<Web>>

    public fun find(
        account: Account,
        webId: Uuid,
    ): Flow<Web?>

    public suspend fun upsert(
        account: Account,
        web: Web,
        tagIdSet: Set<Uuid>,
    )

    public suspend fun updateDetail(
        account: Account,
        webId: Uuid,
        detail: WebDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
