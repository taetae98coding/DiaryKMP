package io.github.taetae98coding.diary.core.network.impl.memo.datasource

import io.github.taetae98coding.diary.core.network.api.memo.datasource.MemoRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memo.entity.MemoPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memo.entity.MemoPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MemoRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MemoRemoteDataSource {
    override suspend fun push(memoList: List<MemoRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MEMO_FUNCTION,
            body = MemoPushRequestRemoteEntity(memoList = memoList),
        )
    }

    override suspend fun pull(usn: Long): List<MemoPullRemoteEntity> =
        supabaseFunction(
            function = PULL_MEMO_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MemoPullResponseRemoteEntity>().memoList

    private companion object {
        const val PUSH_MEMO_FUNCTION: String = "v1-sync-push-memo"
        const val PULL_MEMO_FUNCTION: String = "v1-sync-pull-memo"
    }
}
