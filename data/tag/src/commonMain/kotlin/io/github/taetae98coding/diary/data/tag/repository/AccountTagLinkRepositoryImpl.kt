package io.github.taetae98coding.diary.data.tag.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkTransaction
import io.github.taetae98coding.diary.core.mapper.tag.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountTagLinkRepositoryImpl(
    private val accountTagLinkLocalDataSource: AccountTagLinkLocalDataSource,
    private val accountTagLinkTransaction: AccountTagLinkTransaction,
) : AccountTagLinkRepository {
    override fun getTagList(
        account: Account,
        fromTagId: Uuid,
    ): Flow<List<Tag>> =
        accountTagLinkLocalDataSource
            .getTagList(
                accountId = account.id,
                fromTagId = fromTagId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableTag(
        account: Account,
        fromTagId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagLinkLocalDataSource.pageSelectableTag(
                    accountId = account.id,
                    fromTagId = fromTagId,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun upsert(
        account: Account,
        fromTagId: Uuid,
        toTagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountTagLinkTransaction.upsert(
            accountId = account.id,
            fromTagId = fromTagId,
            toTagId = toTagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
