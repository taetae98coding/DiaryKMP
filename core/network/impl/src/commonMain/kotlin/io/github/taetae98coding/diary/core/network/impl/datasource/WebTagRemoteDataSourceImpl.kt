package io.github.taetae98coding.diary.core.network.impl.datasource

import io.github.taetae98coding.diary.core.network.api.webtag.datasource.WebTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.WebTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.WebTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class WebTagRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : WebTagRemoteDataSource {
    override suspend fun push(webTagList: List<WebTagRemoteEntity>) {
        supabaseFunction(
            function = PUSH_WEB_TAG_FUNCTION,
            body = WebTagPushRequestRemoteEntity(webTagList = webTagList),
        )
    }

    override suspend fun pull(usn: Long): List<WebTagPullRemoteEntity> =
        supabaseFunction(
            function = PULL_WEB_TAG_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<WebTagPullResponseRemoteEntity>().webTagList

    private companion object {
        const val PUSH_WEB_TAG_FUNCTION: String = "v1-sync-push-web-tag"
        const val PULL_WEB_TAG_FUNCTION: String = "v1-sync-pull-web-tag"
    }
}
