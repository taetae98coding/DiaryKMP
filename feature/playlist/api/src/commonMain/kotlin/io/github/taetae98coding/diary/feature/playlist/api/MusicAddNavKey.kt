package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MusicAddNavKey : ScreenNavKey {
    override val screenName: String get() = "MusicAdd"
}
