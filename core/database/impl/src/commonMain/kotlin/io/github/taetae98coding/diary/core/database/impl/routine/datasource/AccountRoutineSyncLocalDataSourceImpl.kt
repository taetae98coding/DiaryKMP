package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<RoutineLocalEntity> = database.accountRoutineSyncDao().findPending(accountId = accountId)
}
