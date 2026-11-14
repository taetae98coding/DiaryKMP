package io.github.taetae98coding.diary.core.network.impl.routine.datasource

import io.github.taetae98coding.diary.core.network.api.routine.datasource.RoutineRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutinePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routine.entity.RoutinePullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routine.entity.RoutinePushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class RoutineRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : RoutineRemoteDataSource {
    override suspend fun push(routineList: List<RoutineRemoteEntity>) {
        supabaseFunction(
            function = PUSH_ROUTINE_FUNCTION,
            body = RoutinePushRequestRemoteEntity(routineList = routineList),
        )
    }

    override suspend fun pull(usn: Long): List<RoutinePullRemoteEntity> =
        supabaseFunction(
            function = PULL_ROUTINE_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<RoutinePullResponseRemoteEntity>().routineList

    private companion object {
        const val PUSH_ROUTINE_FUNCTION: String = "v1-sync-push-routine"
        const val PULL_ROUTINE_FUNCTION: String = "v1-sync-pull-routine"
    }
}
