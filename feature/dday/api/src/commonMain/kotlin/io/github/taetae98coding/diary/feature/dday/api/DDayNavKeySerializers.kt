package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.dDayNavKeys() {
    subclass(DDayHomeNavKey::class, DDayHomeNavKey.serializer())
}
