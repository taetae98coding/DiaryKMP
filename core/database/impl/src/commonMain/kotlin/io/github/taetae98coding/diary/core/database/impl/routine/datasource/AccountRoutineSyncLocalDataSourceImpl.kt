package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountRoutineSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<RoutineLocalEntity> = database.accountRoutineSyncDao().findPending(accountId = accountId)
}
