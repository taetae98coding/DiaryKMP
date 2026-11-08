package io.github.taetae98coding.diary.work.appaction.appfunction.dday

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.dday.DDayAppAction

internal class DDayAppFunction(
    private val ddayAppAction: DDayAppAction,
) {
    suspend fun list(cursor: String?): AppFunctionDDayPage = appFunctionCall { ddayAppAction.list(cursor = cursor).toAppFunctionDDayPage() }

    suspend fun get(ddayId: String): AppFunctionDDayDetail? = appFunctionCall { ddayAppAction.get(ddayId = ddayId)?.toAppFunctionDDayDetail() }

    suspend fun add(
        title: String,
        description: String?,
        color: String?,
        baseDate: String?,
        countsBaseDay: Boolean?,
        repeats: List<String>,
        endDate: String?,
        overflowPolicy: String?,
        tagIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            ddayAppAction
                .add(
                    title = title,
                    description = description,
                    color = color,
                    baseDate = baseDate,
                    countsBaseDay = countsBaseDay,
                    repeats = repeats,
                    endDate = endDate,
                    overflowPolicy = overflowPolicy,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    suspend fun update(
        ddayId: String,
        title: String?,
        description: String?,
        color: String?,
        baseDate: String?,
        countsBaseDay: Boolean?,
        repeats: List<String>?,
        endDate: String?,
        clearEndDate: Boolean?,
        overflowPolicy: String?,
    ): AppFunctionChangedCount =
        appFunctionCall {
            ddayAppAction
                .update(
                    ddayId = ddayId,
                    title = title,
                    description = description,
                    color = color,
                    baseDate = baseDate,
                    countsBaseDay = countsBaseDay,
                    repeats = repeats,
                    endDate = endDate,
                    clearEndDate = clearEndDate,
                    overflowPolicy = overflowPolicy,
                ).toAppFunctionChangedCount()
        }

    suspend fun delete(ddayId: String): AppFunctionChangedCount = appFunctionCall { ddayAppAction.delete(ddayId = ddayId).toAppFunctionChangedCount() }

    suspend fun restore(ddayId: String): AppFunctionChangedCount = appFunctionCall { ddayAppAction.restore(ddayId = ddayId).toAppFunctionChangedCount() }

    suspend fun tagAdd(
        ddayId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { ddayAppAction.tagAdd(ddayId = ddayId, tagId = tagId).toAppFunctionDone() }

    suspend fun tagRemove(
        ddayId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { ddayAppAction.tagRemove(ddayId = ddayId, tagId = tagId).toAppFunctionDone() }
}
