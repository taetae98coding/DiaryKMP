package io.github.taetae98coding.diary.data.routine.repository

import io.github.taetae98coding.diary.core.database.api.routinefilter.datasource.AccountRoutineFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.data.routine.mapper.toLocal
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineFilterRepositoryImpl(
    private val accountRoutineFilterLocalDataSource: AccountRoutineFilterLocalDataSource,
) : AccountRoutineFilterRepository {
    override fun getTagList(account: Account): Flow<List<Tag>> =
        accountRoutineFilterLocalDataSource
            .getTagList(accountId = account.id)
            .map { tagList ->
                tagList.map { tag -> tag.toDomain() }
            }

    override fun getTagIdSet(account: Account): Flow<Set<Uuid>> =
        accountRoutineFilterLocalDataSource
            .getTagIdList(accountId = account.id)
            .map { tagIdList -> tagIdList.toSet() }

    override suspend fun upsert(
        account: Account,
        tagId: Uuid,
    ) {
        accountRoutineFilterLocalDataSource.upsert(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun delete(
        account: Account,
        tagId: Uuid,
    ) {
        accountRoutineFilterLocalDataSource.delete(
            accountId = account.id,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(account: Account) {
        accountRoutineFilterLocalDataSource.deleteAll(accountId = account.id)
    }

    override fun getViewScope(account: Account): Flow<RoutineViewScope> =
        accountRoutineFilterLocalDataSource
            .findViewScope(accountId = account.id)
            .map { local -> local?.toDomain() ?: ROUTINE_DEFAULT_VIEW_SCOPE }

    override suspend fun upsertViewScope(
        account: Account,
        viewScope: RoutineViewScope,
    ) {
        accountRoutineFilterLocalDataSource.upsertViewScope(
            accountId = account.id,
            viewScope = viewScope.toLocal(),
        )
    }
}
