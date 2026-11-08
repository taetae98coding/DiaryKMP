package io.github.taetae98coding.diary.data.dday.repository

import io.github.taetae98coding.diary.core.database.api.ddayfilter.datasource.AccountDDayFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDDayFilterRepositoryImpl(
    private val accountDDayFilterLocalDataSource: AccountDDayFilterLocalDataSource,
) : AccountDDayFilterRepository {
    override fun getTagList(account: Account): Flow<List<Tag>> =
        accountDDayFilterLocalDataSource
            .getTagList(accountId = account.id)
            .map { tagList ->
                tagList.map { tag -> tag.toDomain() }
            }

    override fun getTagIdSet(account: Account): Flow<Set<Uuid>> =
        accountDDayFilterLocalDataSource
            .getTagIdList(accountId = account.id)
            .map { tagIdList -> tagIdList.toSet() }

    override suspend fun upsert(
        account: Account,
        tagId: Uuid,
    ) {
        accountDDayFilterLocalDataSource.upsert(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun delete(
        account: Account,
        tagId: Uuid,
    ) {
        accountDDayFilterLocalDataSource.delete(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(account: Account) {
        accountDDayFilterLocalDataSource.deleteAll(accountId = account.id)
    }
}
