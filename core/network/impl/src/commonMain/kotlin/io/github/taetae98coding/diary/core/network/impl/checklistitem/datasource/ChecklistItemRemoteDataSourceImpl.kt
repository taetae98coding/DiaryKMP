package io.github.taetae98coding.diary.core.network.impl.checklistitem.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.network.api.checklistitem.datasource.ChecklistItemRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.checklistitem.entity.ChecklistItemPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.checklistitem.entity.ChecklistItemPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body

@Inject
@ContributesBinding(AppScope::class)
internal class ChecklistItemRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : ChecklistItemRemoteDataSource {
    override suspend fun push(checklistItemList: List<ChecklistItemRemoteEntity>) {
        supabaseFunction(
            function = PUSH_CHECKLIST_ITEM_FUNCTION,
            body = ChecklistItemPushRequestRemoteEntity(checklistItemList = checklistItemList),
        )
    }

    override suspend fun pull(usn: Long): List<ChecklistItemPullRemoteEntity> =
        supabaseFunction(
            function = PULL_CHECKLIST_ITEM_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<ChecklistItemPullResponseRemoteEntity>().checklistItemList

    private companion object {
        const val PUSH_CHECKLIST_ITEM_FUNCTION: String = "v1-sync-push-checklist-item"
        const val PULL_CHECKLIST_ITEM_FUNCTION: String = "v1-sync-pull-checklist-item"
    }
}
