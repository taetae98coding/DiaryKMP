package io.github.taetae98coding.diary.feature.tag.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.tagNavKeys() {
    subclass(TagHomeNavKey::class, TagHomeNavKey.serializer())
    subclass(TagHomeFilterNavKey::class, TagHomeFilterNavKey.serializer())
    subclass(TagFinishedListNavKey::class, TagFinishedListNavKey.serializer())
    subclass(TagAddNavKey::class, TagAddNavKey.serializer())
    subclass(TagDetailNavKey::class, TagDetailNavKey.serializer())
    subclass(TagMemoFinishedListNavKey::class, TagMemoFinishedListNavKey.serializer())
}
