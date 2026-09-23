package io.github.taetae98coding.diary.feature.place.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.placeNavKeys() {
    subclass(PlaceHomeNavKey::class, PlaceHomeNavKey.serializer())
    subclass(PlaceAddNavKey::class, PlaceAddNavKey.serializer())
    subclass(PlaceDetailNavKey::class, PlaceDetailNavKey.serializer())
}
