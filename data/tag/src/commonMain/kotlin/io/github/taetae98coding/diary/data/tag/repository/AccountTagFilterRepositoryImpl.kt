package io.github.taetae98coding.diary.data.tag.repository

import io.github.taetae98coding.diary.core.database.api.tagfilter.datasource.AccountTagFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class AccountTagFilterRepositoryImpl(
    private val accountTagFilterLocalDataSource: AccountTagFilterLocalDataSource,
) : AccountTagFilterRepository {
    override fun getTopLevelOnly(account: Account): Flow<Boolean> =
        accountTagFilterLocalDataSource
            .find(accountId = account.id)
            .map { local -> local?.isTopLevelOnly ?: false }

    override suspend fun upsert(
        account: Account,
        isTopLevelOnly: Boolean,
    ) {
        accountTagFilterLocalDataSource.upsert(
            accountId = account.id,
            isTopLevelOnly = isTopLevelOnly,
        )
    }
}
