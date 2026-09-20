package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoWebRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoWebRepositoryImpl(
    private val accountMemoWebLocalDataSource: AccountMemoWebLocalDataSource,
    private val accountMemoWebTransaction: AccountMemoWebTransaction,
) : AccountMemoWebRepository {
    override fun getWebList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Web>> =
        accountMemoWebLocalDataSource
            .getWebList(
                accountId = account.id,
                memoId = memoId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableWeb(
        account: Account,
        query: String,
    ): Flow<PagingData<Web>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMemoWebLocalDataSource.pageSelectableWeb(
                    accountId = account.id,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun findWebIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid> =
        accountMemoWebLocalDataSource
            .findWebIdList(
                accountId = account.id,
                memoId = memoId,
            ).toSet()

    override suspend fun upsert(
        account: Account,
        memoId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountMemoWebTransaction.upsert(
            accountId = account.id,
            memoId = memoId,
            webId = webId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
