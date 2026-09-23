package io.github.taetae98coding.diary.feature.tag.ui.detail.form

import kotlin.uuid.Uuid

internal sealed interface TagDetailFormContentEvent {
    data object ClickLink : TagDetailFormContentEvent

    data class ClickTag(
        val id: Uuid,
    ) : TagDetailFormContentEvent
}
