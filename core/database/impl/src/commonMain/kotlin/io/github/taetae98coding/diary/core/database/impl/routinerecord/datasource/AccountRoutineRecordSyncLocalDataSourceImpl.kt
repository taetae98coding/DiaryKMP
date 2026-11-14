package io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource

import io.github.taetae98coding.diary.core.database.api.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineRecordSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineRecordSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<RoutineRecordLocalEntity> = database.accountRoutineRecordSyncDao().findPending(accountId = accountId)
}
