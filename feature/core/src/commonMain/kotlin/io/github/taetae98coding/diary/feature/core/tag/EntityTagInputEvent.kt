package io.github.taetae98coding.diary.feature.core.tag

import kotlin.uuid.Uuid

public sealed interface EntityTagInputEvent {
    public data object ClickAdd : EntityTagInputEvent

    public data class ClickTag(
        val id: Uuid,
    ) : EntityTagInputEvent
}
