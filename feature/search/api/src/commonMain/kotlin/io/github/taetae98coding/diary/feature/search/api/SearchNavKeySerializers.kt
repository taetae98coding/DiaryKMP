package io.github.taetae98coding.diary.feature.search.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.searchNavKeys() {
    subclass(SearchHomeNavKey::class, SearchHomeNavKey.serializer())
}
