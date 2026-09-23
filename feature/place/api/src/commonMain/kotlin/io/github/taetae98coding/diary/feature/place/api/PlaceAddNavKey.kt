package io.github.taetae98coding.diary.feature.place.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class PlaceAddNavKey(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val initialTagId: Uuid? = null,
) : ScreenNavKey {
    override val screenName: String get() = "PlaceAdd"
}
