package io.github.taetae98coding.diary.work.appaction.appfunction.tag

import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.toAppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactPage
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.toAppFunctionContactPage
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.dday.AppFunctionDDayPage
import io.github.taetae98coding.diary.work.appaction.appfunction.dday.toAppFunctionDDayPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.toAppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusicPage
import io.github.taetae98coding.diary.work.appaction.appfunction.music.toAppFunctionMusicPage
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQrPage
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.toAppFunctionQrPage
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

    suspend fun contactList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppFunctionContactPage = appFunctionCall { tagItemAppAction.contactList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionContactPage() }

    suspend fun checklistList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppFunctionChecklistSummaryPage = appFunctionCall { tagItemAppAction.checklistList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionChecklistSummaryPage() }

    suspend fun musicList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppFunctionMusicPage = appFunctionCall { tagItemAppAction.musicList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionMusicPage() }

    suspend fun qrList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppFunctionQrPage = appFunctionCall { tagItemAppAction.qrList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionQrPage() }

    suspend fun ddayList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppFunctionDDayPage = appFunctionCall { tagItemAppAction.ddayList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionDDayPage() }
}
