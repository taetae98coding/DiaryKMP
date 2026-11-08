package io.github.taetae98coding.diary.work.appaction.appfunction.tag

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction

internal class TagLinkAppFunction(
    private val tagLinkAppAction: TagLinkAppAction,
) {
    suspend fun add(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = appFunctionCall { tagLinkAppAction.add(tagId = tagId, linkedTagId = linkedTagId).toAppFunctionDone() }

    suspend fun remove(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = appFunctionCall { tagLinkAppAction.remove(tagId = tagId, linkedTagId = linkedTagId).toAppFunctionDone() }
}
