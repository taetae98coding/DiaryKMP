package io.github.taetae98coding.diary.work.appaction.appfunction.memolink

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction

internal class MemoLinkAppFunction(
    private val memoLinkAppAction: MemoLinkAppAction,
) {
    suspend fun tagAdd(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.tagAdd(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    suspend fun tagRemove(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.tagRemove(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    suspend fun primaryTagSet(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.primaryTagSet(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    suspend fun primaryTagUnset(memoId: String): AppFunctionDone = appFunctionCall { memoLinkAppAction.primaryTagUnset(memoId = memoId).toAppFunctionDone() }

    suspend fun placeAdd(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.placeAdd(memoId = memoId, placeId = placeId).toAppFunctionDone() }

    suspend fun placeRemove(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.placeRemove(memoId = memoId, placeId = placeId).toAppFunctionDone() }

    suspend fun webAdd(
        memoId: String,
        webId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.webAdd(memoId = memoId, webId = webId).toAppFunctionDone() }

    suspend fun webRemove(
        memoId: String,
        webId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.webRemove(memoId = memoId, webId = webId).toAppFunctionDone() }

    suspend fun contactAdd(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.contactAdd(memoId = memoId, contactId = contactId).toAppFunctionDone() }

    suspend fun contactRemove(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.contactRemove(memoId = memoId, contactId = contactId).toAppFunctionDone() }
}
