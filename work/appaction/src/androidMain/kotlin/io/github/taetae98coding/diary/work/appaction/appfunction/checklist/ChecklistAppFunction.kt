package io.github.taetae98coding.diary.work.appaction.appfunction.checklist

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistAppAction

internal class ChecklistAppFunction(
    private val checklistAppAction: ChecklistAppAction,
) {
    suspend fun list(cursor: String?): AppFunctionChecklistSummaryPage = appFunctionCall { checklistAppAction.list(cursor = cursor).toAppFunctionChecklistSummaryPage() }

    suspend fun get(
        checklistId: String,
        itemSort: String?,
    ): AppFunctionChecklistDetail? = appFunctionCall { checklistAppAction.get(checklistId = checklistId, itemSort = itemSort)?.toAppFunctionChecklistDetail() }

    suspend fun add(
        title: String,
        description: String?,
        items: List<String>,
    ): AppFunctionId = appFunctionCall { checklistAppAction.add(title = title, description = description, items = items).toAppFunctionId() }

    suspend fun update(
        checklistId: String,
        title: String?,
        description: String?,
    ): AppFunctionChangedCount = appFunctionCall { checklistAppAction.update(checklistId = checklistId, title = title, description = description).toAppFunctionChangedCount() }

    suspend fun reset(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.reset(checklistId = checklistId).toAppFunctionChangedCount() }

    suspend fun delete(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.delete(checklistId = checklistId).toAppFunctionChangedCount() }

    suspend fun restore(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.restore(checklistId = checklistId).toAppFunctionChangedCount() }

    suspend fun itemAdd(
        checklistId: String,
        content: String,
    ): AppFunctionChangedCount = appFunctionCall { checklistAppAction.itemAdd(checklistId = checklistId, content = content).toAppFunctionChangedCount() }

    suspend fun itemCheck(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.itemCheck(itemId = itemId).toAppFunctionChangedCount() }

    suspend fun itemUncheck(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.itemUncheck(itemId = itemId).toAppFunctionChangedCount() }

    suspend fun itemDelete(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.itemDelete(itemId = itemId).toAppFunctionChangedCount() }
}
