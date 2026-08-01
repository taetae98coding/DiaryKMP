package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagTransaction
import io.github.taetae98coding.diary.core.mapper.tag.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTagRepositoryImpl(
    private val accountMemoTagLocalDataSource: AccountMemoTagLocalDataSource,
    private val accountMemoTagTransaction: AccountMemoTagTransaction,
) : AccountMemoTagRepository {
    override fun getTagList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Tag>> =
        accountMemoTagLocalDataSource
            .getTagList(
                accountId = account.id,
                memoId = memoId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableTag(
        account: Account,
        memoId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMemoTagLocalDataSource.pageSelectableTag(
                    accountId = account.id,
                    memoId = memoId,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun findTagIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid> =
        accountMemoTagLocalDataSource
            .findTagIdList(
                accountId = account.id,
                memoId = memoId,
            ).toSet()

    override suspend fun upsert(
        account: Account,
        memoId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountMemoTagTransaction.upsert(
            accountId = account.id,
            memoId = memoId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    override suspend fun updatePrimaryTagId(
        account: Account,
        memoId: Uuid,
        primaryTagId: Uuid?,
        updatedAt: Instant,
    ) {
        accountMemoTagTransaction.updatePrimaryTagId(
            accountId = account.id,
            memoId = memoId,
            primaryTagId = primaryTagId,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
