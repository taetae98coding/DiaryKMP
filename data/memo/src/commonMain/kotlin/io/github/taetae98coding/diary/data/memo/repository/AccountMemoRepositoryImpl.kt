package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoTransaction
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.data.memo.mapper.toLocal
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoRepositoryImpl(
    private val accountMemoLocalDataSource: AccountMemoLocalDataSource,
    private val accountMemoTransaction: AccountMemoTransaction,
) : AccountMemoRepository {
    override fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMemoLocalDataSource.page(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun pageFinished(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMemoLocalDataSource.pageFinished(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun find(
        account: Account,
        memoId: Uuid,
    ): Flow<Memo?> =
        accountMemoLocalDataSource
            .find(
                accountId = account.id,
                memoId = memoId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        memo: Memo,
        tagIdSet: Set<Uuid>,
        placeIdSet: Set<Uuid>,
        webIdSet: Set<Uuid>,
        contactIdSet: Set<Uuid>,
    ) {
        accountMemoTransaction.upsert(
            accountId = account.id,
            memoList = listOf(memo.toLocal()),
            memoTagList =
                tagIdSet.map { tagId ->
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = memo.updatedAt,
                        createdAt = memo.createdAt,
                    )
                },
            memoPlaceList =
                placeIdSet.map { placeId ->
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = placeId,
                        isDeleted = false,
                        updatedAt = memo.updatedAt,
                        createdAt = memo.createdAt,
                    )
                },
            memoWebList =
                webIdSet.map { webId ->
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = webId,
                        isDeleted = false,
                        updatedAt = memo.updatedAt,
                        createdAt = memo.createdAt,
                    )
                },
            memoContactList =
                contactIdSet.map { contactId ->
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contactId,
                        isDeleted = false,
                        updatedAt = memo.updatedAt,
                        createdAt = memo.createdAt,
                    )
                },
        )
    }

    override suspend fun updateFinished(
        account: Account,
        memoId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        accountMemoTransaction.updateFinished(
            accountId = account.id,
            memoId = memoId,
            isFinished = isFinished,
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        memoId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountMemoTransaction.updateDeleted(
            accountId = account.id,
            memoId = memoId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    override suspend fun updateDetail(
        account: Account,
        memoId: Uuid,
        detail: MemoDetail,
        updatedAt: Instant,
    ): Int =
        accountMemoTransaction.updateDetail(
            accountId = account.id,
            memoId = memoId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )
}
