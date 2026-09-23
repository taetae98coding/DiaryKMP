package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.routineNavKeys() {
    subclass(RoutineHomeNavKey::class, RoutineHomeNavKey.serializer())
    subclass(RoutineAddNavKey::class, RoutineAddNavKey.serializer())
}
