package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountDDaySyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDDaySyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountDDaySyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<DDayLocalEntity> = database.accountDDaySyncDao().findPending(accountId = accountId)
}
