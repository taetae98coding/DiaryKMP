package io.github.taetae98coding.diary.feature.memo.ui.tag

import kotlin.uuid.Uuid

internal sealed interface MemoTagPickerEvent {
    data object ClickAdd : MemoTagPickerEvent

    data object UnselectPrimary : MemoTagPickerEvent

    data class Select(
        val id: Uuid,
    ) : MemoTagPickerEvent

    data class Unselect(
        val id: Uuid,
    ) : MemoTagPickerEvent

    data class SelectPrimary(
        val id: Uuid,
    ) : MemoTagPickerEvent

    data class ChangeQuery(
        val query: String,
    ) : MemoTagPickerEvent
}
