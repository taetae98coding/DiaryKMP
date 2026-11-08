package io.github.taetae98coding.diary.work.appaction.appfunction.tag

import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.toAppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction

internal class TagItemAppFunction(
    private val tagItemAppAction: TagItemAppAction,
) {
    suspend fun memoList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { tagItemAppAction.memoList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun memoFinishedList(
        tagId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { tagItemAppAction.memoFinishedList(tagId = tagId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun webList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppFunctionTagWebPage = appFunctionCall { tagItemAppAction.webList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionTagWebPage() }

    suspend fun placeList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppFunctionTagPlacePage = appFunctionCall { tagItemAppAction.placeList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionTagPlacePage() }
}
