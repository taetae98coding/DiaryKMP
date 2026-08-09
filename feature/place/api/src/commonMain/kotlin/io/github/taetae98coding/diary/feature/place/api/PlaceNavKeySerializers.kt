package io.github.taetae98coding.diary.feature.place.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.placeNavKeys() {
    subclass(PlaceHomeNavKey::class, PlaceHomeNavKey.serializer())
    subclass(PlaceAddNavKey::class, PlaceAddNavKey.serializer())
    subclass(PlaceDetailNavKey::class, PlaceDetailNavKey.serializer())
}
