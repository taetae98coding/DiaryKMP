package io.github.taetae98coding.diary.feature.memo.ui.form

import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

internal sealed interface MemoFormEvent {
    data object ClickTagAdd : MemoFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : MemoFormEvent

    data object ClickWebAdd : MemoFormEvent

    data class ClickWeb(
        val id: Uuid,
    ) : MemoFormEvent

    data object ClickContactAdd : MemoFormEvent

    data class ClickContact(
        val id: Uuid,
    ) : MemoFormEvent

    data class ClickPlace(
        val id: Uuid,
    ) : MemoFormEvent

    data class ClickPlaceAdd(
        val coordinate: Coordinate?,
    ) : MemoFormEvent
}
