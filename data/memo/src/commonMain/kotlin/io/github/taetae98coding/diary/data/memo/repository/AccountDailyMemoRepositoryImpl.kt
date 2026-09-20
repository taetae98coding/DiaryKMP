package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountDailyMemoLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountDailyMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory
internal class AccountDailyMemoRepositoryImpl(
    private val accountDailyMemoLocalDataSource: AccountDailyMemoLocalDataSource,
) : AccountDailyMemoRepository {
    override fun get(
        account: Account,
        date: LocalDate,
    ): Flow<List<DailyMemo>> =
        accountDailyMemoLocalDataSource
            .get(
                accountId = account.id,
                date = date,
            ).map { localList -> localList.map { local -> local.toDomain() } }
}
