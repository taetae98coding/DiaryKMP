package io.github.taetae98coding.diary.core.network.impl.web.datasource

import io.github.taetae98coding.diary.core.network.api.web.datasource.WebRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.web.entity.WebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.web.entity.WebPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.web.entity.WebPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class WebRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : WebRemoteDataSource {
    override suspend fun push(webList: List<WebRemoteEntity>) {
        supabaseFunction(
            function = PUSH_WEB_FUNCTION,
            body = WebPushRequestRemoteEntity(webList = webList),
        )
    }

    override suspend fun pull(usn: Long): List<WebPullRemoteEntity> =
        supabaseFunction(
            function = PULL_WEB_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<WebPullResponseRemoteEntity>().webList

    private companion object {
        const val PUSH_WEB_FUNCTION: String = "v1-sync-push-web"
        const val PULL_WEB_FUNCTION: String = "v1-sync-pull-web"
    }
}
