package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.AccountMemoFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoFilterRepositoryImpl(
    private val accountMemoFilterLocalDataSource: AccountMemoFilterLocalDataSource,
) : AccountMemoFilterRepository {
    override fun getTagList(account: Account): Flow<List<Tag>> =
        accountMemoFilterLocalDataSource
            .getTagList(accountId = account.id)
            .map { tagList ->
                tagList.map { tag -> tag.toDomain() }
            }

    override fun getTagIdSet(account: Account): Flow<Set<Uuid>> =
        accountMemoFilterLocalDataSource
            .getTagIdList(accountId = account.id)
            .map { tagIdList -> tagIdList.toSet() }

    override suspend fun upsert(
        account: Account,
        tagId: Uuid,
    ) {
        accountMemoFilterLocalDataSource.upsert(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun delete(
        account: Account,
        tagId: Uuid,
    ) {
        accountMemoFilterLocalDataSource.delete(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(account: Account) {
        accountMemoFilterLocalDataSource.deleteAll(accountId = account.id)
    }
}
