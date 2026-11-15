package io.github.taetae98coding.diary.core.network.api.routine.datasource

import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutinePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity

public interface RoutineRemoteDataSource {
    public suspend fun push(routineList: List<RoutineRemoteEntity>)

    public suspend fun pull(usn: Long): List<RoutinePullRemoteEntity>
}
