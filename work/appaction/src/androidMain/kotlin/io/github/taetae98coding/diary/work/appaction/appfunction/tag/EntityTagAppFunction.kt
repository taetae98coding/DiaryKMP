package io.github.taetae98coding.diary.work.appaction.appfunction.tag

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction

internal class EntityTagAppFunction(
    private val entityTagAppAction: EntityTagAppAction,
) {
    suspend fun add(
        kind: AppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = kind, entityId = entityId, tagId = tagId).toAppFunctionDone() }

    suspend fun remove(
        kind: AppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = kind, entityId = entityId, tagId = tagId).toAppFunctionDone() }
}
