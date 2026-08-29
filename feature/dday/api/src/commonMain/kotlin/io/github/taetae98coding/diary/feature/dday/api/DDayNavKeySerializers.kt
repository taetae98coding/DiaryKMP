package io.github.taetae98coding.diary.feature.dday.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.dDayNavKeys() {
    subclass(DDayHomeNavKey::class, DDayHomeNavKey.serializer())
}
