package io.github.taetae98coding.diary.core.network.impl.checklist.datasource

import io.github.taetae98coding.diary.core.network.api.checklist.datasource.ChecklistRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.checklist.entity.ChecklistPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.checklist.entity.ChecklistPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class ChecklistRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : ChecklistRemoteDataSource {
    override suspend fun push(checklistList: List<ChecklistRemoteEntity>) {
        supabaseFunction(
            function = PUSH_CHECKLIST_FUNCTION,
            body = ChecklistPushRequestRemoteEntity(checklistList = checklistList),
        )
    }

    override suspend fun pull(usn: Long): List<ChecklistPullRemoteEntity> =
        supabaseFunction(
            function = PULL_CHECKLIST_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<ChecklistPullResponseRemoteEntity>().checklistList

    private companion object {
        const val PUSH_CHECKLIST_FUNCTION: String = "v1-sync-push-checklist"
        const val PULL_CHECKLIST_FUNCTION: String = "v1-sync-pull-checklist"
    }
}
