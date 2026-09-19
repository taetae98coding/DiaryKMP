package io.github.taetae98coding.diary.feature.checklist.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.checklistNavKeys() {
    subclass(ChecklistHomeNavKey::class, ChecklistHomeNavKey.serializer())
}
