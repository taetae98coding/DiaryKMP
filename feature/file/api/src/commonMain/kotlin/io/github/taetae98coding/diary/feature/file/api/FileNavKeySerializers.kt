package io.github.taetae98coding.diary.feature.file.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.fileNavKeys() {
    subclass(FileHomeNavKey::class, FileHomeNavKey.serializer())
}
