package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.calendarfilter.datasource.AccountCalendarFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarFilterRepositoryImpl(
    private val accountCalendarFilterLocalDataSource: AccountCalendarFilterLocalDataSource,
) : AccountCalendarFilterRepository {
    override fun getTagList(account: Account): Flow<List<Tag>> =
        accountCalendarFilterLocalDataSource
            .getTagList(accountId = account.id)
            .map { tagList ->
                tagList.map { tag -> tag.toDomain() }
            }

    override suspend fun upsert(
        account: Account,
        tagId: Uuid,
    ) {
        accountCalendarFilterLocalDataSource.upsert(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun delete(
        account: Account,
        tagId: Uuid,
    ) {
        accountCalendarFilterLocalDataSource.delete(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(account: Account) {
        accountCalendarFilterLocalDataSource.deleteAll(accountId = account.id)
    }
}
