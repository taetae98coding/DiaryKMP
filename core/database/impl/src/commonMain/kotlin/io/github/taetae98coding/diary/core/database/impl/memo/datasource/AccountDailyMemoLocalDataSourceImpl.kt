package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountDailyMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDailyMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountDailyMemoLocalDataSource {
    override fun get(
        accountId: Uuid,
        date: LocalDate,
    ): Flow<List<DailyMemoLocalEntity>> =
        database.accountDailyMemoDao().get(
            accountId = accountId,
            date = date,
        )
}
