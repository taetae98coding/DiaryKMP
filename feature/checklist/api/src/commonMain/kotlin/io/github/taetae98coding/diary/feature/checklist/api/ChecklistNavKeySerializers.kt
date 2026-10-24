package io.github.taetae98coding.diary.feature.checklist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.checklistNavKeys() {
    subclass(ChecklistAddNavKey::class, ChecklistAddNavKey.serializer())
    subclass(ChecklistDetailNavKey::class, ChecklistDetailNavKey.serializer())
    subclass(ChecklistHomeNavKey::class, ChecklistHomeNavKey.serializer())
}
