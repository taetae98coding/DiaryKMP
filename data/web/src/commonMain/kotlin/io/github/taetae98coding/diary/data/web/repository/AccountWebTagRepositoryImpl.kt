package io.github.taetae98coding.diary.data.web.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.domain.web.repository.AccountWebTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTagRepositoryImpl(
    private val accountWebTagLocalDataSource: AccountWebTagLocalDataSource,
    private val accountWebTagTransaction: AccountWebTagTransaction,
) : AccountWebTagRepository {
    override fun getTagList(
        account: Account,
        webId: Uuid,
    ): Flow<List<Tag>> =
        accountWebTagLocalDataSource
            .getTagList(
                accountId = account.id,
                webId = webId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableTag(
        account: Account,
        webId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountWebTagLocalDataSource.pageSelectableTag(
                    accountId = account.id,
                    webId = webId,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun upsert(
        account: Account,
        webId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountWebTagTransaction.upsert(
            accountId = account.id,
            webId = webId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
