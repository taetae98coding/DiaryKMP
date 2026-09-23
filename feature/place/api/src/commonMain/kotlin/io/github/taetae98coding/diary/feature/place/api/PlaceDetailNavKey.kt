package io.github.taetae98coding.diary.feature.place.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class PlaceDetailNavKey(
    val id: Uuid,
) : ScreenNavKey {
    override val screenName: String get() = "PlaceDetail"
}
