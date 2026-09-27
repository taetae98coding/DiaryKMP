package io.github.taetae98coding.diary.data.qr.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.data.qr.mapper.toDomain
import io.github.taetae98coding.diary.data.qr.mapper.toLocal
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountQrRepositoryImpl(
    private val accountQrLocalDataSource: AccountQrLocalDataSource,
    private val accountQrTransaction: AccountQrTransaction,
) : AccountQrRepository {
    override fun page(account: Account): Flow<PagingData<Qr>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { accountQrLocalDataSource.page(accountId = account.id) },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun upsert(
        account: Account,
        qr: Qr,
    ) {
        accountQrTransaction.upsert(
            accountId = account.id,
            qrList = listOf(qr.toLocal()),
        )
    }

    override suspend fun updateDeleted(
        account: Account,
        qrId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountQrTransaction.updateDeleted(
            accountId = account.id,
            qrId = qrId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
