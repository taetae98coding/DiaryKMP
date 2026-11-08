package io.github.taetae98coding.diary.work.appaction.appfunction.memo

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction

internal class MemoAppFunction(
    private val memoAppAction: MemoAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoAppAction.list(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun finishedList(
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoAppAction.finishedList(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun get(memoId: String): AppFunctionMemoDetail? = appFunctionCall { memoAppAction.get(memoId = memoId)?.toAppFunctionMemoDetail() }

    suspend fun add(
        title: String,
        description: String?,
        color: String?,
        dateTime: AppFunctionMemoDateTimeInput?,
        primaryTagId: String?,
        tagIds: List<String>,
        placeIds: List<String>,
        webIds: List<String>,
        contactIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            memoAppAction
                .add(
                    title = title,
                    description = description,
                    color = color,
                    dateTime = dateTime?.toAppActionMemoDateTimeInput(),
                    primaryTagId = primaryTagId,
                    tagIds = tagIds,
                    placeIds = placeIds,
                    webIds = webIds,
                    contactIds = contactIds,
                ).toAppFunctionId()
        }

    suspend fun update(
        memoId: String,
        title: String?,
        description: String?,
        color: String?,
        dateTime: AppFunctionMemoDateTimeInput?,
        clearDateTime: Boolean?,
    ): AppFunctionChangedCount =
        appFunctionCall {
            memoAppAction
                .update(
                    memoId = memoId,
                    title = title,
                    description = description,
                    color = color,
                    dateTime = dateTime?.toAppActionMemoDateTimeInput(),
                    clearDateTime = clearDateTime,
                ).toAppFunctionChangedCount()
        }

    suspend fun copy(memoId: String): AppFunctionId = appFunctionCall { memoAppAction.copy(memoId = memoId).toAppFunctionId() }

    suspend fun finish(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.finish(memoId = memoId).toAppFunctionChangedCount() }

    suspend fun restart(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.restart(memoId = memoId).toAppFunctionChangedCount() }

    suspend fun delete(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.delete(memoId = memoId).toAppFunctionChangedCount() }

    suspend fun restore(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.restore(memoId = memoId).toAppFunctionChangedCount() }
}
