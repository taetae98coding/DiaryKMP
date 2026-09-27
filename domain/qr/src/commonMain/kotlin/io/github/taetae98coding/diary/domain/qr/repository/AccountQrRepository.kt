package io.github.taetae98coding.diary.domain.qr.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountQrRepository {
    public fun page(account: Account): Flow<PagingData<Qr>>

    public suspend fun upsert(
        account: Account,
        qr: Qr,
    )

    public suspend fun updateDeleted(
        account: Account,
        qrId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
