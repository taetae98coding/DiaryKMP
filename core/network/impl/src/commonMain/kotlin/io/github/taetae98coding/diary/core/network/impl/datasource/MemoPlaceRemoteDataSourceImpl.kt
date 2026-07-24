package io.github.taetae98coding.diary.core.network.impl.datasource

import io.github.taetae98coding.diary.core.network.api.memoplace.datasource.MemoPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.MemoPlacePullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.MemoPlacePushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MemoPlaceRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MemoPlaceRemoteDataSource {
    override suspend fun push(memoPlaceList: List<MemoPlaceRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MEMO_PLACE_FUNCTION,
            body = MemoPlacePushRequestRemoteEntity(memoPlaceList = memoPlaceList),
        )
    }

    override suspend fun pull(usn: Long): List<MemoPlacePullRemoteEntity> =
        supabaseFunction(
            function = PULL_MEMO_PLACE_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MemoPlacePullResponseRemoteEntity>().memoPlaceList

    private companion object {
        const val PUSH_MEMO_PLACE_FUNCTION: String = "v1-sync-push-memo-place"
        const val PULL_MEMO_PLACE_FUNCTION: String = "v1-sync-pull-memo-place"
    }
}
