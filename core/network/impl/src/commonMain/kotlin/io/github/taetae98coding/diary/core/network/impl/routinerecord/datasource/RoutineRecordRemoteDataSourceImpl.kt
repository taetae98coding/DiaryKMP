package io.github.taetae98coding.diary.core.network.impl.routinerecord.datasource

import io.github.taetae98coding.diary.core.network.api.routinerecord.datasource.RoutineRecordRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class RoutineRecordRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : RoutineRecordRemoteDataSource {
    override suspend fun push(routineRecordList: List<RoutineRecordRemoteEntity>) {
        supabaseFunction(
            function = PUSH_ROUTINE_RECORD_FUNCTION,
            body = RoutineRecordPushRequestRemoteEntity(routineRecordList = routineRecordList),
        )
    }

    override suspend fun pull(usn: Long): List<RoutineRecordPullRemoteEntity> =
        supabaseFunction(
            function = PULL_ROUTINE_RECORD_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<RoutineRecordPullResponseRemoteEntity>().routineRecordList

    private companion object {
        const val PUSH_ROUTINE_RECORD_FUNCTION: String = "v1-sync-push-routine-record"
        const val PULL_ROUTINE_RECORD_FUNCTION: String = "v1-sync-pull-routine-record"
    }
}
