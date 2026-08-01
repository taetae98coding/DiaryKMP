package io.github.taetae98coding.diary.feature.memo.ui.web

import kotlin.uuid.Uuid

internal sealed interface MemoWebPickerEvent {
    data object ClickAdd : MemoWebPickerEvent

    data class Select(
        val id: Uuid,
    ) : MemoWebPickerEvent

    data class Unselect(
        val id: Uuid,
    ) : MemoWebPickerEvent

    data class ChangeQuery(
        val query: String,
    ) : MemoWebPickerEvent
}
