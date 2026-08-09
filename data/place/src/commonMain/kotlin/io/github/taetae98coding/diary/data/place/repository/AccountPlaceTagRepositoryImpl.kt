package io.github.taetae98coding.diary.data.place.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagTransaction
import io.github.taetae98coding.diary.core.mapper.tag.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTagRepositoryImpl(
    private val accountPlaceTagLocalDataSource: AccountPlaceTagLocalDataSource,
    private val accountPlaceTagTransaction: AccountPlaceTagTransaction,
) : AccountPlaceTagRepository {
    override fun getTagList(
        account: Account,
        placeId: Uuid,
    ): Flow<List<Tag>> =
        accountPlaceTagLocalDataSource
            .getTagList(
                accountId = account.id,
                placeId = placeId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableTag(
        account: Account,
        placeId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountPlaceTagLocalDataSource.pageSelectableTag(
                    accountId = account.id,
                    placeId = placeId,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun upsert(
        account: Account,
        placeId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountPlaceTagTransaction.upsert(
            accountId = account.id,
            placeId = placeId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
