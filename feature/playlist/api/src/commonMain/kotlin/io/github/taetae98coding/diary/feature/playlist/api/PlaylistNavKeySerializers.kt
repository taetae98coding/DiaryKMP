package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.playlistNavKeys() {
    subclass(PlaylistHomeNavKey::class, PlaylistHomeNavKey.serializer())
    subclass(MusicAddNavKey::class, MusicAddNavKey.serializer())
}
