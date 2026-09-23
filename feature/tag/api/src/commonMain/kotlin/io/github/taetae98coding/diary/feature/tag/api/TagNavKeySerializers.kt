package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.tagNavKeys() {
    subclass(TagHomeNavKey::class, TagHomeNavKey.serializer())
    subclass(TagHomeFilterNavKey::class, TagHomeFilterNavKey.serializer())
    subclass(TagFinishedListNavKey::class, TagFinishedListNavKey.serializer())
    subclass(TagAddNavKey::class, TagAddNavKey.serializer())
    subclass(TagDetailNavKey::class, TagDetailNavKey.serializer())
    subclass(TagMemoFinishedListNavKey::class, TagMemoFinishedListNavKey.serializer())
}
