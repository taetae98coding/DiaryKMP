package io.github.taetae98coding.diary.core.network.impl.memoweb.datasource

import io.github.taetae98coding.diary.core.network.api.memoweb.datasource.MemoWebRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memoweb.entity.MemoWebPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memoweb.entity.MemoWebPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MemoWebRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MemoWebRemoteDataSource {
    override suspend fun push(memoWebList: List<MemoWebRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MEMO_WEB_FUNCTION,
            body = MemoWebPushRequestRemoteEntity(memoWebList = memoWebList),
        )
    }

    override suspend fun pull(usn: Long): List<MemoWebPullRemoteEntity> =
        supabaseFunction(
            function = PULL_MEMO_WEB_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MemoWebPullResponseRemoteEntity>().memoWebList

    private companion object {
        const val PUSH_MEMO_WEB_FUNCTION: String = "v1-sync-push-memo-web"
        const val PULL_MEMO_WEB_FUNCTION: String = "v1-sync-pull-memo-web"
    }
}
