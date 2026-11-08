package io.github.taetae98coding.diary.work.appaction.appfunction.search

import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.toAppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction

internal class SearchAppFunction(
    private val searchAppAction: SearchAppAction,
) {
    suspend fun memo(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { searchAppAction.memo(query = query, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun tag(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionSearchTagPage = appFunctionCall { searchAppAction.tag(query = query, sort = sort, cursor = cursor).toAppFunctionSearchTagPage() }

    suspend fun place(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionSearchPlacePage = appFunctionCall { searchAppAction.place(query = query, sort = sort, cursor = cursor).toAppFunctionSearchPlacePage() }

    suspend fun web(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionSearchWebPage = appFunctionCall { searchAppAction.web(query = query, sort = sort, cursor = cursor).toAppFunctionSearchWebPage() }
}
