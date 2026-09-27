package io.github.taetae98coding.diary.feature.playlist.ui.detail

internal sealed interface MusicDetailEffect {
    data object UpdateSucceeded : MusicDetailEffect

    data object LinkNotYoutube : MusicDetailEffect

    data object LinkBlank : MusicDetailEffect

    data class LinkFetched(
        val link: String,
        val title: String,
        val artist: String,
    ) : MusicDetailEffect

    data object LinkFetchFailed : MusicDetailEffect

    data object DeleteSucceeded : MusicDetailEffect
}
