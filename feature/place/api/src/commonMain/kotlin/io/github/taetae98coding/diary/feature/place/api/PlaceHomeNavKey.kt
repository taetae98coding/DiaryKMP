package io.github.taetae98coding.diary.feature.place.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object PlaceHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "PlaceHome"
}
