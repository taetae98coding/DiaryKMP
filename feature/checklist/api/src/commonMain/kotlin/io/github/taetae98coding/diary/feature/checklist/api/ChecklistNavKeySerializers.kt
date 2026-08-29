package io.github.taetae98coding.diary.feature.checklist.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.checklistNavKeys() {
    subclass(ChecklistHomeNavKey::class, ChecklistHomeNavKey.serializer())
}
