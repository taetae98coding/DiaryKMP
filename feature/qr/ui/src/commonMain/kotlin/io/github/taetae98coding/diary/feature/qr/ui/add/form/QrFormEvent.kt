package io.github.taetae98coding.diary.feature.qr.ui.add.form

import kotlin.uuid.Uuid

internal sealed interface QrFormEvent {
    data object ClickTagAdd : QrFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : QrFormEvent
}
