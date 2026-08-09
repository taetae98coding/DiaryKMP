package io.github.taetae98coding.diary.feature.place.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class PlaceDetailNavKey(
    val id: Uuid,
) : NavKey
