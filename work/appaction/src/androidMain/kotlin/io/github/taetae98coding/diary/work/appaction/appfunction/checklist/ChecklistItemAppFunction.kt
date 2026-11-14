package io.github.taetae98coding.diary.work.appaction.appfunction.checklist

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction

internal class ChecklistItemAppFunction(
    private val checklistItemAppAction: ChecklistItemAppAction,
) {
    suspend fun add(
        checklistId: String,
        content: String,
    ): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.add(checklistId = checklistId, content = content).toAppFunctionChangedCount() }

    suspend fun check(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.check(itemId = itemId).toAppFunctionChangedCount() }

    suspend fun uncheck(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.uncheck(itemId = itemId).toAppFunctionChangedCount() }

    suspend fun delete(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.delete(itemId = itemId).toAppFunctionChangedCount() }
}
