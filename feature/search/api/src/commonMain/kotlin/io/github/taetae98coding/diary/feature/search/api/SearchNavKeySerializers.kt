package io.github.taetae98coding.diary.feature.search.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.searchNavKeys() {
    subclass(SearchHomeNavKey::class, SearchHomeNavKey.serializer())
}
