package io.github.taetae98coding.diary.feature.account.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.accountNavKeys() {
    subclass(ProfileNavKey::class, ProfileNavKey.serializer())
    subclass(ProfileImageEditNavKey::class, ProfileImageEditNavKey.serializer())
    subclass(ProfileImageViewerNavKey::class, ProfileImageViewerNavKey.serializer())
}
