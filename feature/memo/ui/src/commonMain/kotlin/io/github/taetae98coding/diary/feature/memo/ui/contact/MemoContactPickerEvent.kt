package io.github.taetae98coding.diary.feature.memo.ui.contact

import kotlin.uuid.Uuid

internal sealed interface MemoContactPickerEvent {
    data object ClickAdd : MemoContactPickerEvent

    data class Select(
        val id: Uuid,
    ) : MemoContactPickerEvent

    data class Unselect(
        val id: Uuid,
    ) : MemoContactPickerEvent

    data class ChangeQuery(
        val query: String,
    ) : MemoContactPickerEvent
}
