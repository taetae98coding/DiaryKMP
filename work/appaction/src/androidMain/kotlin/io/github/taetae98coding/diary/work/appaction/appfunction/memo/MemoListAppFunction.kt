package io.github.taetae98coding.diary.work.appaction.appfunction.memo

import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction

internal class MemoListAppFunction(
    private val memoListAppAction: MemoListAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.list(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun finishedList(
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.finishedList(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun placeList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.placeList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun placeFinishedList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.placeFinishedList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun webList(
        webId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.webList(webId = webId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun webFinishedList(
        webId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.webFinishedList(webId = webId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun contactList(
        contactId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.contactList(contactId = contactId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun contactFinishedList(
        contactId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.contactFinishedList(contactId = contactId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }
}
