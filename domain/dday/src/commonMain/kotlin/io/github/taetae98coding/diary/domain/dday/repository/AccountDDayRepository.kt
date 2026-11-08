package io.github.taetae98coding.diary.domain.dday.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountDDayRepository {
    public fun page(account: Account): Flow<PagingData<DDay>>

    public fun find(
        account: Account,
        ddayId: Uuid,
    ): Flow<DDay?>

    public suspend fun upsert(
        account: Account,
        dday: DDay,
        tagIdSet: Set<Uuid>,
    )

    public suspend fun updateDetail(
        account: Account,
        ddayId: Uuid,
        detail: DDayDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        ddayId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
