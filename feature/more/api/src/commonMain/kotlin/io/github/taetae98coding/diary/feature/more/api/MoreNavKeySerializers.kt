package io.github.taetae98coding.diary.feature.more.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.moreNavKeys() {
    subclass(MoreHomeNavKey::class, MoreHomeNavKey.serializer())
}
