package io.github.taetae98coding.diary.core.network.api.routinerecord.datasource

import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity

public interface RoutineRecordRemoteDataSource {
    public suspend fun push(routineRecordList: List<RoutineRecordRemoteEntity>)

    public suspend fun pull(usn: Long): List<RoutineRecordPullRemoteEntity>
}
