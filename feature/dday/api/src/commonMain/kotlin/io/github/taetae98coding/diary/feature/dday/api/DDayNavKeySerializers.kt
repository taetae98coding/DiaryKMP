package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.dDayNavKeys() {
    subclass(DDayAddNavKey::class, DDayAddNavKey.serializer())
    subclass(DDayDetailNavKey::class, DDayDetailNavKey.serializer())
    subclass(DDayHomeFilterNavKey::class, DDayHomeFilterNavKey.serializer())
    subclass(DDayHomeNavKey::class, DDayHomeNavKey.serializer())
}
