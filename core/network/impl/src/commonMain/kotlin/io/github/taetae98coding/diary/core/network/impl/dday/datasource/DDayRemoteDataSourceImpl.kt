package io.github.taetae98coding.diary.core.network.impl.dday.datasource

import io.github.taetae98coding.diary.core.network.api.dday.datasource.DDayRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.dday.entity.DDayPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.dday.entity.DDayPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class DDayRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : DDayRemoteDataSource {
    override suspend fun push(ddayList: List<DDayRemoteEntity>) {
        supabaseFunction(
            function = PUSH_DDAY_FUNCTION,
            body = DDayPushRequestRemoteEntity(ddayList = ddayList),
        )
    }

    override suspend fun pull(usn: Long): List<DDayPullRemoteEntity> =
        supabaseFunction(
            function = PULL_DDAY_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<DDayPullResponseRemoteEntity>().ddayList

    private companion object {
        const val PUSH_DDAY_FUNCTION: String = "v1-sync-push-dday"
        const val PULL_DDAY_FUNCTION: String = "v1-sync-pull-dday"
    }
}
