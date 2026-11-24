package io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routinerecord.datasource.AccountRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountRoutineRecordLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineRecordLocalDataSource {
    override fun page(
        accountId: Uuid,
        routineId: Uuid,
    ): PagingSource<Int, RoutineRecordLocalEntity> =
        database.accountRoutineRecordDao().page(
            accountId = accountId,
            routineId = routineId,
        )

    override fun find(
        accountId: Uuid,
        recordId: Uuid,
    ): Flow<RoutineRecordLocalEntity?> =
        database.accountRoutineRecordDao().find(
            accountId = accountId,
            recordId = recordId,
        )
}
