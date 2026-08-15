package io.github.taetae98coding.diary.data.web.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebTransaction
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.mapper.web.toDomain
import io.github.taetae98coding.diary.core.mapper.web.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountWebRepositoryImpl(
    private val accountWebLocalDataSource: AccountWebLocalDataSource,
    private val accountWebTransaction: AccountWebTransaction,
) : AccountWebRepository {
    override fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Web>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountWebLocalDataSource.page(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun get(
        account: Account,
        webIdSet: Set<Uuid>,
    ): Flow<List<Web>> =
        accountWebLocalDataSource
            .get(
                accountId = account.id,
                webIdSet = webIdSet,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun find(
        account: Account,
        webId: Uuid,
    ): Flow<Web?> =
        accountWebLocalDataSource
            .find(
                accountId = account.id,
                webId = webId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        web: Web,
        tagIdSet: Set<Uuid>,
    ) {
        accountWebTransaction.upsert(
            accountId = account.id,
            webList = listOf(web.toLocal()),
            webTagList =
                tagIdSet.map { tagId ->
                    WebTagLocalEntity(
                        webId = web.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = web.updatedAt,
                        createdAt = web.createdAt,
                    )
                },
        )
    }

    override suspend fun updateDetail(
        account: Account,
        webId: Uuid,
        detail: WebDetail,
        updatedAt: Instant,
    ): Int =
        accountWebTransaction.updateDetail(
            accountId = account.id,
            webId = webId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountWebTransaction.updateDeleted(
            accountId = account.id,
            webId = webId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
