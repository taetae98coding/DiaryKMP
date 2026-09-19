package io.github.taetae98coding.diary.feature.playlist.ui.add

internal sealed interface MusicAddEffect {
    data object AddSucceeded : MusicAddEffect

    data object LinkBlank : MusicAddEffect

    data object LinkNotYoutube : MusicAddEffect

    data object TitleBlank : MusicAddEffect

    data object ArtistBlank : MusicAddEffect

    data class LinkFetched(
        val title: String,
        val artist: String,
        val thumbnail: String,
    ) : MusicAddEffect

    data object LinkFetchFailed : MusicAddEffect
}
