package io.github.taetae98coding.diary.feature.playlist.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.playlistNavKeys() {
    subclass(PlaylistHomeNavKey::class, PlaylistHomeNavKey.serializer())
}
