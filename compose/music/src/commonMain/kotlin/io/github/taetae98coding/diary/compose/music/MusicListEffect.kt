package io.github.taetae98coding.diary.compose.music

import kotlin.uuid.Uuid

public sealed interface MusicListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : MusicListEffect
}
