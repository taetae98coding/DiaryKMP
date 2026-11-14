package io.github.taetae98coding.diary.core.database.api.routinerecord.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountRoutineRecordLocalDataSource {
    public fun page(
        accountId: Uuid,
        routineId: Uuid,
    ): PagingSource<Int, RoutineRecordLocalEntity>

    public fun find(
        accountId: Uuid,
        recordId: Uuid,
    ): Flow<RoutineRecordLocalEntity?>
}
