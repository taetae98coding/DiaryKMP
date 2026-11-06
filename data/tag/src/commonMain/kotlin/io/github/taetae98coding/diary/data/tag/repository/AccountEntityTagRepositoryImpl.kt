package io.github.taetae98coding.diary.data.tag.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.entitytag.datasource.AccountEntityTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.data.tag.mapper.toLocal
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.repository.AccountEntityTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountEntityTagRepositoryImpl(
    private val accountEntityTagLocalDataSource: AccountEntityTagLocalDataSource,
    private val accountEntityTagTransaction: AccountEntityTagTransaction,
) : AccountEntityTagRepository {
    override fun getTagList(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
    ): Flow<List<Tag>> =
        accountEntityTagLocalDataSource
            .getTagList(
                accountId = account.id,
                kind = kind.toLocal(),
                entityId = entityId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableTag(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>> =
        pagingFlow(
            pagingSourceFactory = {
                accountEntityTagLocalDataSource.pageSelectableTag(
                    accountId = account.id,
                    kind = kind.toLocal(),
                    entityId = entityId,
                    query = query,
                )
            },
            transform = { local -> local.toDomain() },
        )

    override suspend fun upsert(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountEntityTagTransaction.upsert(
            accountId = account.id,
            kind = kind.toLocal(),
            entityId = entityId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }
}
