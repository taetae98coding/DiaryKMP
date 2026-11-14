package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.routineNavKeys() {
    subclass(RoutineAddNavKey::class, RoutineAddNavKey.serializer())
    subclass(RoutineDetailNavKey::class, RoutineDetailNavKey.serializer())
    subclass(RoutineFinishedListNavKey::class, RoutineFinishedListNavKey.serializer())
    subclass(RoutineHomeFilterNavKey::class, RoutineHomeFilterNavKey.serializer())
    subclass(RoutineHomeNavKey::class, RoutineHomeNavKey.serializer())
}
