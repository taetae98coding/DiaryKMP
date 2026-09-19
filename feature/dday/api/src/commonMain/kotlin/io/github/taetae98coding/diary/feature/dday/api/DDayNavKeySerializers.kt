package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.dDayNavKeys() {
    subclass(DDayHomeNavKey::class, DDayHomeNavKey.serializer())
}
