package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.playlistNavKeys() {
    subclass(PlaylistHomeNavKey::class, PlaylistHomeNavKey.serializer())
    subclass(MusicAddNavKey::class, MusicAddNavKey.serializer())
}
