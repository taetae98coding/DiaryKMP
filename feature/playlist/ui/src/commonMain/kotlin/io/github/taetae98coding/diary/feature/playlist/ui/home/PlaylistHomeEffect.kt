package io.github.taetae98coding.diary.feature.playlist.ui.home

import kotlin.uuid.Uuid

internal sealed interface PlaylistHomeEffect {
    data class Deleted(
        val id: Uuid,
    ) : PlaylistHomeEffect
}
