package io.github.taetae98coding.diary.data.tag.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagTransaction
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.data.tag.mapper.toLocal
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountTagRepositoryImpl(
    private val accountTagLocalDataSource: AccountTagLocalDataSource,
    private val accountTagTransaction: AccountTagTransaction,
) : AccountTagRepository {
    override fun get(
        account: Account,
        tagIdSet: Set<Uuid>,
    ): Flow<List<Tag>> =
        accountTagLocalDataSource
            .get(
                accountId = account.id,
                tagIdSet = tagIdSet,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun pageTopLevel(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagLocalDataSource.pageTopLevel(
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
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagLocalDataSource.pageFinished(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun find(
        account: Account,
        tagId: Uuid,
    ): Flow<Tag?> =
        accountTagLocalDataSource
            .find(
                accountId = account.id,
                tagId = tagId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        tag: Tag,
        linkedTagIdSet: Set<Uuid>,
    ) {
        accountTagTransaction.upsert(
            accountId = account.id,
            tagList = listOf(tag.toLocal()),
            tagLinkList =
                linkedTagIdSet.map { toTagId ->
                    TagLinkLocalEntity(
                        fromTagId = tag.id,
                        toTagId = toTagId,
                        isDeleted = false,
                        updatedAt = tag.updatedAt,
                        createdAt = tag.createdAt,
                    )
                },
        )
    }

    override suspend fun updateFinished(
        account: Account,
        tagId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        accountTagTransaction.updateFinished(
            accountId = account.id,
            tagId = tagId,
            isFinished = isFinished,
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountTagTransaction.updateDeleted(
            accountId = account.id,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    override suspend fun updateDetail(
        account: Account,
        tagId: Uuid,
        detail: TagDetail,
        updatedAt: Instant,
    ): Int =
        accountTagTransaction.updateDetail(
            accountId = account.id,
            tagId = tagId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
