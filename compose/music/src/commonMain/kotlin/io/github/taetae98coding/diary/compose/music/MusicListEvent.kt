package io.github.taetae98coding.diary.compose.music

import kotlin.uuid.Uuid

public sealed interface MusicListEvent {
    public data class ClickMusic(
        val id: Uuid,
    ) : MusicListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : MusicListEvent
}
