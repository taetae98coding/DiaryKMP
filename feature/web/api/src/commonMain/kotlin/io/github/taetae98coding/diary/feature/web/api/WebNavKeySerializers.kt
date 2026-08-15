package io.github.taetae98coding.diary.feature.web.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.webNavKeys() {
    subclass(WebHomeNavKey::class, WebHomeNavKey.serializer())
    subclass(WebAddNavKey::class, WebAddNavKey.serializer())
    subclass(WebDetailNavKey::class, WebDetailNavKey.serializer())
}
