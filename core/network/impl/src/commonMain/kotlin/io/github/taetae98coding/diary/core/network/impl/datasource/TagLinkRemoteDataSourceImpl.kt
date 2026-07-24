package io.github.taetae98coding.diary.core.network.impl.datasource

import io.github.taetae98coding.diary.core.network.api.taglink.datasource.TagLinkRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.TagLinkPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.TagLinkPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class TagLinkRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : TagLinkRemoteDataSource {
    override suspend fun push(tagLinkList: List<TagLinkRemoteEntity>) {
        supabaseFunction(
            function = PUSH_TAG_LINK_FUNCTION,
            body = TagLinkPushRequestRemoteEntity(tagLinkList = tagLinkList),
        )
    }

    override suspend fun pull(usn: Long): List<TagLinkPullRemoteEntity> =
        supabaseFunction(
            function = PULL_TAG_LINK_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<TagLinkPullResponseRemoteEntity>().tagLinkList

    private companion object {
        const val PUSH_TAG_LINK_FUNCTION: String = "v1-sync-push-tag-link"
        const val PULL_TAG_LINK_FUNCTION: String = "v1-sync-pull-tag-link"
    }
}
