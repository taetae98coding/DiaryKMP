package io.github.taetae98coding.diary.work.appaction.appfunction.tag

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction

internal class TagAppFunction(
    private val tagAppAction: TagAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionTagPage = appFunctionCall { tagAppAction.list(sort = sort, cursor = cursor).toAppFunctionTagPage() }

    suspend fun finishedList(
        sort: String?,
        cursor: String?,
    ): AppFunctionTagPage = appFunctionCall { tagAppAction.finishedList(sort = sort, cursor = cursor).toAppFunctionTagPage() }

    suspend fun get(tagId: String): AppFunctionTagDetail? = appFunctionCall { tagAppAction.get(tagId = tagId)?.toAppFunctionTagDetail() }

    suspend fun add(
        title: String,
        emoji: String?,
        description: String?,
        color: String?,
        linkedTagIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            tagAppAction.add(title = title, emoji = emoji, description = description, color = color, linkedTagIds = linkedTagIds).toAppFunctionId()
        }

    suspend fun update(
        tagId: String,
        emoji: String?,
        title: String?,
        description: String?,
        color: String?,
    ): AppFunctionChangedCount =
        appFunctionCall {
            tagAppAction.update(tagId = tagId, emoji = emoji, title = title, description = description, color = color).toAppFunctionChangedCount()
        }

    suspend fun finish(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.finish(tagId = tagId).toAppFunctionChangedCount() }

    suspend fun restart(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.restart(tagId = tagId).toAppFunctionChangedCount() }

    suspend fun delete(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.delete(tagId = tagId).toAppFunctionChangedCount() }

    suspend fun restore(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.restore(tagId = tagId).toAppFunctionChangedCount() }
}
