package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MusicAddNavKey : ScreenNavKey {
    override val screenName: String get() = "MusicAdd"
}
