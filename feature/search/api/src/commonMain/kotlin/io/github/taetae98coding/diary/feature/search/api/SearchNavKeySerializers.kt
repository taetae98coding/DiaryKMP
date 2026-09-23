package io.github.taetae98coding.diary.feature.search.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.searchNavKeys() {
    subclass(SearchHomeNavKey::class, SearchHomeNavKey.serializer())
}
