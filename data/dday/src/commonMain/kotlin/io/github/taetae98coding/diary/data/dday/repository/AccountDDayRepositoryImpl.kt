package io.github.taetae98coding.diary.data.dday.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.transaction.AccountDDayTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toEntityTagLocalList
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountDDayRepositoryImpl(
    private val accountDDayLocalDataSource: AccountDDayLocalDataSource,
    private val accountDDayTransaction: AccountDDayTransaction,
) : AccountDDayRepository {
    override fun page(account: Account): Flow<PagingData<DDay>> =
        pagingFlow(
            pagingSourceFactory = { accountDDayLocalDataSource.page(accountId = account.id) },
            transform = { local -> local.toDomain() },
        )

    override fun find(
        account: Account,
        ddayId: Uuid,
    ): Flow<DDay?> =
        accountDDayLocalDataSource
            .find(
                accountId = account.id,
                ddayId = ddayId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        dday: DDay,
        tagIdSet: Set<Uuid>,
    ) {
        accountDDayTransaction.upsert(
            accountId = account.id,
            dday = dday.toLocal(),
            ddayTagList = tagIdSet.toEntityTagLocalList(entityId = dday.id, updatedAt = dday.updatedAt, createdAt = dday.createdAt),
        )
    }

    override suspend fun updateDetail(
        account: Account,
        ddayId: Uuid,
        detail: DDayDetail,
        updatedAt: Instant,
    ): Int =
        accountDDayTransaction.updateDetail(
            accountId = account.id,
            ddayId = ddayId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        ddayId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountDDayTransaction.updateDeleted(
            accountId = account.id,
            ddayId = ddayId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
