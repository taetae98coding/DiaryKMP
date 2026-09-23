package io.github.taetae98coding.diary.core.network.impl.memotag.datasource

import io.github.taetae98coding.diary.core.network.api.memotag.datasource.MemoTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memotag.entity.MemoTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memotag.entity.MemoTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MemoTagRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MemoTagRemoteDataSource {
    override suspend fun push(memoTagList: List<MemoTagRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MEMO_TAG_FUNCTION,
            body = MemoTagPushRequestRemoteEntity(memoTagList = memoTagList),
        )
    }

    override suspend fun pull(usn: Long): List<MemoTagPullRemoteEntity> =
        supabaseFunction(
            function = PULL_MEMO_TAG_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MemoTagPullResponseRemoteEntity>().memoTagList

    private companion object {
        const val PUSH_MEMO_TAG_FUNCTION: String = "v1-sync-push-memo-tag"
        const val PULL_MEMO_TAG_FUNCTION: String = "v1-sync-pull-memo-tag"
    }
}
