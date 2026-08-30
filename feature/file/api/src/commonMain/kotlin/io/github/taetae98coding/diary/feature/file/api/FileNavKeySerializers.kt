package io.github.taetae98coding.diary.feature.file.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.fileNavKeys() {
    subclass(FileHomeNavKey::class, FileHomeNavKey.serializer())
}
