package io.github.taetae98coding.diary.feature.place.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object PlaceHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "PlaceHome"
}
