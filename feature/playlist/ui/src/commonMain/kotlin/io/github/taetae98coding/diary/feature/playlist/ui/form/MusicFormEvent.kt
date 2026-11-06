package io.github.taetae98coding.diary.feature.playlist.ui.form

import kotlin.uuid.Uuid

internal sealed interface MusicFormEvent {
    data object ClickTagAdd : MusicFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : MusicFormEvent
}
