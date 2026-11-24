package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountDDaySyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountDDaySyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountDDaySyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<DDayLocalEntity> = database.accountDDaySyncDao().findPending(accountId = accountId)
}
