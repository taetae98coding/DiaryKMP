package io.github.taetae98coding.diary.feature.memo.ui.tag

import kotlin.uuid.Uuid

internal interface MemoTagInputController {
    fun updateQuery(query: String)

    fun selectTag(tagId: Uuid)

    fun unselectTag(tagId: Uuid)

    fun selectPrimaryTag(tagId: Uuid)

    fun unselectPrimaryTag()
}
