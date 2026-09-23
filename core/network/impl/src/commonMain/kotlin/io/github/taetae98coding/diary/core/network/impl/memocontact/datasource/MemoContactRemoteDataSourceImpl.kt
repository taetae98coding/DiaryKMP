package io.github.taetae98coding.diary.core.network.impl.memocontact.datasource

import io.github.taetae98coding.diary.core.network.api.memocontact.datasource.MemoContactRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memocontact.entity.MemoContactPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memocontact.entity.MemoContactPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MemoContactRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MemoContactRemoteDataSource {
    override suspend fun push(memoContactList: List<MemoContactRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MEMO_CONTACT_FUNCTION,
            body = MemoContactPushRequestRemoteEntity(memoContactList = memoContactList),
        )
    }

    override suspend fun pull(usn: Long): List<MemoContactPullRemoteEntity> =
        supabaseFunction(
            function = PULL_MEMO_CONTACT_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MemoContactPullResponseRemoteEntity>().memoContactList

    private companion object {
        const val PUSH_MEMO_CONTACT_FUNCTION: String = "v1-sync-push-memo-contact"
        const val PULL_MEMO_CONTACT_FUNCTION: String = "v1-sync-pull-memo-contact"
    }
}
