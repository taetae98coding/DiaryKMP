package io.github.taetae98coding.diary.feature.memo.ui.web

import kotlin.uuid.Uuid

internal interface MemoWebInputController {
    fun updateQuery(query: String)

    fun selectWeb(webId: Uuid)

    fun unselectWeb(webId: Uuid)
}
