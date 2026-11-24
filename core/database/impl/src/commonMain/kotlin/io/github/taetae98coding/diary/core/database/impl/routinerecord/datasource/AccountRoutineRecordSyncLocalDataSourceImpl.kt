package io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountRoutineRecordSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineRecordSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<RoutineRecordLocalEntity> = database.accountRoutineRecordSyncDao().findPending(accountId = accountId)
}
