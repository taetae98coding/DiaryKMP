package io.github.taetae98coding.diary.feature.memo.ui.contact

import kotlin.uuid.Uuid

internal interface MemoContactInputController {
    fun updateQuery(query: String)

    fun selectContact(contactId: Uuid)

    fun unselectContact(contactId: Uuid)
}
