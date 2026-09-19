package io.github.taetae98coding.diary.feature.playlist.ui.add

internal sealed interface MusicAddEffect {
    data object AddSucceeded : MusicAddEffect

    data object TitleBlank : MusicAddEffect

    data object ArtistBlank : MusicAddEffect
}
