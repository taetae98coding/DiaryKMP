package io.github.taetae98coding.diary.feature.more.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.moreNavKeys() {
    subclass(MoreHomeNavKey::class, MoreHomeNavKey.serializer())
}
