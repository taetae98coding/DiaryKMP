package io.github.taetae98coding.diary.work.appaction.appfunction.web

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction

internal class WebAppFunction(
    private val webAppAction: WebAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionWebPage = appFunctionCall { webAppAction.list(sort = sort, cursor = cursor).toAppFunctionWebPage() }

    suspend fun get(webId: String): AppFunctionWebDetail? = appFunctionCall { webAppAction.get(webId = webId)?.toAppFunctionWebDetail() }

    suspend fun add(
        title: String,
        url: String,
        description: String?,
        headers: List<String>,
        tagIds: List<String>,
    ): AppFunctionId = appFunctionCall { webAppAction.add(title = title, url = url, description = description, headers = headers, tagIds = tagIds).toAppFunctionId() }

    suspend fun update(
        webId: String,
        title: String?,
        description: String?,
        url: String?,
        headers: List<String>?,
    ): AppFunctionChangedCount = appFunctionCall { webAppAction.update(webId = webId, title = title, description = description, url = url, headers = headers).toAppFunctionChangedCount() }

    suspend fun delete(webId: String): AppFunctionChangedCount = appFunctionCall { webAppAction.delete(webId = webId).toAppFunctionChangedCount() }

    suspend fun restore(webId: String): AppFunctionChangedCount = appFunctionCall { webAppAction.restore(webId = webId).toAppFunctionChangedCount() }
}
