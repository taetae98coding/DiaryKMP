package io.github.taetae98coding.diary.core.network.impl.tag.datasource

import io.github.taetae98coding.diary.core.network.api.tag.datasource.TagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.tag.entity.TagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.tag.entity.TagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class TagRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : TagRemoteDataSource {
    override suspend fun push(tagList: List<TagRemoteEntity>) {
        supabaseFunction(
            function = PUSH_TAG_FUNCTION,
            body = TagPushRequestRemoteEntity(tagList = tagList),
        )
    }

    override suspend fun pull(usn: Long): List<TagPullRemoteEntity> =
        supabaseFunction(
            function = PULL_TAG_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<TagPullResponseRemoteEntity>().tagList

    private companion object {
        const val PUSH_TAG_FUNCTION: String = "v1-sync-push-tag"
        const val PULL_TAG_FUNCTION: String = "v1-sync-pull-tag"
    }
}
