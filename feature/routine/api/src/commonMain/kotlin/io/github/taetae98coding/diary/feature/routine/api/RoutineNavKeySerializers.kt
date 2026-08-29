package io.github.taetae98coding.diary.feature.routine.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.routineNavKeys() {
    subclass(RoutineHomeNavKey::class, RoutineHomeNavKey.serializer())
    subclass(RoutineAddNavKey::class, RoutineAddNavKey.serializer())
}
