package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.webNavKeys() {
    subclass(WebHomeNavKey::class, WebHomeNavKey.serializer())
    subclass(WebAddNavKey::class, WebAddNavKey.serializer())
    subclass(WebDetailNavKey::class, WebDetailNavKey.serializer())
}
